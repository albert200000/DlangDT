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

package io.github.albert200000.dlangdt.common.preference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.albert200000.dlangdt.common.ui.Messages;

public class DEnvPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public DEnvPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		this.setPreferenceStore(DEnvPreferencePlugin.getPlugin().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createFieldEditors() {

		FileFieldEditor dPath = new FileFieldEditor(DEnvPreferenceConstants.DUB_BINARY_PATH, Messages.dPathLabel,
				true, getFieldEditorParent()) {

			@Override
			protected void fireValueChanged(String property, Object oldValue, Object newValue) {
				super.fireValueChanged(property, oldValue, newValue);
				String filePath = newValue.toString();
				if (!filePath.equals("")) {
					ProcessBuilder processBuilder = new ProcessBuilder();
					processBuilder.command(filePath, "version");
					try {
						Process process = processBuilder.start();
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						String firstLine = bufferedReader.readLine();
						Pattern pattern = Pattern.compile("^dub version (\\d+\\.){2}\\d+");
						Matcher matcher = pattern.matcher(firstLine);
						if (matcher.find()) {
							setValid(true);
						} else {
							MessageDialog.openError(null, "Illegal File", "Not an available d binary file.");
							setValid(false);
						}
					} catch (IOException e) {
						MessageDialog.openError(null, "Illegal File", e.getMessage());
						e.printStackTrace();
						setValid(false);
					}
				} else {
					setValid(true);
				}
				refreshValidState();
			}
		};

		dPath.setFileExtensions(new String[] { getBinaryName() });
		this.addField(dPath);
	}

	public static String getBinaryName() {
		String dBinary = "d";
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			dBinary = "d.exe";
		}
		return dBinary;
	}

}
