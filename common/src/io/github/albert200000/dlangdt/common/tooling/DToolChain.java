/*******************************************************************************
 * Copyright (c) 2026 Contributors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     zshongyi - GoDT
 *     albert200000 - DlangDT
 *******************************************************************************/

package io.github.albert200000.dlangdt.common.tooling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import io.github.albert200000.dlangdt.common.preference.DEnvPreferenceConstants;
import io.github.albert200000.dlangdt.common.preference.DEnvPreferencePlugin;
import io.github.albert200000.dlangdt.common.ui.consoles.DToolChainConsole;
import io.github.albert200000.dlangdt.common.utils.LogUtils;

public class DToolChain {

	private static final String PLUGIN_ID = "DToolChain";
	private static final int QUEUE_CAPACITY = 10;

	private static final IProcessExecutor processExecutor = new ProcessExecutor();

	private static final ReentrantLock runningLock = new ReentrantLock();
	private static final ReentrantLock preemptiveLock = new ReentrantLock();
	private static final Object queueMonitor = new Object();
	private static final BlockingQueue<DCommandJob> jobQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

	private static final ExecutorService outputRedirectExecutor = Executors.newCachedThreadPool(runnable -> {
		Thread t = new Thread(runnable, "DToolChain-Output-Reader");
		t.setDaemon(true);
		return t;
	});

	private static volatile boolean workerRunning = false;

	private static class DCommandJob extends Job {
		private final IProject project;
		private final List<String> commands;

		private volatile Process process;
		private volatile boolean resultStatus = false;

		public DCommandJob(IProject project, List<String> commands) {
			super("D Command: " + String.join(" ", commands));
			this.project = project;
			this.commands = new ArrayList<>(commands);
			this.setUser(true);
		}

		@Override
		protected void canceling() {
			Process current = this.process;
			if (current != null && current.isAlive()) {
				current.destroyForcibly();
			}
			super.canceling();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DToolChainConsole.bindProject(project);

			List<String> fullCommand = new ArrayList<>(commands.size() + 1);
			fullCommand.add(getDubBinary());
			fullCommand.addAll(commands);

			String commandString = StringUtils.join(fullCommand, " ");

			ProcessBuilder builder = new ProcessBuilder(fullCommand);
			if (project != null && project.getLocation() != null) {
				builder.directory(project.getLocation().toFile());
			}

			runningLock.lock();
			try {
				monitor.beginTask("Waiting for: " + commandString, IProgressMonitor.UNKNOWN);
				DToolChainConsole.stdout(String.format("[started] >>>>> %s <<<<<", commandString));

				process = builder.start();

				Future<?> stdoutFuture = outputRedirectExecutor.submit(
						() -> streamOutput(process.getInputStream(), false, monitor));
				Future<?> stderrFuture = outputRedirectExecutor.submit(
						() -> streamOutput(process.getErrorStream(), true, monitor));

				while (!monitor.isCanceled() && process.isAlive()) {
					if (process.waitFor(500, TimeUnit.MILLISECONDS)) {
						break;
					}
				}

				if (monitor.isCanceled()) {
					destroyProcessIfAlive();
					awaitStreamTask(stdoutFuture);
					awaitStreamTask(stderrFuture);
					DToolChainConsole.stdout(String.format("[cancelled] >>>>> %s <<<<<", commandString));
					return Status.CANCEL_STATUS;
				}

				int exitCode = process.waitFor();
				awaitStreamTask(stdoutFuture);
				awaitStreamTask(stderrFuture);

				if (exitCode != 0) {
					String msg = String.format("Command failed with exit code %d: %s", exitCode, commandString);
					DToolChainConsole.stderr(String.format("[failed (exit code %d)] >>>>> %s <<<<<", exitCode, commandString));
					return new Status(IStatus.ERROR, PLUGIN_ID, exitCode, msg, null);
				}

				DToolChainConsole.stdout(String.format("[succeeded] >>>>> %s <<<<<", commandString));
				resultStatus = true;
				return Status.OK_STATUS;

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				destroyProcessIfAlive();
				DToolChainConsole.stderr(e.getMessage());
				LogUtils.logError("Execute D Command " + commandString + " interrupted:", e);
				return Status.CANCEL_STATUS;
			} catch (IOException e) {
				destroyProcessIfAlive();
				DToolChainConsole.stderr(e.getMessage());
				LogUtils.logError("Execute D Command " + commandString + " failed:", e);
				return new Status(IStatus.ERROR, PLUGIN_ID, 2, e.getMessage(), e);
			} finally {
				runningLock.unlock();
				monitor.done();
			}
		}

