package io.github.albert200000.dlangdt.common.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String openDeclarationLabel;
	public static String openReturnTypeLabel;
	public static String enableDDTDeclarationLabel;
	public static String dPathLabel;

	public static String goPathLabel;

	static {
		NLS.initializeMessages("io.github.albert200000.dlangdt.common.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
