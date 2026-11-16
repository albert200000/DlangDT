package io.github.albert200000.dlangdt.project.gomod.commands;

public class DubDownloadHandler extends DubModCommandsHandler {

	@Override
	public String[] getModCommandParameters() {
		return new String[] { "fetch" };
	}

}
