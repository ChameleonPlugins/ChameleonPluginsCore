package com.github.chameleon.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Strings with replacements, such as "If X then X else X" where X is to
 * be replaced with other text.
 * 
 * @author Jesse Olsen
 *
 */
public class StringWithReplacements {

	String X = "X";
//	char REPLACEMENT_TERMINATOR = ';';
	char REPLACEMENT_TERMINATOR = ' ';	// No replacement terminator

	private String templateString = "";
	
	private String typedString;

	private String replacementString;
	public String expandedReplacementString;

	String defaultsString;
	List<String> defaults = new ArrayList<String>();
	
	int replacementIndex = 1;
	
	List<String> delimiters = new ArrayList<String>();
	List<Replacement> replacements = new ArrayList<Replacement>();
	String beginString;
	String endString;
	int beginIndex = -1;
	int endIndex = -1;
	private int indexOfDisplayX2 = -1;
	private int indexOfDelimeter;
	protected int indexOfX = -1;
	public int indexOfDisplayX = 0;

	private int indexAfterDisplayX;
	boolean match = false;
	private String originalIndent = "";
	private String replacementIndent = "";
	
	public String getReplacementIndent() {
		return replacementIndent;
	}

	public String getOriginalIndent() {
		return originalIndent;
	}


	public String getBothIndents() {
		return originalIndent + replacementIndent;
	}
	
