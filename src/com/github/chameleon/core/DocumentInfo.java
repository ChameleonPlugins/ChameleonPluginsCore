package com.github.chameleon.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class DocumentInfo {

	public String typedString;
	protected IDocument document;
	protected ITextSelection documentTextSelection;
	protected boolean console = false;
	public int globalOffset;
	protected String globalLine;
	protected int lineNumber;
	protected int lineLength;
	protected String testingLine = "Testing Line";
	protected int testingOffset = -1;
	
	protected boolean testing = false;
	protected ITextEditor editor;
	protected IEditorPart editorPart;

	public DocumentInfo() {
	}

	public DocumentInfo(boolean testing, String testingLine, int testingOffset) throws CoreException {
		this.testing = testing;
		this.testingLine = testingLine;
		this.testingOffset = testingOffset;
		getDocumentInfo();
	}
	
	protected void getDocumentInfo() throws CoreException {
		if (!testing ) {
			IEditorPart editorPart = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			ITextEditor editor = (ITextEditor) editorPart
					.getAdapter(ITextEditor.class);
			IDocumentProvider provider = editor.getDocumentProvider();
			document = provider.getDocument(editorPart.getEditorInput());
			documentTextSelection = (ITextSelection) editorPart.getSite()
					.getSelectionProvider().getSelection();

			IEditorInput input = editorPart.getEditorInput();
			if (input instanceof FileEditorInput) {
				// IFile file = ((FileEditorInput) input).getFile();
				// InputStream is = file.getContents();
				// TODO get contents from InputStream
			}

			if (!console ) {
				globalOffset = documentTextSelection.getOffset();
				setGlobalLine(documentTextSelection.getText());
				try {
					lineNumber = document.getLineOfOffset(globalOffset);
					lineLength = document.getLineLength(lineNumber);
					setGlobalLine(document.get(
							document.getLineOffset(lineNumber), lineLength));
				} catch (org.eclipse.jface.text.BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			setGlobalLine(testingLine);
			globalOffset = testingOffset;
		}
	}

	public String getGlobalLine() {
		return globalLine;
	}

	public void setGlobalLine(String globalLine) {
		this.globalLine = globalLine;
	}
	
	protected Matcher matchLastToken(final String pattern)
			throws BadLocationException {
		final Pattern LINE_DATA_PATTERN = Pattern.compile(pattern);
		// final String data = getCurrentLine();
		final Matcher matcher = LINE_DATA_PATTERN.matcher(globalLine);
		matcher.matches();
		return matcher;
	}
	
	public String getTypedString() {
		String WITH_SPACES_PATTERN = ".*\\s*([^\\p{Alnum}]?)(\\p{Alnum}*)\\s*$";
		Matcher matcher;
		try {
			matcher = matchLastToken(WITH_SPACES_PATTERN);
			typedString = matcher.group(0).replace("\n", "").replace("\r", "");
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return typedString;
	}

}

