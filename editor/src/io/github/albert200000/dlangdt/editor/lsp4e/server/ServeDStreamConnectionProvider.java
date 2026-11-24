package io.github.albert200000.dlangdt.editor.lsp4e.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.swt.widgets.Display;

import io.github.albert200000.dlangdt.common.preference.DEnvPreferenceConstants;
import io.github.albert200000.dlangdt.common.preference.DEnvPreferencePlugin;
import io.github.albert200000.dlangdt.editor.preference.ServeDPreferenceConstants;
import io.github.albert200000.dlangdt.editor.preference.ServeDPreferencePage;
import io.github.albert200000.dlangdt.editor.preference.ServeDPreferencePlugin;

public class ServeDStreamConnectionProvider extends ProcessStreamConnectionProvider {

    public ServeDStreamConnectionProvider() {
        String servedBinary = ServeDPreferencePlugin.getPlugin().getPreferenceStore()
                .getString(ServeDPreferenceConstants.SERVED_PATH);
        if (servedBinary == null || servedBinary.isBlank()) {
            servedBinary = ServeDPreferencePage.getBinaryName();
        }

        List<String> commands = new ArrayList<>();
        commands.add(servedBinary);
        setCommands(commands);
    }

    @Override
    protected ProcessBuilder createProcessBuilder() {
        ProcessBuilder processBuilder = super.createProcessBuilder();
        Map<String, String> env = processBuilder.environment();
        return processBuilder;
    }

    @Override
    public void start() {
        try {
            super.start();
        } catch (IOException e) {
            Display.getDefault().asyncExec(() ->
                    MessageDialog.openError(null, "Can't start serve-d",
                            "Please download it with command:\n" + "dub install serve-d"));
            // TODO: use plugin logger instead of printStackTrace
            e.printStackTrace();
        }
    }
}
