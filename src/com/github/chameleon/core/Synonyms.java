package com.github.chameleon.core;


public class Synonyms {
	String filename;
	final static String SPOKEN_LANGUAGE = "english";
	final String homeLanguagesDirectory = System.getProperty("user.home")
			+ "/chameleon/languages/" + SPOKEN_LANGUAGE + "/";
	String commandsDirectory = homeLanguagesDirectory;
	public Synonyms() {
		// Open _synonyms.txt and read it...
		
		readFile();
	}
	
	private void readFile() {
//		final File folder = new File(commandsDirectory);
//		final File[] listOfFiles = folder.listFiles();
//
//		final String synonymFile = commandsDirectory + "/_synonyms.txt";
//		BufferedReader br = null;
//		String line = "";
//		final String cvsSplitBy = ",";
//		try {
//			br = new BufferedReader(new FileReader(synonymFile));
//			while ((line = br.readLine()) != null) {
//				final String[] map = line.split(cvsSplitBy);
//				final String original = map[1];
//				addCommandFromFile(language, original, commandsDirectory, map[2],
//						proposals);
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
//		
	}
}