		private void streamOutput(InputStream stream, boolean isError, IProgressMonitor monitor) {
			try (InputStreamReader reader = new InputStreamReader(stream);
					BufferedReader bufferedReader = new BufferedReader(reader)) {
				String line;
				while ((line = bufferedReader.readLine()) != null && !monitor.isCanceled()) {
					if (isError) {
						DToolChainConsole.stderr(line);
					} else {
						DToolChainConsole.stdout(line);
					}
				}
			} catch (IOException e) {
				if (!monitor.isCanceled()) {
					LogUtils.logError("Error reading process " + (isError ? "stderr" : "stdout") + " output: ", e);
				}
			}
		}

		private void awaitStreamTask(Future<?> future) {
			try {
				future.get(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException | TimeoutException e) {
				future.cancel(true);
			}
		}

		private void destroyProcessIfAlive() {
			Process current = this.process;
			if (current != null && current.isAlive()) {
				current.destroyForcibly();
			}
		}

		public boolean getResultStatus() {
			return resultStatus;
		}
	}

	public static String getDubBinary() {
		String dubBinary = DEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(DEnvPreferenceConstants.DUB_BINARY_PATH);
		if (dubBinary == null || dubBinary.isBlank()) {
			String osName = System.getProperty("os.name", "").toLowerCase();
			dubBinary = osName.startsWith("win") ? "dub.exe" : "dub";
		}
		return dubBinary;
	}

	/**
	 * Checks if the dub executable is available in PATH or via DUB_BINARY_PATH preference.
	 * Returns true if `dub --version` exits with code 0, false otherwise.
	 */
	public static boolean isDUBAvailable() {
		List<String> cmd = Arrays.asList(getDubBinary(), "--version");
		int exitCode = processExecutor.execute(cmd);
		return exitCode == 0;
	}

	public static void asyncExecute(IProject project, String[] commands) {
		asyncExecute(project, new ArrayList<>(Arrays.asList(commands)));
	}

	public static void asyncExecute(IProject project, List<String> commands) {
		DCommandJob job = new DCommandJob(project, commands);

		if (!jobQueue.offer(job)) {
			showErrorDialogAsync("Error", "Exec D Tool Job Failed: " + job.getName());
			return;
		}
		ensureWorkerStarted();
	}

	public static boolean syncExecute(IProject project, String[] commands) {
		return syncExecute(project, new ArrayList<>(Arrays.asList(commands)));
	}

	public static boolean syncExecute(IProject project, List<String> commands) {
		DCommandJob job = new DCommandJob(project, commands);
		if (!preemptiveLock.tryLock()) {
			showErrorDialogAsync("Error", "D Tool Job Failed: Too Busy");
			ensureWorkerStarted();
			return false;
		}

		try {
			Shell activeShell = getActiveShellSafe();
			if (activeShell == null) {
				job.schedule();
				job.join();
			} else {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(activeShell);
				dialog.run(true, true, monitor -> {
					job.schedule();
					job.join();
				});
			}
		} catch (InvocationTargetException e) {
			LogUtils.logError("Error during synchronous D command execution:", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			job.cancel();
		} finally {
			preemptiveLock.unlock();
			ensureWorkerStarted();
		}

		return job.getResultStatus();
	}

	private static void ensureWorkerStarted() {
		synchronized (queueMonitor) {
			if (workerRunning || preemptiveLock.isLocked() || jobQueue.isEmpty()) {
				return;
			}
			workerRunning = true;
			Thread worker = new Thread(DToolChain::drainQueue, "DToolChain-Queue-Worker");
			worker.setDaemon(true);
			worker.start();
		}
	}

	private static void drainQueue() {
		try {
			while (true) {
				DCommandJob job;
				synchronized (queueMonitor) {
					if (preemptiveLock.isLocked() || jobQueue.isEmpty()) {
						workerRunning = false;
						return;
					}
					job = jobQueue.poll();
					if (job == null) {
						workerRunning = false;
						return;
					}
				}

				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					job.cancel();
					synchronized (queueMonitor) {
						workerRunning = false;
					}
					return;
				}
			}
		} finally {
			synchronized (queueMonitor) {
				workerRunning = false;
				if (!preemptiveLock.isLocked() && !jobQueue.isEmpty()) {
					ensureWorkerStarted();
				}
			}
		}
	}

	private static Shell getActiveShellSafe() {
		Display display = Display.getDefault();
		if (display == null || display.isDisposed()) {
			return null;
		}
		if (Display.getCurrent() != null) {
			return display.getActiveShell();
		}
		Shell[] holder = new Shell[1];
		display.syncExec(() -> {
			if (!display.isDisposed()) {
				holder[0] = display.getActiveShell();
			}
		});
		return holder[0];
	}

	private static void showErrorDialogAsync(String title, String message) {
		Display display = Display.getDefault();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(() -> {
			if (!display.isDisposed()) {
				MessageDialog.openError(display.getActiveShell(), title, message);
			}
		});
	}
}