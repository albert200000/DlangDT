package io.github.albert200000.dlangdt.editor.preference;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ServeDPreferencePlugin extends AbstractUIPlugin {

	private static ServeDPreferencePlugin plugin;

	public static ServeDPreferencePlugin getPlugin() {
		if (plugin == null) {
			plugin = new ServeDPreferencePlugin();
		}
		return plugin;
	}

}