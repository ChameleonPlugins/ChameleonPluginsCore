/**
 * 
 */
package com.github.chameleon.core;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.chameleon.core.StringWithReplacements;

/**
 * @author JESSE
 *
 */
public class StringWithReplacementsTest {

	String language = "english";
	String X = "X";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	public StringWithReplacements createStringWithReplacements(String typedString, String templateString, String replacementString, String defaults) {
		return new StringWithReplacements(typedString, templateString, replacementString, defaults, X);
	}

	@Test
	public void testSay() {
		StringWithReplacements swr = createStringWithReplacements("say", "say a line of text", "print \"text!\"", "Defaults");
		String expectedExpandedReplacementString = "print \"text!\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testIndent4() {
		StringWithReplacements swr = createStringWithReplacements("    say", "say a line of text", "print \"text!\"", "Defaults");
		String expectedExpandedReplacementString = "    print \"text!\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testIndent8() {
		StringWithReplacements swr = createStringWithReplacements("        say", "say a line of text", "print \"text!\"", "Defaults");
		String expectedExpandedReplacementString = "        print \"text!\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testIndentTab() {
		StringWithReplacements swr = createStringWithReplacements("\tsay", "say a line of text", "print \"text!\"", "Defaults");
		String expectedExpandedReplacementString = "\tprint \"text!\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testInputEscapedTab() {
		StringWithReplacements swr = createStringWithReplacements("write \"\\t\" to file", "write X to file X", "X2.print(X1)", "X1=\"text\",X2=myfile");
		String expectedExpandedReplacementString = "myfile.print(\"\\t\")";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}


	@Test
	public void testIndentMultipleLines() {
		StringWithReplacements swr = createStringWithReplacements("    if x>0 then print \">\" else print \"not\" done",
				"if X then X else X done", "if X1:\n    X2\nelse:\n    X3", "");
		String expectedExpandedReplacementString = "    if x>0:\n        print \">\"\n    else:\n        print \"not\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
		String expectedTemplateString = "if X then X else X done";
		assertEquals("templateStrings must match",
				expectedTemplateString, swr.getTemplateStringWithoutPrefixes());
	}
	
	@Test
	public void testSayX() {
		StringWithReplacements swr = createStringWithReplacements("say HI", "say X and go to the next line", "print \"X1\"", "Defaults");
		String expectedExpandedReplacementString = "print \"HI\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testSayXTEXT() {
		StringWithReplacements swr = createStringWithReplacements("say HI", "say X:TEXT and go to the next line", "print \"X1\"", "Defaults");
		String expectedExpandedReplacementString = "print \"HI\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testOpenXFILEasXNAME() {
		StringWithReplacements swr = createStringWithReplacements("open file.txt as myfile", "open X:FILE as X:NAME", "X2=open(X1)", "Defaults");
		String expectedExpandedReplacementString = "myfile=open(file.txt)";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}

	@Test
	public void testIfXThenXElseXDone() {
		StringWithReplacements swr = createStringWithReplacements("if x>0 then print \">\" else print \"not\" done",
				"if X then X else X done", "if X1:\n\tX2\nelse:\n\tX3", "");
		String expectedExpandedReplacementString = "if x>0:\n\tprint \">\"\nelse:\n\tprint \"not\"";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
		String expectedTemplateString = "if X then X else X done";
		assertEquals("templateStrings must match",
				expectedTemplateString, swr.getTemplateStringWithoutPrefixes());
	}
	
	@Test
	public void testOpenFileXFILEasXNAMEdone1() {
		StringWithReplacements swr = createStringWithReplacements("open", "open file X:FILE as X:NAME done", "X2=open(X1)", "X1=file.txt\nX2=myfile");
		String expectedExpandedReplacementString = "myfile=open(file.txt)";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
		String expectedTemplateString = "open file FILE as NAME done";
		assertEquals("templateStrings must match",
				expectedTemplateString, swr.getTemplateStringWithoutPrefixes());
	}

	@Test
	public void testOpenFileXFILEasXNAMEdone2() {
		StringWithReplacements swr = createStringWithReplacements("open file", "open file X:FILE as X:NAME done", "X2=open(X1)", "X1=file.txt\nX2=myfile");
		String expectedExpandedReplacementString = "myfile=open(file.txt)";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
		String expectedTemplateString = "open file FILE as NAME done";
		assertEquals("templateStrings must match",
				expectedTemplateString, swr.getTemplateStringWithoutPrefixes());
	}

	@Test
	public void testIterateOverItem() {
		StringWithReplacements swr = createStringWithReplacements("iterate over tsv original 1,23 doing print hi",
				"iterate over X:ITEM original X:ARRAY doing X:ACTION", 
				 "for X1 original X2:\n    X3", "Defaults");
		String expectedExpandedReplacementString = "for tsv original 1,23:\n    print hi";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);

	}

	@Test
	public void testIterateOverItemBrackets() {
		StringWithReplacements swr = createStringWithReplacements("iterate over tsv original [1,23] doing print hi",
				"iterate over X:ITEM original X:ARRAY doing X:ACTION", 
				 "for X1 original X2:\n    X3", "Defaults");
		String expectedExpandedReplacementString = "for tsv original [1,23]:\n    print hi";
		assertEquals("ReplacementStrings must match",
				expectedExpandedReplacementString, swr.expandedReplacementString);
	}
	
}
