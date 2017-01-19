package com.github.chameleon.core.grammar.english;


public class Grammar {

	private Grammar() {
	}
	
	//http://www.really-learn-english.com/spelling-rules-add-s-verb.html
	public String removeSFromVerb(String in) {
        String verbWithoutS = in;
    	if( in.endsWith("ses") ||	// misses  --> miss 
    		in.endsWith("ches") ||	// watches --> watch
    		in.endsWith("xes") ||	// mixes   --> mix
    		in.endsWith("zes") ||	// buzzes  --> buzz
    		in.endsWith("oes") ||	// goes    --> go
    		in.endsWith("shes") ) {	// washes  --> wash
    		verbWithoutS = in.substring(0, in.length()-2);
    	} else {
            if( in.endsWith("ies") ) {	// flies --> fly
        		verbWithoutS = in.substring(0, in.length()-3);
        		verbWithoutS += "y";
            } else {
	            if( in.endsWith("s") ) {	// adds --> add
	        		verbWithoutS = in.substring(0, in.length()-1);
	            }
            }
        }
        return verbWithoutS;
    }

	public static boolean isVowel(char c) {
		char l = Character.toLowerCase(c);
		return (l=='a'||l=='e'||l=='i'||l=='o'||l=='u'||l=='y');
	}
	
	//http://www.really-learn-english.com/spelling-rules-add-s-verb.html
	public static String addSToVerb(String in) {
        String verbWithoutS = in;
    	if( in.endsWith("s") ||		// miss  --> misses 
    		in.endsWith("ch") ||	// watch --> watches
    		in.endsWith("x") ||		// mix   --> mixes
    		in.endsWith("z") ||		// buzz  --> buzzes
    		in.endsWith("o") ||		// go    --> goes
    		in.endsWith("sh") ) {	// wash  --> washes
    		verbWithoutS += "es";
    	} else {
    		char before = 'a';
    		if (in.length()>=2) {
    			before = in.charAt(in.length()-2);			//2nd to last char
    		}
            if( in.endsWith("y") && !isVowel(before) ) {	// fly --> flies
        		verbWithoutS = in.substring(0, in.length()-1);
        		verbWithoutS += "ies";
            } else {
	            // add --> adds (default)
            	verbWithoutS += "s";
            }
        }
        return verbWithoutS;
    }
}
