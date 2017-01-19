package com.github.chameleon.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/* TO DO:
 * TODO Refactor code (long methods)
 * TODO Fix performance (don't load files each time they type...)
 * 
 * TODO TEMPLATES:
 * TODO + contributor field in templates (e.g. email address)
 * TODO + remove template core/printXOops.txt (because) X now
 * TODO + update template core/printXOops.txt (just a save that overwrites?)
 * TODO + show template X (e.g. show template core/printXln.txt -- with assistance as you type, listing the options...)
 * TODO + set synonym
 * TODO + remove synonym
 * TODO + list generated (& Static) templates
 * 
 * TODO Get JavaScript & Java back up-to-date with Python version
 * TODO Add tags to templates to describe each field e.g. //\\ Sentence: (This is what the user sees when they start typing...)
 * TODO + dictionary (dynamic) keyword to list all keywords
 * TODO + synonyms (dynamic) keyword to list all synonym keywords and what they refer to
 * TODO + "What did you mean?" default action to add new synonyms to existing templates
 * TODO + "What did you mean?" default action to add new synonyms to new templates
 * TODO + Settings to select which groupings you want (e.g. check Spring, so non-Spring REST commands do NOT show up, unless specifically selected in settings)
 * 
 * TODO Natural Language Additions:
 * TODO + and support
 * TODO + it/the/a new support
 * TODO + ignore words support (Open file, or open a new file, or open a file, etc.)
 * TODO noun, verb support (e.g. File, open vs. open file)
 *
 * TODO Programming Language Support:
 * TODO Ruby -- HP Helion Dev Platform
 * TODO PHP -- HP Helion Dev Platform
 * TODO Node.js -- HP Helion Dev Platform
 * TODO MySQL -- HP Helion Dev Platform
 * TODO RabbitMQ -- HP Helion Dev Platform
 * TODO MemCached -- HP Helion Dev Platform
 * TODO + PowerShell (OneView)
 * TODO + VisualBasic, SQL?, C#, Java, Perl and also scripts within Excel and Word (VB?) (IT/ALM)
 * TODO + ABAP programming language support (for SAP)
 * TODO + Hadoop/Pig/Hive/etc. big data language support...
 * TODO + GO language support
 * TODO + Rust language support
 * TODO + R language support
 * TODO + Ruby (base for Puppet, Vagrant)
 * TODO + PSON: JSON for Puppet
 * TODO + SQL support
 * TODO + GUI to add new templates
 * DONE + C/C++ Support (Workstations; Peter familiar with CDT).
 * DONE + Python Support (Cloud; OpenStack; OneView)
 * DONE Java -- HP Helion Dev Platform
 * DONE Python -- HP Helion Dev Platform
 * 
 * TODO IDE Support:
 * TODO + support for IntelliJ
 * TODO + support for Atom.io editor
 * TODO + support for Visual Studio
 * 
 * TODO Host on GitHub
 * 
 * TODO Fix foreach (1,2,3)
 * TODO Default values
 */

/* DONE:
 * DONE JUnits
 * 2014:
 * Added //\\ (Chaemeleon legs) as new field delimiter instead of newline
 * Set up update site: http://wiki.eclipse.org/FAQ_How_do_I_create_an_update_site_%28site.xml%29%3F
 * 2015:
 * Use chameleon icon
 * Add _template.txt template file
 * Organization--Enable packaging--Team specific templates + Finance + Cloud + web + Programming Language Mapping + Learning Specific (commented) templates, etc...
 * + Dynamic templates e.g. print "Hello World!"
 * Resolved issue of updating and losing your created templates... (save added templates to user's home directory/chameleon)
 * DON'T upload (_)private templates
 * 2015-08-17 MOVE _synonyms.txt to individual template files
 */

