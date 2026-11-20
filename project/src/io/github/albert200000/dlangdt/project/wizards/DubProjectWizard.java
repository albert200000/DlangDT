/*******************************************************************************
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

package io.github.albert200000.dlangdt.project.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import io.github.albert200000.dlangdt.common.tooling.DToolChain;
import io.github.albert200000.dlangdt.project.builder.DNature;
import io.github.albert200000.dlangdt.project.ui.icons.DIcons;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DubProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public void addPages() {
		super.addPages();
		IWizardPage mainPage = this.getPage("basicNewProjectPage");
		mainPage.setTitle("Create a D Project");
		mainPage.setImageDescriptor(ImageDescriptor.createFromFile(DIcons.class, DIcons.DDT_PROJECT_LARGE));
	}

	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();

		IProject newProject = getNewProject();

		if (!DToolChain.isDUBAvailable()) {
			Shell activeShell = Display.getDefault().getActiveShell();
			MessageDialog.openError(activeShell, "DUB tool not found",
				"DUB tool not found. Please install DMD (and dub) from https://dlang.org/download.html or set the DBinaryPath preference to the `dub` binary location.");
			return false;
		}

		result &= DToolChain.syncExecute(newProject, new String[] { "init", "-n" });
		try {
			IProjectDescription projectDescription = newProject.getDescription();
			String[] prevNatures = projectDescription.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = DNature.NATURE_ID;
			projectDescription.setNatureIds(newNatures);
			newProject.setDescription(projectDescription, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return result;
	}

}
