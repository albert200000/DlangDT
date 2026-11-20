package io.github.albert200000.dlangdt.editor.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String openDeclarationLabel;
	public static String openReturnTypeLabel;
	public static String enableDlangDTDeclarationLabel;
	public static String servedPathLabel;
	public static String addDPathBeforeRunServeD;
	public static String goplsPathLabel;
	public static String addGoPathBeforeRunGopls;

	static {
		NLS.initializeMessages("io.github.albert200000.dlangdt.editor.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
