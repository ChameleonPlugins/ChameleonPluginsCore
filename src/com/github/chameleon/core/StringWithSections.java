package com.github.chameleon.core;

import java.util.LinkedList;
import java.util.List;

/**
 * Strings with sections, such as "(create,make)( a,)( new,) file" 
 * where each GROUP of sections is denoted with opening and closing parenthesis
 * and sections original groups are seperated by a delimiter, such as ','
 * 
 * @author Jesse Olsen
 *
 */
public class StringWithSections {

	final String BEGIN = "(";
	final String END = ")";
	String DELIMETER; // "|" does NOT work with .split() :-(

	private List<String> out = new LinkedList<String>();

	int beginIndex = -1;
	int endIndex = -1;
	public int length = 0;

	public StringWithSections(String original, String delimiter) {
		DELIMETER = delimiter;
		expandString(moveLastEmptyToFirst(
				replaceQuestionMarksWithEmpty(original, delimiter),delimiter));
		if (out.size() == 0) {
			out.add(original);
		}
	}

	// e.g. open ( a)?file --> open (, a)file
	public String replaceWithEmpty(String in, String delimiter, String toBeReplaced) {
		int index = in.indexOf(toBeReplaced);
		if (index > 0) {
			int lastIndex = in.substring(0, index).lastIndexOf("(");
			in = in.substring(0, lastIndex)+"("+delimiter+
					in.substring(lastIndex+1);
			in = in.replace(toBeReplaced, ")");
			in = replaceQuestionMarksWithEmpty(in, delimiter);
		}
		return in;
	}

	// e.g. open ( a)?file --> open (, a)file
	public String replaceQuestionMarksWithEmpty(String in, String delimiter) {
		return replaceWithEmpty(in, delimiter, ")?");
	}

	// e.g. open ( a,)file --> open (, a)file
	// Reason: Java doesn't support empty string at the end
	public String moveLastEmptyToFirst(String in, String delimiter) {
		return replaceWithEmpty(in, delimiter, ",)");
	}

	public int getSize() {
		return out.size();
	}
	public List<String> getExpandedStrings() {
		return out;
	}

	private void expandString(String in) {
		String beginString = "";
		String endString = "";
		String sections[];
		beginIndex = in.indexOf(BEGIN) + 1;
		if(beginIndex == 0) {
			out.add(in);
		} else {
			endIndex = in.indexOf(END);
			if (beginIndex > 0) {
				beginString = in.substring(0, beginIndex - 1);
			}
			if (endIndex > 0) {
				endString = in.substring(endIndex + 1);
			}
			if (beginIndex > 0 && endIndex > beginIndex) {
				if (in.indexOf(DELIMETER) >= 0) {
					sections = in.substring(beginIndex, endIndex).split(DELIMETER);
					for(int section=0; section < sections.length; section++) {
						expandString(getStringForSection(in, section, sections,
								beginString, endString));
					}
				} else {
					out.add(in);
				}
			}
		}
	}

	private String getStringForSection(String in, int section,
			String[] sections, String beginString, String endString) {
		if (sections != null && sections.length >= section) {
			return beginString + sections[section] + endString;
		} else {
			return in;
		}
	}
	
	
	
//	public String[] getExpandedStrings() {
//	String[] out = {};
//	int i=0;
//	for (int group = 0; group < groupsCount; group++) {
//		int sectionsCount = sections[group].length;
//		for (int section = 0; section < sectionsCount; section++) {
//			out[i]=sections[group][section];
//			i++;
//		}
//	}
//	return out;
//}

}
