/*******************************************************************************
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     zshongyi - GoDT
 *     albert200000 - DlangDT
 *******************************************************************************/

package io.github.albert200000.dlangdt.common.ui.consoles;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class DToolChainConsolePatternMatchListener implements IPatternMatchListener {

	// Matches:
	//   source/app.d(6):
	//   source/app.d(6,10):
	//   object.Exception@source/app.d(6):
	//   source/app.d:6:10:
	private static final String MATCH_REGEX_PATTERN = "[^:(),\\s]*\\.d(?:\\([0-9]+(?:,[0-9]+)?\\)|:[0-9]+:[0-9]+):";

	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		this.console = console;
	}

	@Override
	public void disconnect() {
		this.console = null;
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
	    try {
	        String fileReferenceText = this.console.getDocument().get(event.getOffset(), event.getLength());

	        int openParen = fileReferenceText.indexOf('(');
	        int closeParen = -1;
	        if (openParen != -1) {
	            closeParen = fileReferenceText.indexOf(')', openParen);
	        }
	        int lastColon = fileReferenceText.lastIndexOf(':');

	        boolean isParenMarker = (openParen != -1 && closeParen != -1 && closeParen < lastColon);

	        String absFilePath;
	        int lineNumber;

	        if (isParenMarker) {
	            absFilePath = fileReferenceText.substring(0, openParen);
	            String lineColText = fileReferenceText.substring(openParen + 1, closeParen);
	            String[] parts = lineColText.split(",");
	            if (parts.length >= 1 && parts[0].matches("\\d+")) {
	                lineNumber = Integer.parseInt(parts[0]);
	            } else {
	                return;
	            }
	        } else {
	            int separatorIndex = fileReferenceText.indexOf(':');
	            if (separatorIndex == -1) {
	                return;
	            }
	            absFilePath = fileReferenceText.substring(0, separatorIndex);

	            String rest = fileReferenceText.substring(separatorIndex + 1);
	            separatorIndex = rest.indexOf(':');
	            if (separatorIndex == -1) {
	                return;
	            }
	            try {
	                lineNumber = Integer.parseInt(rest.substring(0, separatorIndex));
	            } catch (NumberFormatException e) {
	                return;
	            }
	        }

	        // Strip any "ExceptionType@" prefix (e.g. "object.Exception@source/app.d").
	        int atIndex = absFilePath.lastIndexOf('@');
	        if (atIndex != -1) {
	            absFilePath = absFilePath.substring(atIndex + 1);
	        }
	        if (absFilePath.isEmpty()) {
	            return;
	        }

	        IProject project = (IProject) console.getAttribute(DToolChainConsole.ATTRIBUTE_PROJECT);

	        File targetFile;
	        if (project == null) {
	            targetFile = new File(absFilePath);
	        } else {
	            targetFile = new File(project.getLocation().toFile(), absFilePath);
	        }

	        IHyperlink hyperlink = makeHyperlink(targetFile, lineNumber);

	        // --- Compute hyperlink region: only "app.d(6)" should be clickable ---
	        // Work in absolute indices into fileReferenceText for everything below.
	        int pathStart = (atIndex != -1) ? atIndex + 1 : 0;

	        // Skip directory components: last '/' or '\' inside the path portion.
	        int lastSlash = Math.max(
	                fileReferenceText.lastIndexOf('/'),
	                fileReferenceText.lastIndexOf('\\'));
	        int fileNameStart;
	        if (lastSlash >= pathStart) {
	            fileNameStart = lastSlash + 1;
	        } else {
	            fileNameStart = pathStart;
	        }

	        // End of the clickable region: position of the trailing ':' (the last char).
	        int linkEnd = fileReferenceText.length() - 1; // exclude trailing ':'

	        if (linkEnd > fileNameStart) {
	            this.console.addHyperlink(
	                    hyperlink,
	                    event.getOffset() + fileNameStart,
	                    linkEnd - fileNameStart);
	        }
	    } catch (NumberFormatException | BadLocationException exception) {
	        exception.printStackTrace();
	    }
	}

	private static IHyperlink makeHyperlink(File targetFile, int lineNumber) {
		return new IHyperlink() {

			@Override
			public void linkEntered() {

			}

			@Override
			public void linkExited() {

			}

			@Override
			public void linkActivated() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IEditorPart editorPart = IDE.openEditorOnFileStore(page, EFS.getStore(targetFile.toURI()));
					goToLine(editorPart, lineNumber);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	private static void goToLine(IEditorPart editorPart, int lineNumber) {
		if (editorPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

			if (document != null) {
				try {
					IRegion region = document.getLineInformation(lineNumber - 1);
					textEditor.selectAndReveal(region.getOffset(), region.getLength());
				} catch (BadLocationException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	@Override
	public String getPattern() {
		return MATCH_REGEX_PATTERN;
	}

	@Override
	public int getCompilerFlags() {
		return 0;
	}

	@Override
	public String getLineQualifier() {
		return null;
	}

}
