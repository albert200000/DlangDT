/**
 * 
 */
package io.github.albert200000.dlangdt.editor.preference;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.albert200000.dlangdt.editor.ui.Messages;

/**
 * @author zshongyi
 *
 */
public class ServeDPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public ServeDPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		this.setPreferenceStore(ServeDPreferencePlugin.getPlugin().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {

		FileFieldEditor servedPath = new FileFieldEditor(ServeDPreferenceConstants.SERVED_PATH,
				Messages.servedPathLabel, true, getFieldEditorParent()) {

			@Override
			protected void fireValueChanged(String property, Object oldValue, Object newValue) {
				super.fireValueChanged(property, oldValue, newValue);
				String filePath = newValue.toString();
				if (!filePath.equals("")) {
					File file = new File(filePath);
					if (file.exists()) {
						setValid(true);
					} else {
						MessageDialog.openError(null, "Illegal File", "Not an available serve-d binary file.");
						setValid(false);
					}
				} else {
					setValid(true);
				}
				refreshValidState();
			}
		};

		servedPath.setFileExtensions(new String[] { getBinaryName() });
		this.addField(servedPath);

		BooleanFieldEditor enableDDTDeclaration = new BooleanFieldEditor(ServeDPreferenceConstants.DDT_DECLARATION,
				Messages.enableDDTDeclarationLabel, getFieldEditorParent());
		this.addField(enableDDTDeclaration);

// TODO: We need to test on the macOS system whether the current version can load serve-d normally with this option disabled
//		BooleanFieldEditor addDPath = new BooleanFieldEditor(ServeDPreferenceConstants.ADD_D_PATH,
//				Messages.addDPathBeforeRunServeD, getFieldEditorParent()) {
//			@Override
//			protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
//				super.fireStateChanged(property, oldValue, newValue);
//				for (LanguageServer languageServer : LanguageServiceAccessor.getActiveLanguageServers(
//						capabilities -> LSPEclipseUtils.hasCapability(capabilities.getWorkspaceSymbolProvider()))) {
//					// FIXME: not work
//					if (languageServer instanceof ServeDStreamConnectionProvider) {
//						languageServer.shutdown();
//					}
//				}
//			}
//		};
//
//		this.addField(addDPath);

	}

	public static String getBinaryName() {
		String servedBinary = "serve-d";
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			servedBinary = "serve-d.exe";
		}
		return servedBinary;
	}

}
