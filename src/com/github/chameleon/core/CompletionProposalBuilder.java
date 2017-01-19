package com.github.chameleon.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class CompletionProposalBuilder {

	final boolean settingsCommentsOn = false;

	final static int DELIMITERS_COUNT = 5; // 5 //\\ sets original a template file
	final static String LANGUAGE = "english";
	final static String DELIMITER = "//\\\\"; // (//\\) = Delimiter for fields
	final static String X = "X";
	final static String ARROW = "==>";
	final protected static String TARGET_LANGUAGE = null;

	protected StringWithReplacements displayString;
	protected String help;
	protected int replacementOffset;
	protected String additionalProposalInfo;
	protected boolean testing;
	protected int cursorPosition;
	public int replacementLength;
	protected int priority;
	protected int onApplyAction;
	protected String paddedReplacementString;
	protected String remainingString;
	protected String args;
	protected ContextInformation contextInformation;
	protected Boolean console = true;
	protected ITextSelection documentTextSelection;
	public String testingLine;
	protected int testingOffset;
	protected int testReplacementOffset = 0;
	private int testingLineNumber;
	private int testingLineOffset;
	protected int indexOfDisplayX2;
	protected String language;
	protected Map<String, Object> proposals;
	protected int lineNumber;
	protected int lineLength;
	protected int lineOffset;
	protected DocumentInfo documentInfo;
	protected Synonyms synonyms;
	protected String typedString;

	public CompletionProposalBuilder(final String language,
                                     String displayString, String replacementString,
                                     String additionalProposalInfo, final String help,
                                     final String defaults,
                                     final Map<String, Object> proposals, boolean testing,
                                     String testingLine, int testingOffset, boolean console, DocumentInfo documentInfo,
                                     String typedString) {
		this.language = language;
		this.additionalProposalInfo = additionalProposalInfo;
		this.help = help;
		this.proposals = proposals;
		this.testing = testing;
		this.testingLine = testingLine;
		this.testingOffset = testingOffset;
		this.documentInfo = documentInfo;
		this.typedString = typedString;		
		this.displayString = new StringWithReplacements((typedString == "" ? documentInfo.getTypedString() : typedString), displayString, replacementString, defaults, X);
		this.synonyms = new Synonyms();
		this.console = console;
	}

	protected static URI locateFile(final String bundle, final String fullPath) {
		try {
			final URL url = FileLocator.find(Platform.getBundle(bundle),
					new Path(fullPath), null);
			if (url != null) {
				return FileLocator.resolve(url).toURI();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Display getDisplay() {
		Display display = Display.getCurrent();
		// may be null if outside the UI thread
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	abstract protected String getCommentStarter();
	
	protected void preCreateProposal() {
		cursorPosition = 0;
		replacementLength = documentInfo.getTypedString().length();
		if (displayString.indexOfDisplayX >= 0 && displayString.indexOfDisplayX < documentInfo.getTypedString().length())
		{
			cursorPosition = displayString.getReplacementString().length()
					+ documentInfo.getTypedString().substring(displayString.indexOfDisplayX).length();
		}
		contextInformation = new ContextInformation("displayStr", " " + help + " ");
		args = "";
		remainingString = displayString.getRemainingTemplate().trim();

		// pyDevPadding makes replacement length > original text
		// otherwise pyDev throws an exception
//		String pyDevPadding = new String(new char[documentInfo.typedString.length()]).replace('\0', ' ');
		// Make space for the tool tip...
		String toolTipPadding =  "";	// parameter substitution mode...
//		String toolTipPadding =  "\n"
//				+ displayString.getBothIndents();
		if (!help.trim().isEmpty()) {
			toolTipPadding = "\n"
			+ displayString.getBothIndents() + "\n"
			+ displayString.getBothIndents();
		}
		String comment = "";
//		if (settingsCommentsOn && !testing) {
//			comment = displayString.getOriginalIndent() + getCommentStarter() 
//					+ displayString.getExpandedTemplateString();
//		}
		comment = comment.replace("\n", "");
		comment = comment.replace("\r", "");		
		paddedReplacementString = // comment + pyDevPadding + "\n" + 
				displayString.getExpandedReplacementString() +
				toolTipPadding;
		priority = 0;
		onApplyAction = 1; //3;//ON_APPLY_SHOW_CTX_INFO_AND_ADD_PARAMETETRS;//1;
	}

	/**
	 * Get the current document information including: * Document *
	 * DocumentTextSelection
	 * 
	 * @throws CoreException
	 */
	//protected abstract void getDocumentInfo() throws CoreException;

	protected int getOurReplacementOffset() {
		if (console) {
//			try {
////				getDocumentInfo();
//			} catch (CoreException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			replacementOffset = documentInfo.globalOffset;
		} else {
			if (!testing) {
				IEditorPart editor = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				if (editor instanceof ITextEditor) {
					ISelectionProvider selectionProvider = ((ITextEditor) editor)
							.getSelectionProvider();
					ISelection selection = selectionProvider.getSelection();
					if (selection instanceof ITextSelection) {
						ITextSelection textSelection = (ITextSelection) selection;
						replacementOffset = textSelection.getOffset(); // etc.
					}
				}
				if (replacementOffset < 0) {
					replacementOffset = 0;
				}
			} else {
				return testReplacementOffset;
			}
		}
		return replacementOffset;
	}

	/**
	 * Extract context relevant information from current line. The returned
	 * matcher locates the last alphanumeric word original the line and an optional
	 * non alphanumeric character right before that word. result.group(1)
	 * contains the last non-alphanumeric token (eg a dot, brackets, arithmetic
	 * operators, ...), result.group(2) contains the alphanumeric text. This
	 * text can be used to filter content assist proposals.
	 * 
	 * @param context
	 *            content assist context
	 * @return matcher containing content assist information
	 * @throws BadLocationException
	 */
	protected Matcher matchLastToken(final String pattern)
			throws BadLocationException {
		final Pattern LINE_DATA_PATTERN = Pattern.compile(pattern);
		// final String data = getCurrentLine();
		final Matcher matcher = LINE_DATA_PATTERN.matcher(typedString);
		matcher.matches();
		return matcher;
	}

	protected int getDupSize(String displayStringAfterX, String typedString,
			String xReplacement) {
		// typedString =ask "Who? " to name variable
		// displayString=ask X to X variable
		// remove duplication:
		// ask "What" to ... to the user -->
		// ask "What" to the user
		int dupSize = 0;
		int length = displayStringAfterX.length();
		for (int i = 1; i < length; i++) {
			// if we find part of the display original is typed...
			if (documentInfo.typedString.length() - i > 0) {
				System.out.println("documentInfo.typedString.length()-i="
						+ (documentInfo.typedString.length() - i));
				System.out.println("bit="
						+ documentInfo.typedString.substring(documentInfo.typedString.length() - i,
								documentInfo.typedString.length()));
				if (displayStringAfterX.startsWith(documentInfo.typedString.substring(
						documentInfo.typedString.length() - i, documentInfo.typedString.length()))) {
					// then we need to cut that piece off...
					dupSize = i;
					System.out.println("dupSize=" + dupSize);
					if (xReplacement.length() > 1) {
						xReplacement = xReplacement.substring(0,
								xReplacement.length() - dupSize);
					}
				}
			}
		}
		return dupSize;
	}

	// fullPath is full path to file needing parent directories
	protected void createDirectories(String fullPath) {
		File destinationFile = new File(fullPath);
		File dir = new File(destinationFile.getParent());
		if ((dir != null) && !dir.exists()) {
			if (!dir.mkdir()) {
				createDirectories(dir.getPath());
				dir.mkdir();
			}
		}
	}

	// http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
	protected void ftpTemplateToServer(String fullPath, String relativePath) {
		String server = "tntattach.usa.hp.com";
		int port = 21;
		String user = "anonymous";
		String pass = "";

		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
			ftpClient.enterLocalPassiveMode();

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			File localFile = new File(fullPath);
			File relativeFile = new File(relativePath);

			InputStream inputStream = new FileInputStream(localFile);

			// Create directory hierarchy if needed...
			String path = relativeFile.getParent();
			if (path != null) {
				ftpClient.makeDirectory("chameleon/uploads/" + path);
			}

			// Copy file...
			System.out.println("Start uploading first file");
			boolean done = ftpClient.storeFile("chameleon/uploads/"
					+ relativePath, inputStream);
			inputStream.close();
			if (done) {
				System.out.println("The first file is uploaded successfully.");
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	protected void handleInternalTemplates(int lineNumber,
			IDocument document) throws BadLocationException,
			FileNotFoundException, org.eclipse.jface.text.BadLocationException {
		// String line = "";
		if (documentInfo.typedString.equals("save template n")) {

			System.out.println("**SAVE TEMPLATE**");
			// Grab template from above

			int delimiters = DELIMITERS_COUNT;
			int lineLength = document.getLineLength(lineNumber - 1);
			String relativePath = document.get(
					document.getLineOffset(lineNumber - 1), lineLength - 1);
			String fileContent = "";
			int cursor = lineNumber - 3;
			String line;
			while (cursor >= 0 && delimiters > 0) {
				lineLength = document.getLineLength(cursor);
				line = document.get(document.getLineOffset(cursor),
						lineLength);
				if (line.startsWith(" " + DELIMITER)) {
					delimiters--;
					line = line.substring(1);
				}
				fileContent = line + fileContent;
				cursor--;
			}
			System.out.println(">> relativePath=" + relativePath);
			System.out.println(">> fileContent=[" + fileContent + "]");

			// Home directory as root:
			String destination = System.getProperty("user.home")
					+ "/chameleon/languages/2" + TARGET_LANGUAGE + "/"
					+ LANGUAGE + "2" + TARGET_LANGUAGE + "/" + relativePath;

			// Create parent directories if needed...
			createDirectories(destination);

			PrintWriter out = new PrintWriter(destination);
			out.println(fileContent);
			out.close();

			String source = destination;
			if (!relativePath.startsWith("_private")
					&& !relativePath.startsWith("private")) {
				ftpTemplateToServer(source, relativePath);
			}
		} else if (!documentInfo.typedString.equals("delete template n")
				&& documentInfo.typedString.startsWith("delete template ")
				&& documentInfo.typedString.endsWith(" right n")) {

			System.out.println("**DELETE TEMPLATE**");
			// Grab template from documentInfo.typedString...
			String templateName = documentInfo.typedString.substring("delete template "
					.length());
			templateName = templateName.substring(0, templateName.length()
					- " right n".length());

			String destination = System.getProperty("user.home")
					+ "/chameleon/languages/2" + TARGET_LANGUAGE + "/"
					+ LANGUAGE + "2" + TARGET_LANGUAGE + "/";
			File file = new File(destination + templateName);

			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}
			System.out.println(">> templateName=" + templateName);
		}
	}

	protected int getReplacementOffset() {
		lineNumber = 0;
		lineLength = 0;
		lineOffset = 0;
		int replacementOffset = 0;
		try {
//			getDocumentInfo();
			if (!testing) {
				if (console) {
					replacementOffset = documentInfo.globalOffset;
				} else {
					lineNumber = documentInfo.document.getLineOfOffset(documentInfo.globalOffset);
					lineLength = documentInfo.document.getLineLength(lineNumber);
					lineOffset = documentInfo.document.getLineOffset(lineNumber);
					replacementOffset = lineOffset;
				}
			} else {
				lineNumber = testingLineNumber;
				lineOffset = testingLineOffset;
				documentInfo.globalLine = testingLine;
				typedString = testingLine;
			}
			System.out.println(">> typedString=" + documentInfo.typedString);
			//handleInternalTemplates(lineNumber, documentInfo.document);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return replacementOffset;
	}
	
	public boolean isMatch() {
		return displayString.isMatch();
	}		
}
