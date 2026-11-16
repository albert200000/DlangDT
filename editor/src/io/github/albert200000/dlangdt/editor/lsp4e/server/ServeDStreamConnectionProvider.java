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

/**
 * @author zshongyi
 */
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

        boolean addDPath = ServeDPreferencePlugin.getPlugin().getPreferenceStore()
                .getBoolean(ServeDPreferenceConstants.ADD_D_PATH);
        if (!addDPath) {
            return processBuilder;
        }

        String dBinaryPath = DEnvPreferencePlugin.getPlugin().getPreferenceStore()
                .getString(DEnvPreferenceConstants.D_BINARY_PATH);
        if (dBinaryPath == null || dBinaryPath.isBlank()) {
            return processBuilder;
        }

        String dPath = new File(dBinaryPath).getParent();
        if (dPath == null) {
            return processBuilder;
        }

        String currentPath = env.get("PATH");
        if (currentPath == null) {
            currentPath = env.get("Path"); // Windows fallback
        }
        if (currentPath != null) {
            for (String path : currentPath.split(File.pathSeparator)) {
                if (dPath.equals(path)) {
                    return processBuilder;
                }
            }
            env.put("PATH", dPath + File.pathSeparator + currentPath);
        } else {
            env.put("PATH", dPath);
        }

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