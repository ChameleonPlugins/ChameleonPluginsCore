package com.github.chameleon.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Template {
	protected String fileText = "";
	protected String fileName = "";
	public Template(String fileName, String completionText, String summaryText, String replacementText,
			String defaultValues, String helpText, String tooltipText, 
			String path) {
		if (fileName.contains("*")) {
			fileName = fileName.replace("*", "STAR");
		}
		this.fileName = fileName;
		fileText = 
			"//\\\\ //// code completion entries (1+ lines):\n"
			+ completionText + "\n"
			+ "//\\\\ //// ==> completion hint (1 line):\n"
			+ summaryText + "\n"
			+ "//\\\\ //// Replacement text (1+ lines):\n"
			+ replacementText + "\n"
			+ "//\\\\ //// Default Values, if needed (e.g. X1=x>0) (1+ lines):\n"
			+ defaultValues + "\n"
			+ "//\\\\ //// Help/documentation (1+ lines):\n"
			+ helpText + "\n"
			+ "//\\\\ //// Tool tip (1 line):\n"
			+ tooltipText + "\n"
			+ "//\\\\ //// Template path (e.g. core/sayHi.txt) (1 line):\n"
			+ path;	
	}
	
	public String toString() {
		return fileText;
	}
	
	public void save() {
        PrintWriter writer;
		try {
			(new File(fileName)).getAbsoluteFile().getParentFile().mkdirs();			
			writer = new PrintWriter(fileName);
            writer.write(fileText);
            writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
