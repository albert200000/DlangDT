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

package io.github.albert200000.dlangdt.project.ui.widgets;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import io.github.albert200000.dlangdt.project.ui.icons.DIcons;

public class DProjectLabelProvider implements ILabelProvider {

	private static final Image projectIcon = ImageDescriptor.createFromFile(DIcons.class, DIcons.DlangDT_PROJECT_SMALL)
			.createImage();

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		return projectIcon;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IProject) {
			return ((IProject) element).getName();
		}
		return null;
	}

}
