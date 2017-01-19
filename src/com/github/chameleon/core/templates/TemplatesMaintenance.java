package com.github.chameleon.core.templates;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;

import com.github.chameleon.core.Template;
import com.github.chameleon.core.Thesaurus;

public class TemplatesMaintenance {
	private static XPathFactory xPathfactory = XPathFactory.newInstance();
	private static XPath xpath = xPathfactory.newXPath();

	private static boolean TESTING = false;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		boolean CONVERT_HTML_TO_TEXT = true;
		
		String language = "python";

		String mapName = "";
		String baseWebDirectory = "";
		String baseOutputDirectoryRelative = "";

		Boolean enabled = true;
		String index = "index.html";
		String methodsXpath = "";
		String replacementXpath = "";
		String helpXpath = "";
		String summaryXpath = "";
		List<String> summaryRegex = null;
		List<String> summaryRegexRemove = null;
		List<String> replacementRegex = null;
		List<Map<String, String>> replacementFindReplace = null;

		String languageDirectory = "../../Chameleon.git/com.github.chameleon.eclipse."
				+language+"/languages/english/"+language;

		// Open YAML file...
//		https://bitbucket.org/asomov/snakeyaml/wiki/Documentation
		Yaml yaml = new Yaml();
	    InputStream input;
	    

		int created = 0;
		int done = 0;
	    
		System.out.println("Templates Maintenance...  This may take a while...  Summary given when done...");
		System.out.println("To implement templates:");
		System.out.println("* Wait until the summary is shown.");
		System.out.println("* Click on a file to edit the template.");
		System.out.println("* Make changes as needed to the template.");
		System.out.println("* Save the template.");
		System.out.println("* Move it from the TODO directory to the non-TODO directory.");
		System.out.println("* Continue with other templates.");
		System.out.println("* Run this script again to see remaining templates and progress.");
		System.out.println();
		System.out.println("Thanks!");
		System.out.println();
		System.out.println("Remaining templates to process... (Please be patient)...");
		System.out.println();
	    
