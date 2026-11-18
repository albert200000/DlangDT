package io.github.albert200000.dlangdt.project.gomod.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import io.github.albert200000.dlangdt.common.tooling.DToolChain;

public abstract class DubModCommandsHandler extends AbstractHandler {

	public abstract String[] getModCommandParameters();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			IFile selectedFile = (IFile) ((IStructuredSelection) selection).getFirstElement();
			IProject project = selectedFile.getProject();
			DToolChain.asyncExecute(project, getDubParameters());
		}

		return null;

	}

	private String[] getDubParameters() {
		String[] modParams = getModCommandParameters();
		String[] c = new String[1 + modParams.length];
		c[0] = "dub";
		System.arraycopy(modParams, 0, c, 1, modParams.length);
		return c;
	}

}
