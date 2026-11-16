/**
 * This class is part of a public API.
 */
package io.github.albert200000.dlangdt.common.preference;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DEnvPreferencePlugin extends AbstractUIPlugin {

	private static DEnvPreferencePlugin plugin;

	public static DEnvPreferencePlugin getPlugin() {
		if (plugin == null) {
			plugin = new DEnvPreferencePlugin();
		}
		return plugin;
	}

}
