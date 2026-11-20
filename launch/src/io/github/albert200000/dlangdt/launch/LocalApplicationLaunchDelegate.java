package io.github.albert200000.dlangdt.launch;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import io.github.albert200000.dlangdt.common.tooling.DToolChain;
import io.github.albert200000.dlangdt.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;

public class LocalApplicationLaunchDelegate extends AbstractCLaunchDelegate {

    @Override
    public String getPluginID() {
        return "io.github.albert200000.dlangdt";
    }

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
        try {
            org.eclipse.core.resources.IResource[] resources = configuration.getMappedResources();
            if (resources == null || resources.length == 0) {
                LogUtils.logError("LocalApplicationLaunchDelegate: no project", null);
                return;
            }
            IProject project = resources[0].getProject();
            List<String> commands = new ArrayList<>();
            commands.add("run");
            DToolChain.asyncExecute(project, commands);
        } catch (Exception e) {
            LogUtils.logError("LocalApplicationLaunchDelegate.launch failed", e);
        }
    }
}
