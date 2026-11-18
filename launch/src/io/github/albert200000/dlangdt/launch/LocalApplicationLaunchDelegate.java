package io.github.albert200000.dlangdt.launch;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;

/**
 * @author zshongyi
 *
 */
public class LocalApplicationLaunchDelegate extends AbstractCLaunchDelegate2 {

    @Override
    public String getPluginID() {
        return "io.github.albert200000.dlangdt";
    }
}