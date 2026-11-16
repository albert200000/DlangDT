/**
 * 
 */
package io.github.albert200000.dlangdt.project.gomod.commands;

/**
 * @author zshongyi
 *
 */
public class DubTidyHandler extends DubModCommandsHandler {

	@Override
	public String[] getModCommandParameters() {
		return new String[] { "fetch" };
	}

}