	public StringWithReplacements(String typedString, String templateString, String replacementString, String defaultsString, String X) {
		this.typedString = typedString;
		Pattern p = Pattern.compile("(\\:[^ "+REPLACEMENT_TERMINATOR+"|$]*) ");
		this.templateString = templateString;
		Matcher m = p.matcher(this.templateString);
		if ( REPLACEMENT_TERMINATOR != ' ') {
			try {
				while (m.find()) {
					this.templateString = m.replaceFirst(m.group().trim()
							+ REPLACEMENT_TERMINATOR + " ");
					m = p.matcher(this.templateString);
				}
				Pattern endPattern = Pattern.compile("(\\:[^ "+REPLACEMENT_TERMINATOR+"]*$)");
				m = endPattern.matcher(this.templateString);
				if (m.find()) {
					this.templateString = m.replaceFirst(m.group().trim()
							+ REPLACEMENT_TERMINATOR);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			this.templateString = this.templateString.replace(X+" ", 
					X + REPLACEMENT_TERMINATOR + " ");
			if (this.templateString.endsWith(X)) {
				this.templateString = this.templateString + REPLACEMENT_TERMINATOR;
			}
		}
		populateOriginalIndent(this.typedString);
		this.typedString = this.typedString.replaceAll("^\\s*",  "");
		populateReplacementIndent(replacementString);
		this.replacementString = replacementString;
		this.defaultsString = defaultsString;
		this.X = X;
		populateDefaults();
		populateDelimitersAndReplacements();
		String expandedTemplateString = getExpandedTemplateString();
		match = expandedTemplateString.startsWith(this.typedString);
		if (match) 
		{
			if( REPLACEMENT_TERMINATOR != ' '
					&& expandedTemplateString.length() > this.typedString.length() 
					&& this.typedString.charAt(this.typedString.length()-1) != REPLACEMENT_TERMINATOR
					&& expandedTemplateString.charAt(this.typedString.length())
					== REPLACEMENT_TERMINATOR) {
				match = false;
			} else {
				expandedReplacementString = getExpandedReplacementString();
			}
		}
	}

	public String getReplacementString() {
		return replacementString;
	}

	public void setReplacementString(String replacementString) {
		this.replacementString = replacementString;
	}

	public String getTypedString() {
		return typedString;
	}

	public void setTypedString(String typedString) {
		this.typedString = typedString;
	}

	private String computeIndent(String originalString) {
		String indent = "";
		for (int i = 0; i < originalString.length(); i++){
		    char c = originalString.charAt(i);        
		    if ( c == ' ' || c == '\t' ) {
		    	indent += c;
		    } else {
		    	break;
		    }
		}
		return indent;
	}

	// indent in original typed string
	private void populateOriginalIndent(String originalTypedString) {
		originalIndent = computeIndent(originalTypedString);
	}

	// indent in last line of replacement string, without the original indent
	private void populateReplacementIndent(String replacementString) {
		String lastLine = "";
		BufferedReader bufReader = new BufferedReader(new StringReader(replacementString));
		String line=null;
		try {
			while( (line=bufReader.readLine()) != null )
			{
				lastLine = line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		replacementIndent = computeIndent(lastLine);
	}

	private void populateDelimitersAndReplacements() {
		int delimiterStartIndex = 0;
		int replacementStartIndex = templateString.indexOf(X);
		while (replacementStartIndex >= 0 && delimiterStartIndex < templateString.length()) {
			delimiters.add(templateString.substring(delimiterStartIndex, replacementStartIndex));
			int originalEndIndex = templateString.indexOf(
					REPLACEMENT_TERMINATOR, replacementStartIndex);
			if (originalEndIndex < 0) {
				originalEndIndex = templateString.indexOf(
					" ", replacementStartIndex);
			}
			if (originalEndIndex < 0) {
				originalEndIndex = templateString.length();
			}
			if (replacementStartIndex != templateString.length()) {
				String originalString = templateString.substring(replacementStartIndex, originalEndIndex);
				replacements.add(new Replacement(originalString, 
						replacementStartIndex, originalEndIndex, X));
	
				delimiterStartIndex = originalEndIndex;
				replacementStartIndex = templateString.indexOf(X, delimiterStartIndex);			
				if (replacementStartIndex == -1) {
					replacementStartIndex = templateString.length();
				}
			} else {
				replacementStartIndex = -1;
			}
		}
		int replacementNumber = 0;
		int indexBeginReplacement = 0;
		int indexEndReplacement = -1;
		if(replacements != null) {
			for (Replacement replacement : replacements) {
				if ( defaults.size() > replacementNumber) {
					replacement.setReplacementString(defaults.get(replacementNumber));
				}				
				indexBeginReplacement = typedString.indexOf(delimiters.get(replacementNumber), indexBeginReplacement);
				if (indexBeginReplacement >= 0) {
					indexBeginReplacement += delimiters.get(replacementNumber).length();
					if ( delimiters.size() > replacementNumber) {
						if (delimiters.size() > replacementNumber+1) {
							String delimiter = delimiters.get(replacementNumber+1);
						  	int length = typedString.length();
							indexEndReplacement = typedString.indexOf(delimiter, indexBeginReplacement);
							if (indexEndReplacement < 0) {
								for (int i = 1; i < length; i++) {
									String segment = typedString.substring(
											typedString.length() - i, typedString.length());
									if (segment.contains(" ") && delimiter.startsWith(segment)) {
										indexEndReplacement = typedString.length() - i;
									}
								}
							}
						} else {
							indexEndReplacement = typedString.length();
						}
						if ( indexEndReplacement <= 0 || (indexEndReplacement < indexBeginReplacement) ) {
							indexEndReplacement = typedString.length();
						}
						if (indexEndReplacement == typedString.length() 
							&& typedString.endsWith(""+REPLACEMENT_TERMINATOR) ) {
							indexEndReplacement--;
						}
						String candidate = typedString;
						if ( indexBeginReplacement >= 0 && indexEndReplacement > indexBeginReplacement ) {
							candidate = typedString.substring(indexBeginReplacement, indexEndReplacement).trim();
							if ( REPLACEMENT_TERMINATOR != ' ') {
								int terminatorIndex = candidate.indexOf(REPLACEMENT_TERMINATOR);
								while (terminatorIndex >= 0) {
									char characterBefore = ' ';
									if (terminatorIndex > 0) {
										characterBefore = candidate.charAt(terminatorIndex - 1);
									}
									if (characterBefore != ';' && characterBefore != '\\') {
										candidate = typedString.substring(indexBeginReplacement, indexBeginReplacement + terminatorIndex);
									} else {
										candidate.replaceFirst(
											""+characterBefore+REPLACEMENT_TERMINATOR, 
											""+REPLACEMENT_TERMINATOR);
									}
									terminatorIndex = candidate.indexOf(REPLACEMENT_TERMINATOR, terminatorIndex);
								}
							}
						}
						replacement.setReplacementString(candidate);
					}
				}
				replacementNumber++;
			}
		}
	}
	
	public String getExpandedTemplateString() {
		String expandedString = templateString.replaceAll(
				"\\:[^ " + REPLACEMENT_TERMINATOR + "]*", "");
		for ( Replacement replacement : replacements) {
			if (replacement.getReplacementString() != null ) {
				expandedString = expandedString.replaceFirst(X, Matcher.quoteReplacement(replacement.getReplacementString()));
			}
		}
		return expandedString;
	}
	
	public String getExpandedReplacementString() {
		String expandedString = replacementString;
		int replacementNumber = 0;
		for ( Replacement replacement : replacements) {
			if (replacement.getReplacementString() != null ) {
				expandedString = expandedString.replace(
				X + (replacementNumber+1), replacement.getReplacementString());
			}
			replacementNumber++;
		}
		
		BufferedReader bufReader = new BufferedReader(new StringReader(expandedString));
		String line=null;
		String indentedExpandedString = "";
		try {
			boolean firstLine = true;
			while( (line=bufReader.readLine()) != null )
			{
				if (!firstLine) {
					indentedExpandedString += "\n";
				}
				indentedExpandedString += originalIndent + line;
				firstLine = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return expandedString;
		}
		return indentedExpandedString;
	}
	
	public String toString() {
		return templateString;
	}

	public String getTemplateString() {
		return templateString;
	}

	public String getTemplateStringWithoutPrefixes() {
		return templateString.replace(X + ":", "");
	}	

	public String getTemplateStringWithoutSuffixes() {
		return templateString.replaceAll("\\:[^ " + REPLACEMENT_TERMINATOR + "]*", "");
	}
	
	public void setTemplateString(String templateString) {
		this.templateString = templateString;
	}
	
	public String getRemainingTemplate() {
		String remainingTemplate = templateString;
		int indexOfLastTypedSpace = typedString.lastIndexOf(' ');
		if ( indexOfLastTypedSpace > 0 ) {
			if (!remainingTemplate.startsWith(typedString)) {
				for ( Replacement replacement : replacements) {
					if (replacement.getReplacementString() != null ) {
						remainingTemplate = remainingTemplate.replaceFirst(X, replacement.getReplacementString().replace("\\",  "\\\\"));
						remainingTemplate = remainingTemplate.replaceFirst("\\:[^ " + REPLACEMENT_TERMINATOR + "]*", "");
						if ( remainingTemplate.startsWith(typedString) ) {
							match = true;
							break;
						}
					}
				}
			}
		} else {
			indexOfLastTypedSpace = 0;
		}
		return remainingTemplate.substring(indexOfLastTypedSpace).replace(X + ":", "");
	}

	public void removePrefixes() {
		templateString = templateString.replace(X + ":", "");
	}

	public void removeSuffixes() {
		templateString = templateString.replaceAll(
				"\\:[^ " + REPLACEMENT_TERMINATOR + "]*", "");
	}

	public void setString(String newString) {
		templateString = newString;
	}

	public boolean beginningsMatch() {
		if (typedString == null) {
			return false;
		}
		return templateString.startsWith(typedString);
	}

	public void setIndexOfX() {
		indexOfX = templateString.indexOf(X);
	}
	
	public void setIndexOfX(int value) {
		indexOfX = value;
	}
	
	public boolean beginningsMatchToReplacement() {
		if ( typedString == null ) {
			return false;
		}
		return (indexOfDisplayX >= 0
				&& typedString.startsWith(templateString.substring(0, indexOfDisplayX)));
	}

	public String getDisplayStringAfterX() {
		indexAfterDisplayX = indexOfDisplayX + X.length();
		// X:SOMETHING -- add
		if (indexAfterDisplayX + 1 < templateString.length()) {
			String afterX = templateString.substring(
					indexAfterDisplayX,
					indexAfterDisplayX + 1);
			if (afterX.equals(":")) {
				int indexAfterName = templateString.indexOf(
						" ", indexAfterDisplayX + 1);
				if (indexAfterName > indexAfterDisplayX) {
					indexAfterDisplayX = indexAfterName;
				}
			}
		}
		return templateString.substring(indexAfterDisplayX);
	}

	public String getRemainingDisplayString() {
		return templateString.substring(indexAfterDisplayX);
	}

	public int getIndexOfDelimeter() {
		return indexOfDelimeter;
	}

	public int getIndexOfX() {
		indexOfX = templateString.indexOf(X);
		return indexOfX;
	}

	public int indexOfX() {
		return templateString.indexOf(X);
	}

	public boolean containsX() {
		return templateString.contains(X);
	}
	
	public boolean moreReplacements() {
		return (indexOfDisplayX2 >= 0 && indexOfDelimeter >= 0);
	}

	public void replaceNextReplacement() {
		
	}

	public boolean isMatch() {
		return match;
	}

	private void populateDefaults() {
		String xReplacement = "";
		int tempReplacementIndex = 1;
		String currentX = X + "1";
		int xReplacementBegin = 9999;
		String nextX = X + (tempReplacementIndex+1); // e.g. X2
		int xReplacementEnd = defaultsString.indexOf(nextX) - 1;

		while (xReplacementBegin >=  currentX.length() + 1) {
			currentX = X + tempReplacementIndex; // e.g. X1
			nextX = X + (tempReplacementIndex+1); // e.g. X2
			xReplacementBegin = defaultsString.indexOf(currentX) + currentX.length() + 1;
			if ( xReplacementBegin >= currentX.length() + 1) {
				xReplacementEnd = defaultsString.indexOf(nextX) - 1;
				if (xReplacementEnd < -1 && xReplacementBegin < defaultsString.length() ) {
					xReplacement = defaultsString.substring(xReplacementBegin);
				} else {
					xReplacement = defaultsString.substring(xReplacementBegin, xReplacementEnd);
				}
				defaults.add(xReplacement); 
			}
			tempReplacementIndex++;
		}
	}
}