		try {
			input = new FileInputStream(new File(languageDirectory+"/templates.yaml"));
		    Object data = yaml.load(input);
		    //Map map = (Map) yaml.load(input);
			List<Map<String, String>> list = (List<Map<String, String>>) data;
			int mapNumber = 0;
			int mapCount = list.size();
			for( Map map : list) {
				mapNumber++;
				enabled = (Boolean) map.get("enabled");
				if (enabled == null) {
					enabled = true;	//default
				} else {
					if (!enabled) {
						continue;
					}
				}
				mapName = (String) map.get("name");
				index = (String) map.get("index");
				if ( index == null ) {
					index = "index.html"; //default
				}
				language = (String) map.get("language");
				baseOutputDirectoryRelative = (String) map.get("output");
				baseWebDirectory = (String) map.get("url") + "/";
				methodsXpath = (String) map.get("methodsXpath");
				replacementXpath = (String) map.get("replacementXpath");
				helpXpath = (String) map.get("helpXpath");
				summaryXpath = (String) map.get("summaryXpath");
				summaryRegex = (List<String>) map.get("summaryRegex");
				summaryRegexRemove = (List<String>) map.get("summaryRegexRemove");
				replacementRegex = (List<String>) map.get("replacementRegex");
				replacementFindReplace = (List<Map<String, String>>) map.get("replacementFindReplace");
				
				System.out.println("############################################");
				System.out.println("NAME="+mapName);
				System.out.println("LANGUAGE="+language);
				System.out.println("The URL="+baseWebDirectory);
				System.out.println("output dir="+baseOutputDirectoryRelative);
				System.out.println("############################################");
			
				System.out.println(list);
				System.out.println();
				System.out.println();
			


				String statementDirectory = languageDirectory+"/STATEMENT";
				String baseOutputDirectory = statementDirectory + baseOutputDirectoryRelative;
				String indexPath = baseWebDirectory + index;
				String firstName = "";

				String synonymVerbsFile = statementDirectory + "/../../_synonym_verbs.txt";
				String synonymNounsFile = statementDirectory + "/../../_synonym_nouns.txt";
				String synonymOtherFile = statementDirectory + "/../../_synonym_other.txt";
		        Thesaurus verbsThesaurus = new Thesaurus(synonymVerbsFile);
		        verbsThesaurus.setVerbs();
		        Thesaurus nounsThesaurus = new Thesaurus(synonymNounsFile);
		        Thesaurus otherThesaurus = new Thesaurus(synonymOtherFile);
				        
				Document doc = load(indexPath);
				
				XPathExpression linkExpr = xpath.compile("//a/@href");
	
				NodeList pageNodes = (NodeList) linkExpr.evaluate(doc, XPathConstants.NODESET);
	           	for (int pageNumber = 0; pageNumber < pageNodes.getLength(); pageNumber++) {
	           		String link = pageNodes.item(pageNumber).getNodeValue();
	           		if (link.startsWith("#")) {
//	           			System.out.println("Skipped link starting with #: "+link);
	           			continue;
	           		}
	           	    String name = link.replace(".html", "");
	           		if (link.contains("#")) {
//	           			System.out.println("Link with #: "+link);
//	           			continue;
	           			int hashIndex = link.indexOf("#");
		           	    name = link.substring(hashIndex+1);
	           		}
	           		String  myUrl = baseWebDirectory + link;
	           		if (link.startsWith("http")) {
	           			continue;
	           		}
	           	    
	           	    
	           	    if ( firstName.isEmpty() && !name.equals("#") ) {
	           	    	firstName = name;
	           	    } else {
	           	    	if (name.equals(firstName)) {
	           	    		break;
	           	    	}
	           	    }
	           	    try {
		           	    	
		           		Document page = load(myUrl);
		           		
	
		           		
		                // METHODS:
		    			XPathExpression methodsExpr = xpath.compile(methodsXpath);
		    			
		                String actualDirectory = baseOutputDirectory + name + "/";
		                String todoDirectory = baseOutputDirectory + name + "_TODO/";
		                boolean directoryPrinted = false;
		    			NodeList methodNodes = (NodeList) methodsExpr.evaluate(page, XPathConstants.NODESET);
		               	for (int methodNumber = 0; methodNumber < methodNodes.getLength(); methodNumber++) {
		               		String defaultsText = "";
		               		String fullMethodName = methodNodes.item(methodNumber).getNodeValue();
		                    String methodName = fullMethodName.replace("()", "");
		                    methodName = methodName.replaceFirst(".*\\.", "").trim();
		
		                    String todoFilename = todoDirectory + methodName + ".txt";
		                    String actualFilename = actualDirectory + methodName + ".txt";
		                    
		                    if (!directoryPrinted) {
		                        System.out.println(baseOutputDirectoryRelative + name);
		                        directoryPrinted = true;
		                    }
		
		                    if ((new File(actualFilename).exists())) {
		                    	done++;
		                    	continue;
		                    }
		                    
		                    // SUMMARY TEXT:
		                    String xpathString = summaryXpath;
		                    xpathString = xpathString.replace("FULLMETHOD", fullMethodName);
		                    xpathString = xpathString.replace("METHOD", methodName);
		                    String summaryText = getSummaryText(xpathString, page);
		                    if ( summaryText.isEmpty()) {
		                    	continue;
		                    } else {
		                    	if ( summaryRegexRemove != null) {
		                			for( String regex : summaryRegexRemove) {
		                				summaryText = summaryText.replaceAll(regex, "");
		                			}
		                    	}
		                    	if ( summaryRegex != null) {
		                			for( String regex : summaryRegex) {
		                				summaryText = summaryText.replaceAll(regex, "$1");
		                			}
		                    	}
		                    	String safeMethodName = methodName.replace("*",  "\\*");
		                    	summaryText = summaryText.replaceFirst("(?i)^"+safeMethodName+" ", "");
		                    	summaryText = summaryText.replaceAll("\\(.*\\)", "");
		                    	if (summaryText.contains(".")) {
		                    		String[] array = summaryText.split("\\.");
		                    		if (array.length > 0) {
		                    			summaryText = summaryText.split("\\.")[0];
		                    		}
		                    	}
		                    	if (summaryText.contains(";")) {
		                    		String[] array = summaryText.split("\\;");
		                    		if (array.length > 0) {
		                    			summaryText = summaryText.split("\\;")[0];
		                    		}
		                    	}
		                    	summaryText = summaryText.replaceAll("   *", " ");
		                    }
		                    
		                    String completionText = 
		                    		summaryText.replace(".", "").toLowerCase();

		                    completionText = verbsThesaurus.addReplacements(completionText);
		                    completionText = nounsThesaurus.addReplacements(completionText);	                    
		                    completionText = otherThesaurus.addReplacements(completionText);

		                    // HELP TEXT:
		                    String helpText = "";
		                    xpathString = helpXpath;
		                    xpathString = xpathString.replace("METHOD", methodName);
		        			XPathExpression helpExpr = xpath.compile(xpathString);
		        			NodeList helpNodes = (NodeList) helpExpr.evaluate(page, XPathConstants.NODESET);
		                   	for (int helpNumber = 0; helpNumber < helpNodes.getLength(); helpNumber++) {
		                   		if (CONVERT_HTML_TO_TEXT) {
		                            helpText += helpNodes.item(helpNumber).getTextContent();	//HTML->TEXT (but for Java, we want html...)
		                   		} else {
			                        TransformerFactory tf = TransformerFactory.newInstance();
			                        Transformer transformer;
			                        try {
			                            transformer = tf.newTransformer();
			                            // below code to remove XML declaration
			                            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			                            StringWriter writer = new StringWriter();
			                            transformer.transform(new DOMSource(helpNodes.item(helpNumber)), new StreamResult(writer));
			                            helpText += writer.getBuffer().toString();
			                        } catch (TransformerException e) {
			                            e.printStackTrace();
			                        }                                       			
		                   		}
		                    }
		                    helpText = helpText.trim();
		                    helpText += "\n" + myUrl;
		                    if ( helpText.isEmpty()) {
		                    	continue;
		                    }    
		               	
		
		                    // TOOLTIP TEXT:
							String tooltipText = "";
		                    if (completionText.contains("index")) {
		                        tooltipText = 
		                        		"!! Python is ZERO based, so lists start at 0";
		                    }                    
	
		                    // REPLACEMNT TEXT:
		                    xpathString = replacementXpath;
		                    xpathString = xpathString.replace("METHOD", methodName);
		                    XPathExpression codeExpr = xpath.compile(xpathString);
		        			
		                    
		                    String replacementText = getFlattenedEvalutation(codeExpr, page);;

		                    if (replacementText.isEmpty()) {
		                    	replacementText = helpText;
		                    	if ( replacementRegex != null) {
		                			for( String regex : replacementRegex) {		                				
		                				regex = regex.replace("METHOD", 
		                						methodName.replace("*",  "\\*"));
		                				replacementText = replacementText.replaceAll(regex, "$1");
		                			}
		                    	}
		                    	if (replacementText.contains("\n")) {
		                    		String[] array = replacementText.split("\n");
		                    		if (array.length > 0) {
		                    			replacementText = replacementText.split("\n")[0];
		                    		}
		                    	}
		                    	int defaultNumber = 1;
		                    	if (replacementFindReplace != null) {
			            			for( Map<String, String> findReplace : replacementFindReplace) {
			            				String find = (String) findReplace.get("find");
			            				String replace = (String) findReplace.get("replace");
			            				if (find != null && replace != null) {
			            					String original = replacementText;
			            					int position = replacementText.indexOf(find);
			            					int parameter = defaultNumber;
			            					if ( position >= 0 ) {
				            					parameter = charactersInString(',', 
				            							replacementText.substring(0, position))+1;
			            					} else {
			            						parameter = 1;
			            					}
			            					if (parameter>1) {
			            						System.out.println("Parameter="+parameter);
			            					}
		            						replace = replace.replace("#", ""+parameter);
			            					replacementText = replacementText.replaceFirst(find, replace);
			            					if (!replacementText.equals(original)) {
					            				String completionFind = (String) findReplace.get("completionFind");
					            				String completionReplace = (String) findReplace.get("completionReplace");
					            				String defaults = (String) findReplace.get("defaults");
					            				if (completionFind != null && completionReplace != null) {
					            					original = summaryText;
					            					completionText = completionText.replaceAll(completionFind, completionReplace);
//					            					if (!completionText.equals(original)) 
					            					{
					            						defaults = defaults.replace("#", ""+parameter);
					            						defaultsText += defaults+"\n";
					            						if (parameter != defaultNumber) {
					            							defaultNumber++;
					            						}
					            					}
					            				}
			            					}
			            				}	
			            			}
		            			}
		                    }
		                    
		                   	replacementText = replacementText.trim();
		                   	
		                    if ( !replacementText.isEmpty()) {
//		                    	System.out.println(">>>>>>>\n" 
//		                    			+ replacementText
//		                    			+ "<<<<<<<<\n");
		                    }
		
			                    
		
		                    //
		                    
		                    
		                    Template template = new Template(todoFilename,
		                    		completionText,
		                    		summaryText, replacementText, defaultsText, helpText,
		                    		tooltipText, baseOutputDirectoryRelative 
		                    			+ name + "/" + methodName + ".txt");
		
		            		String absoluteTodoDirectory = todoDirectory;
		            		try {
		            			absoluteTodoDirectory = 
		            					(new File(todoDirectory)).getCanonicalPath();
		            		} catch (IOException e1) {
		            			e1.printStackTrace();
		            		}
		
		                    if (!TESTING) {
		                    	(new File(actualDirectory)).mkdir();
		                    	(new File(todoDirectory)).mkdir();
		                    }
		                    
		                    System.out.printf(" %d/%d %,d " + methodName + "  File \"" +
		                    		absoluteTodoDirectory + "\\" + methodName + 
		                    		".txt\", line 2\n", mapNumber, mapCount,  created);
		                    
		                    if (!TESTING) {
								template.save();
			                    created++;
		                    }
		               	}
	        		} catch (ClassCastException e) {
	        			System.out.println("Unable to parse HTML for: " + myUrl);
		    		} catch (IOException e) {
		    			System.out.println("Unable to open url:" + myUrl);
		    		}
	           	    catch (Exception e) {
	        			e.printStackTrace();
	 	        	}	           	
	        	}
	           	
	           	System.out.println();
		       	System.out.println("Existing, Created");
		       	System.out.println("------------------------------------");
				System.out.printf("%,d\t%,d\t%s\n", done, created, mapName);
	           	map.put("results_done", done);
	           	map.put("results_created", created);
	           	done = 0;
	           	created = 0;
			}			
	       	System.out.println();
	       	System.out.println("SUMMARY:");
	       	System.out.println("Existing, Created");
	       	System.out.println("------------------------------------");
	       	int total_done = 0;
	       	int total_created = 0;
	       	for( Map map : list) {
				enabled = (Boolean) map.get("enabled");
				if (enabled == null) {
					enabled = true;	//default
				} else {
					if (!enabled) {
						continue;
					}
				}
				mapName = (String) map.get("name");
				done = (Integer) map.get("results_done");
				created = (Integer) map.get("results_created");
				System.out.printf("%,d\t%,d\t%s\n", done, created, mapName);
				total_done += done;
				total_created += created;
			}
	       	System.out.println("------------------------------------");
			System.out.printf("%,d\t%,d\t <== TOTAL\t(%f%%)\n", total_done, 
					total_created, 100.0*(total_done/total_created));
			
		} catch (ClassCastException e) {
			System.out.println("Class cast exception for:");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}

