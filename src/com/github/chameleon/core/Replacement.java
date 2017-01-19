package com.github.chameleon.core;


/**
 * Replacement for StringWithReplacements, such as "If X then X else X" where X is
 * the Replacement object.
 * 
 * Replacement keeps track of where original the StringWithReplacement it is located,
 * what X was, and what the new value will be...
 * 
 * @author Jesse Olsen
 *
 */
public class Replacement {
	String X = "X";

	private String originalString;		//e.g. X:FILE
	private String replacementString;	//e.g. myfile.txt
	int startIndex;						//where replacement is found original StringWithReplacements
	int originalEndIndex;				//startIndex+length of originalString
	int replacementEndIndex;			//startIndex+length of replacementString
	
	public Replacement(String originalString, int startIndex, int originalEndIndex, String X) {
		this.setOriginalString(originalString);
//		this.replacementString = replacementString;
		this.startIndex = startIndex;
		this.originalEndIndex = originalEndIndex;
//		this.replacementEndIndex = replacementEndIndex;
	}

	public String getReplacementString() {
		return replacementString;
	}

	public void setReplacementString(String replacementString) {
		this.replacementString = replacementString;
	}

	public String getOriginalString() {
		return originalString;
	}

	public void setOriginalString(String originalString) {
		this.originalString = originalString;
	}

}
