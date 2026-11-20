package io.github.albert200000.dlangdt.common.preference;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.dialogs.EmptyPreferencePage;

@SuppressWarnings("restriction")
public class CategoryPreferencePage extends EmptyPreferencePage implements IWorkbenchPreferencePage {

	public CategoryPreferencePage() {
		this.setDescription("Expand the tree to edit preferences for DlangDT.");
	}

}