	private static int charactersInString(char c, String s) {
		int counter = 0;
		for( int i=0; i<s.length(); i++ ) {
		    if( s.charAt(i) == c ) {
		        counter++;
		    } 
		}
		return counter;
	}
	
	private static String getFlattenedEvalutation(XPathExpression codeExpr, Document page) throws XPathExpressionException {
        String flattenedText = "";
        NodeList codeNodes = (NodeList) codeExpr.evaluate(page, XPathConstants.NODESET);
       	for (int codeNumber = 0; codeNumber < codeNodes.getLength(); codeNumber++) {
       		String code = codeNodes.item(codeNumber).getNodeValue();
            flattenedText += code;
        }
       	return flattenedText;
	}
	
	private static String getSummaryText(String xpathString, Document page) {
		String summaryText = "";
		XPathExpression summaryExpr;
		try {
			summaryExpr = xpath.compile(xpathString);
			NodeList summaryNodes = (NodeList) 
					summaryExpr.evaluate(page, XPathConstants.NODESET);

			if (summaryNodes.getLength() > 0) {	
				summaryText = "";				
				for(int nodeNumber = 0; nodeNumber < summaryNodes.getLength(); nodeNumber++) {
					if(summaryNodes.item(nodeNumber).getNodeType() == Node.ELEMENT_NODE) {
						Node node = summaryNodes.item(nodeNumber).getChildNodes().item(0);
						if (node != null) {
							summaryText += node.getNodeValue();
						}
					} else {
						summaryText += summaryNodes.item(nodeNumber).getNodeValue();
					}
				}
				summaryText = 
						summaryText.replace("\n", " ").replace("\r",  " ");
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return summaryText;
	}

	private static Document load(String in) throws IOException {
		URL url;
		try {
			url = new URL(in);
		    InputStream is = url.openStream();  // throws an IOException
		    BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    String line;
		    String xmlText = "";
		    while ((line = br.readLine()) != null) {
		        xmlText += line + "\n";
		    }
			TagNode tagNode = new HtmlCleaner().clean(xmlText);
			Document doc = new DomSerializer(
			        new CleanerProperties()).createDOM(tagNode);
			return doc;
		} catch (MalformedURLException e) {
			System.out.println("Bad url:" + in);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
