package com.github.chameleon.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.chameleon.core.grammar.english.Grammar;


public class Thesaurus {

    public ArrayList<String[]> synonymsList = new ArrayList<String[]>();
    private boolean verbs = false;
    
	public Thesaurus(String synonymsFile) {
    	BufferedReader br = null;
    	String line = "";
    	String csvSplitBy = ",";
    	try {
    		br = new BufferedReader(new FileReader(synonymsFile));
    		while ((line = br.readLine()) != null) {
    			String[] words = line.split(csvSplitBy);
    			synonymsList.add(words);
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		if (br != null) {
    			try {
    				br.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}

	}
	
	public void setVerbs() {
		verbs = true;
	}

	public void setVerbs(boolean verbs) {
		this.verbs = verbs;
	}

	public String addReplacements(String in) {
    	String completionText = in;
        for (String[] synonyms : synonymsList) {
            for (String word : synonyms) {
                String match = word;
                String replacement = "";
                Pattern p = Pattern.compile("\\b"+match+"\\b");
                Matcher m = p.matcher(completionText);
                boolean found = m.find();
                if (!found && verbs) {
                	match = Grammar.addSToVerb(word);
                    p = Pattern.compile("\\b"+match+"\\b");
                    m = p.matcher(completionText);
                    found = m.find();
                }
                if (found) {
                    if (replacement == "") {
                    	replacement = "(";
                    	String comma = "";
                    	for (String w : synonyms) {
                    		replacement += comma + w;
                    		comma = ",";
                        }
                    	if (synonyms.length == 1) {
                    		replacement += ",";
                    	}
                        replacement += ")";
                    }
                    completionText = completionText.replaceAll(
                        "\\b" + match + "\\b", replacement);
                    break;
                }
            }
		}	
        return completionText;
    }
	
}