/* HOW-TO DEPLOY:
 * 
 * See: http://www.vogella.com/tutorials/EclipsePlugIn/article.html#p2deployplugin
 * 
 * NOTE: If you make a change and re-export, to see changes such
 *       as showing up under a category requires RESTARTING ECLIPSE!
 * 
 * 1. Make sure E: is mapped to \\tntattach.us.rdlabs.hpecorp.net\chameleon\
 * 2. DELETE the contents in E:/test or move it to E:\backups\<date>\
 * 3. Clean all.
 * 4. Export com.github.chameleon.core jar file to each language
 * 5. Export com.github.chameleon.update_site (May take a few minutes...)
 *      Select Plug-in Development | Deployable features
 *        Check features -- Select all 
 *        Destination: Directory: E:/test 
 *        Options:
 *           * UNCHECK - Export Source
 *           * CHECK   - Package as individual JAR archives
 *           * CHECK   - Generate p2 repository
 *           * CHECK   - Categorize repository. Click Browse and select: category - com.github.chameleon.eclipse.update_site
 *           * UNCHECK - Qualifier replacement (default values is today's date)
 * 	         * CHECK   - Allow for binary cycles in target platform
 * 	         * CHECK   - Use class files compiled in the workspace
 *
 * 6. Once it has been verified, copy it to E:/latest and/or E:/stable.
 * 
 */

public abstract class ChameleonCompletionProposalComputer {

	public Boolean testing = false; // set to true by JUnit tests...
	public int testReplacementOffset = 0;
	public String testingLine = "";
	public int testingLineNumber = 0;
	public int testingLineOffset = 0;
	public int testingLineLength = 0;
	public int testingOffset = 0;

	protected String BUNDLE;
	protected Boolean console = true;
	protected final static String SPOKEN_LANGUAGE = "english";
	protected final static String CONDITION = "CONDITION";
	protected final static String STATEMENT = "STATEMENT";
	protected final static String DELIMITER = "//\\\\"; // (//\\) = Delimiter for fields
	protected final static String X = "X";
	protected static String PROGRAMMING_LANGUAGE_SPECIFIC_VERSION = ""; // e.g. "python 2.7", "cpp 17"
	protected static String PROGRAMMING_LANGUAGE_GENERIC_VERSION = ""; // e.g. "Python 2.x", "cpp"
	protected static String PROGRAMMING_LANGUAGE = ""; // e.g. "python", "cdt"
	protected final static String TOOL_TIP_SUFFIX = "  [ESC to dismiss]";
	protected final static String ARROW = "==>";
	final Set<String> selectedLanguages = new HashSet<String>();

	ITextSelection documentTextSelection = null;
	IDocument document = null;
	final static int fLen = 0;
	final static int fAdditionalProposalInfo = 0;
	final static int fContextInformation = 0;
	protected String pluginLanguagesDirectory;
	protected final String userHome = System.getProperty("user.home");
	protected final String homeLanguagesDirectory = System.getProperty("user.home")
			+ "/chameleon/languages/" + SPOKEN_LANGUAGE + "/";

	public ChameleonCompletionProposalComputer(String replacementString,
			int replacementOffset, int replacementLength, int cursorPosition,
			int priority) {
	}

	public void setEditor(Object editor) {
	}

	public void setLanguage(String language) {
		PROGRAMMING_LANGUAGE = language;
	}

	public void setPluginLanguagesDirectory() {
		if (!testing) {
			pluginLanguagesDirectory = locateFile(BUNDLE,
					"languages/" + SPOKEN_LANGUAGE + "/").getPath();
		} else {
			pluginLanguagesDirectory = "languages/english/";
		}
	}

	public void setTesting(boolean testing) {
		this.testing = testing;
		if (testing) {
			document = new Document();
		}
	}

	public boolean getTesting() {
		return this.testing;
	}

	public ChameleonCompletionProposalComputer() {
		System.out.println("ChameleonCompletionProposalComputer()");
		selectedLanguages.add(SPOKEN_LANGUAGE);
	}

