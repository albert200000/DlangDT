/**
 * 
 */
package io.github.albert200000.dlangdt.launch.ui.tabs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.launch.ui.CMainTab2;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import io.github.albert200000.dlangdt.launch.ui.Messages;
import io.github.albert200000.dlangdt.project.builder.DNature;
import io.github.albert200000.dlangdt.project.ui.widgets.DProjectLabelProvider;

/**
 * @author zshongyi
 *
 */
public class MainTab extends CMainTab2 {

	public static final String TAB_ID = "io.github.albert200000.dlangdt.launch.runApplicationLaunch.mainTab"; //$NON-NLS-1$

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		fSearchButton.setEnabled(false);
		fProgLabel.setText(Messages.mainTabDApplication);
	}

	@Override
	protected void handleProjectButtonSelected() {
		IProject project = chooseDProject();
		if (project == null) {
			return;
		}
		String projectName = project.getName();
		fProjText.setText(projectName);
	}

	private IProject chooseDProject() {

		IProject[] projects = getDProjects();
		ILabelProvider labelProvider = new DProjectLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(Messages.mainTabProjectSelection);
		dialog.setMessage(Messages.mainTabChooseProjectToConstrainSearchForProgram);
		dialog.setElements(projects);
		String projectName = fProjText.getText().trim();
		if (projectName.length() > 0) {
			IProject project = getDProject(projectName);
			if (project != null) {
				dialog.setInitialSelections(project);
			}
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}

		return null;
	}

	private IProject[] getDProjects() {
		List<IProject> projectList = new LinkedList<IProject>();
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			for (IProject project : projects) {
				if (project.isOpen() && project.hasNature(DNature.NATURE_ID)) {
					projectList.add(project);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return projectList.toArray(new IProject[projectList.size()]);
	}

	private IProject getDProject(String projectName) {
		try {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			for (IProject project : projects) {
				if (project.isOpen() && project.hasNature(DNature.NATURE_ID)
						&& project.getName().equals(projectName)) {
					return project;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String handleBrowseButtonSelected(String title) {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setText(title);
		IProject project = getDProject(fProjText.getText().trim());
		if (project != null) {
			fileDialog.setFileName(project.getLocation().toOSString());
			fileDialog.setFilterPath(project.getLocation().toOSString());
		}
		return fileDialog.open();
	}

}