	// http://www.codejava.net/java-se/networking/ftp/java-ftp-file-upload-tutorial-and-example
	protected void ftpTemplateToServer(String fullPath, String relativePath) {
		String server = "tntattach.us.rdlabs.hpecorp.net";
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

	private String addCommandsFromDirectory(final String templatesDirectory,
			final String language,
			String typedString,
			final Map<String, Object> proposals)
			throws BadLocationException {
		final File folder = new File(templatesDirectory);
		final File[] listOfFiles = folder.listFiles();
		String replacementString = "";
		String returnReplacementString = "";
		if (listOfFiles != null) {
			for (final File file : listOfFiles) {
				if (file.isFile()) {
					final String fileName = file.getName();
					if (selectedLanguages.contains(SPOKEN_LANGUAGE)
							&& !fileName.equals("_synonyms_list.txt")) {
						try {
							replacementString = addCommandFromFile(language, null, templatesDirectory,
									fileName,
									typedString,
									proposals);
							if ( returnReplacementString == "" ) {
								returnReplacementString = replacementString;
							}
						} catch (final BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else { // directory...
					replacementString = addCommandsFromDirectory(file.getPath() + Path.SEPARATOR,
							file.getName(), typedString, proposals);
					if ( returnReplacementString == "" ) {
						returnReplacementString = replacementString;
					}
				}
			}
		}
//		addCommandsFromSynonymsFile();
		return returnReplacementString;
	}

	protected String addCommandFromFile(final String language, String in,
			final String commandDirectory, final String fileName,
			String typedString,
			final Map<String, Object> proposals)
			throws BadLocationException {
		String replacementString = "";
		String returnReplacementString = "";
		// Open command file
		BufferedReader br = null;
		String line = "";
		try {
			final int FIELD_COUNT = 7;
			br = new BufferedReader(new FileReader(new File(commandDirectory, fileName).toString()));
			final String[] map = { "", "", "", "", "", "", "" };
			int i = 0;
			while (i < FIELD_COUNT && (line = br.readLine()) != null) {
				if (line.startsWith(DELIMITER)) {
					i++;
				} else {
					if (!map[i].isEmpty()) {
						map[i] += "\n";
					}
					//Some fields should only be one line long...
					if (map[i]=="" || (i!=2 && i!=6) ) {
						map[i] += line;
					}
				}
			}
			if (in == null) {
				in = map[1];
			}
			// add directory...
			File file = new File(commandDirectory);
			String dir = file.getName();
			String dirs = "";
			String path = file.getParent();
			File pathFile = new File(path);
			while (path != null	
					&& !dir.equals(SPOKEN_LANGUAGE)
					&& !dir.equals(STATEMENT)
					&& !dir.equals(CONDITION)
					) {
				if (dirs != "") {
					dirs = dir + "/" + dirs;
				} else {
					dirs = dir;
				}
				dir = pathFile.getName();
				pathFile = new File(pathFile.getParent());
			}
			if (dir != null) {
				String atHome = "";
				if (path.startsWith(userHome)) {
					if (dirs.startsWith("/")) {
						atHome = "~";
					} else {
						atHome = "~/";
					}
				}
				map[2] = map[2] + "  ////" + atHome + dirs + "/" + fileName;
			}
			String separator = "\n"; // default to Unix
			if (in.indexOf(System.getProperty("line.separator")) > 0) {
				separator = System.getProperty("line.separator");
			} else {
				if (in.indexOf("\r\n") > 0) {
					separator = "\r\n"; // Windows
				} else {
					if (in.indexOf("\n") > 0) {
						separator = "\n"; // Unix
					} else {
						if (in.indexOf("\r") > 0) {
							separator = "\r"; // Mac
						}
					}
				}
			}
			if (map[1].length() > 0) {
				String[] lines = in.split(separator);
				if (map[3].endsWith(separator)) {
					map[3] = map[3].substring(0,
							map[3].length() - separator.length());
				}
				for (String entry : lines) {
					if ( map.length >= 7 ) {
						String defaults = map[4];
						String afterArrow = map[2]; 
						String replacement = map[3];
						String help = map[5];
						String toolTip = map[6];
						replacementString = addUpperAndLowercaseEntries(language, entry, afterArrow,
								replacement, help, toolTip, defaults, typedString, proposals);
						if ( returnReplacementString == "" ) {
							returnReplacementString = replacementString;
						}
					} else {
						System.out.println("Warning: Invalid template (not enough fields/lines): "
								+ commandDirectory + fileName);
					}
				}
			} else {
				System.out.println("Warning: Empty template: "
						+ commandDirectory + fileName);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return returnReplacementString;
	}

	abstract public DocumentInfo getDocumentInfo();
	
	protected String addUpperAndLowercaseEntries(final String language,
			final String from, final String to, final String replacementString,
			final String help, final String toolTip, String defaults, String typedString, 
			final Map<String, Object> proposals)
			throws IOException, BadLocationException {
		String tempReplacementString = "";
		String returnReplacementString = "";
		if (from.length() > 0) {
			String source = Character.toLowerCase(from.charAt(0))
					+ from.substring(1);
//			DocumentInfo docInfo = getDocumentInfo();
//			String typedString = docInfo.getTypedString();
//			String typedString = "";
			tempReplacementString = addEntry(language, source + " " + ARROW + " " + to, typedString,
					replacementString, help, toolTip,
					defaults, proposals);
			if ( returnReplacementString == "" ) {
				returnReplacementString = tempReplacementString;
			}
			// Same thing, with first letter Capitalized...
			source = Character.toUpperCase(from.charAt(0))
					+ from.substring(1);
			tempReplacementString = addEntry(language, source + " " + ARROW + " " + to, typedString,
					replacementString, help, toolTip,
					defaults, proposals);
			if ( returnReplacementString == "" ) {
				returnReplacementString = tempReplacementString;
			}
		} else {
			System.out.println("Warning: Empty template file.");
		}
		return returnReplacementString;
	}

	// fullPath is full path to file needing parent directories
	public void createDirectories(String fullPath) {
		File destinationFile = new File(fullPath);
		File dir = new File(destinationFile.getParent());
		if ((dir != null) && !dir.exists()) {
			if (!dir.mkdir()) {
				createDirectories(dir.getPath());
				dir.mkdir();
			}
		}
	}

	public String replaceQuestionMarksWithEmpty(String in, String delimeter) {
		if (in.indexOf(")?") > 0) {
			in = in.replaceAll("\\(", "(" + delimeter);
			in = in.replaceAll("\\)\\?", ")");
		}
		return in;
	}

	public String replaceStarWithEmpty(String in, String delimeter) {
		if (in.indexOf(")*") > 0) {
			in = in.replaceAll("\\(", "(" + delimeter);
			in = in.replaceAll("\\)\\*", ")");
		}
		return in;
	}

	public String addEntry(final String language, 
			String displayString, String typedString, String replacementString,
			String help, final String toolTip, String defaults,
			final Map<String, Object> proposals)
			throws IOException, BadLocationException {
		String tempReplacementString = "";
		String returnReplacementString = "";

		// (a)* --> a or aa or aaa...; vs... X(a,b,c)Z
		// (a)* --> (a)? --> (,a)

		displayString = replaceStarWithEmpty(displayString, ",");
		replacementString = replaceStarWithEmpty(replacementString, "//////");

		displayString = replaceQuestionMarksWithEmpty(displayString, ",");

		replacementString = replaceQuestionMarksWithEmpty(replacementString,
				"//////");
		StringWithSections displayStringWithSections = new StringWithSections(
				displayString, ",");
		StringWithSections replacementStringWithSections = new StringWithSections(
				replacementString, "//////");
		List<String> expandedDisplayStrings = displayStringWithSections.getExpandedStrings();
		List<String> expandedReplacementStrings = replacementStringWithSections.getExpandedStrings();

		int i=0;
		for (String expandedDisplayString : expandedDisplayStrings) {
			String expandedReplacementString = (
					i>=expandedReplacementStrings.size() ?
					expandedReplacementStrings.get(0) : 
					expandedReplacementStrings.get(i));
//			String finalHelp = expandedDisplayString + "\n\n" + help;
			String finalToolTip = "";
			if (!toolTip.trim().equals("")) {
				finalToolTip = toolTip + TOOL_TIP_SUFFIX;
			}
			tempReplacementString = addExpandedEntry(language,
					expandedDisplayString,
					typedString,
					expandedReplacementString,
					//finalHelp,
					help,
					finalToolTip, defaults, proposals);
			if ( returnReplacementString == "" ) {
				returnReplacementString = tempReplacementString;
			}
			i++;
		}
		if (displayStringWithSections.getSize() == 0) {
			tempReplacementString = addExpandedEntry(language, displayString, typedString, replacementString,
					help, toolTip, defaults, proposals);
			if ( returnReplacementString == "" ) {
				returnReplacementString = tempReplacementString;
			}
		}
		return returnReplacementString;
	}

	abstract protected String addExpandedEntry(
		final String language, String displayString,
		String typedString,
		String replacementString, String additionalProposalInfo,
		final String message,
		final String defaults,
//        final Collection<ICompletionProposal> proposals)
		final Map<String, Object> proposals)
		throws IOException, BadLocationException;
	

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

//	private String addCommandsFromSynonymsFile(String templatesDirectory, Collection<ICompletionProposal> typedString, String proposals, String replacementString, String language) {
//		String returnReplacementString = "";
//		final String synonymFile = templatesDirectory + "/_synonyms.txt";
//		BufferedReader br = null;
//		String line = "";
//		final String cvsSplitBy = ",";
//		try {
//			br = new BufferedReader(new FileReader(synonymFile));
//			while ((line = br.readLine()) != null) {
//				final String[] map = line.split(cvsSplitBy);
//				final String in = map[1];
//				replacementString = addCommandFromFile(language, in, templatesDirectory, map[2],
//						typedString, proposals);
//				if ( returnReplacementString  == "" ) {
//					returnReplacementString = replacementString;
//				}
//			}
//		} catch (final FileNotFoundException e) {
//			// e.printStackTrace();
//			// _synonym.txt file not required for every directory...
//		} catch (final IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (br != null) {
//				try {
//					br.close();
//				} catch (final IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return returnReplacementString;
//	}

	protected String getCompletionsFromDirectory(String languagesDirectory,
                                                 String typedString,
                                                 Map<String, Object> completions) {
		String replacementString = "";
		String returnReplacementString = "";
		final File folder = new File(languagesDirectory);
		final File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (final File file : listOfFiles) {
				if (file.isDirectory() && 
						(file.getName().equals(PROGRAMMING_LANGUAGE) ||
						 file.getName().equals(PROGRAMMING_LANGUAGE_GENERIC_VERSION))) {
					try {
						replacementString = addCommandsFromDirectory(file.getPath(),
								file.getName(), typedString, completions);
						if ( returnReplacementString == "" ) {
							returnReplacementString = replacementString;
						}
					} catch (final BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("WARNING: Null listOfFiles.");
		}
/*
		try {
			addInternalCommands(typedString, completions);
		 } catch (BadLocationException e) {
			 e.printStackTrace();
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
*/
		return returnReplacementString;
	}
	
	protected void addInternalCommands(
			String typedString,
			final Map<String, Object> proposals)
			throws BadLocationException, IOException {

		addUpperAndLowercaseEntries(
				SPOKEN_LANGUAGE,
				"save template now",
				"!!Must type up to the 'n' in 'now'!",
				"TODO: Test template then erase text above.  Make sure you TYPED 'save template n' up to the 'n' in 'now'...",
				"When you type the 'n' in 'now' it will save, then press enter to remove the line you typed.",
				"Test template then erase text above.  Make sure you TYPED 'save template n' up to the 'n' in 'now'...",
				"",
				typedString,
				proposals);

		addUpperAndLowercaseEntries(
				SPOKEN_LANGUAGE,
				"delete template X right now",
				"!!Must type up to the 'n' in 'now'!",
				"TODO: Make sure template is gone then erase text above.  Make sure you TYPED 'delete template n' up to the 'n' in 'now'...",
				"When you type the 'n' in 'now' it will delete, then press enter to remove the line you typed.",
				"Check to make sure template is removed then erase text above.  Make sure you TYPED 'delete template n' up to the 'n' in 'now'...",
				"",
				typedString,
				proposals);

//		try {
////			getDocumentInfo();
//		} catch (CoreException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		// Grab line(s) from above
//		int lineLength;
//		int globalOffset = documentTextSelection.getOffset();
//		int lineNumber = document.getLineOfOffset(globalOffset);
//		String line = "";
//		String lastLine = "";
//		String lines = "";
//		do {
//			try {
//				lastLine = line;
//				lineNumber--;
//				lineLength = document.getLineLength(lineNumber);
//				line = document.get(document.getLineOffset(lineNumber),
//						lineLength);
//		//		globalOffset = document.getLineOffset(lineNumber);
//				if (line.length() > 1) {
//					lines = line + lines;
//				}
//			} catch (BadLocationException e) {
//				e.printStackTrace();
//			}
//		} while (line.length() > 1 && lineNumber > 0);
//		System.out.println(">> line: " + line);

//		String replacementText = " //\\\\ //// code completion entries (1+ lines):\n"
//				+ "\n"
//				+ " //\\\\ //// ==> completion hint (1 line):\n"
//				+ lastLine
//				+ "\n"
//				+ " //\\\\ //// Replacement text (1+ lines):\n"
//				+ lines
//				+ "\n"
//				+ " //\\\\ //// Help/documentation (1+ lines):\n"
//				+ "\n"
//				+ " //\\\\ //// Tool tip (1 line):\n"
//				+ "\n"
//				+ " //\\\\ //// Template path (e.g. core/sayHi.txt) (1 line):";

//		addUpperAndLowercaseEntries(
//				SPOKEN_LANGUAGE,
//				"generate template",
//				"Generate a new template based on the lines of text that you have provided above, up to the next blank line.",
//				replacementText,
//				"Generate a new template based on the lines of text that you have provided above, up to the next blank line.",
//				"Modify template as needed, then just below the template type save template now...",
//				proposals);
//		System.out.print("*");

	}

    // Eclipse
    protected Collection<ICompletionProposal> getAllCompletions() {
        setPluginLanguagesDirectory();
        Map<String, Object> completions = new HashMap<String, Object>();

        getCompletionsFromDirectory(homeLanguagesDirectory, "", completions);

        getCompletionsFromDirectory(pluginLanguagesDirectory, "", completions);

        System.out.println("getAllCompletions()");

        ArrayList<ICompletionProposal> returnCompletions = new ArrayList<>(completions.size());
        for (Object object : completions.entrySet()) {
            returnCompletions.add(object != null ? (ICompletionProposal)object : null);
        }

        return returnCompletions;
    }

    // IntelliJ
    public Map<String, Object> getAllCompletionsAsMap() {
        Map<String, Object> completions = new HashMap<String, Object>();

        getCompletionsFromDirectory(homeLanguagesDirectory, "", completions);

        try {
            setPluginLanguagesDirectory();
            getCompletionsFromDirectory(pluginLanguagesDirectory, "", completions);
        } catch(NoClassDefFoundError e) {
            System.out.println("WARNING: Unable to load templates from Chameleon jar file.");
        } catch(ExceptionInInitializerError e) {
            System.out.println("WARNING: Unable to load templates from Chameleon jar file.");
        }

        System.out.println("getAllCompletions()");
        return completions;
    }

//    public Collection<Object> getAllCompletionsAsObjects() {
//        Map<String, Object> completions = getAllCompletionsAsMap();
//
//        Collection<ICompletionProposal> returnCompletions = new ArrayList<ICompletionProposal>(completions.size());
//
//        for (Object object : completions.entrySet()) {
//            returnCompletions.add(object != null ? (ICompletionProposal)object : null);
//        }
//        return returnCompletions;
//    }


    public Collection<ICompletionProposal> getAllCompletionsAsObjects() {
        Map<String, Object> completions = getAllCompletionsAsMap();

        Collection<ICompletionProposal> returnCompletions = new ArrayList<ICompletionProposal>(completions.size());

        for (Object object : completions.entrySet()) {
            returnCompletions.add(object != null ? (ICompletionProposal)object : null);
        }
        return returnCompletions;
    }


    public List<ICompletionProposal> getAllCompletionsList() {
        Collection<ICompletionProposal> completions = getAllCompletionsAsObjects();

        ArrayList<ICompletionProposal> returnCompletions = new ArrayList<>(completions.size());
        for (Object object : completions) {
            returnCompletions.add(object != null ? (ICompletionProposal)object : null);
        }
		return returnCompletions;
	}	
}
