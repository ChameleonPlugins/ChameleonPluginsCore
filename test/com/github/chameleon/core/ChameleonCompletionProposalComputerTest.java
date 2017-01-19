/**
 * 
 */
package com.github.chameleon.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jesse.olsen@hpe.com
 *
 */
public class ChameleonCompletionProposalComputerTest {

	String language = "english";
	String X = "X";
	ChameleonTestCompletionProposalComputer computer;
	Map<String, Object> proposals;
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
		try {
			computer = new ChameleonTestCompletionProposalComputer();
			computer.setTesting(true);
			proposals = new HashMap<String, Object>();
		} catch (Exception e) {
			// Ignore exception caused by file not being there during test
			// runs...
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testShowTemplate() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 6;
		addEntry("show", "show template", "//template");
		assertProposalsSize(1);
		checkEntry(0, "show template", "//template\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testReplaceXWithXToResults() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("replace '\\\\' with ','",
				"replace X:OLD with X:NEW in X:STRING to X:RESULTS",
				"X4 = X3.replace(X1, X2)",
				"X1=old\nX2=new\nX3=\"My old hat is very old\"\nX4=output");
		assertProposalsSize(1);
		checkEntry(0, "',' in STRING to RESULTS",
				"output = \"My old hat is very old\".replace('\\\\', ',')\n\n",
				expectedReplacementOffset,
				expectedReplacementLength);
	}
	
	@Test
	public void testReplaceXWithXInOutputToOutput() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("replace '\\\\' with ',' in output to output",
				"replace X:OLD with X:NEW in X:STRING to X:RESULTS",
				"X4 = X3.replace(X1, X2)",
				"X1=old\nX2=new\nX3=\"My old hat is very old\"\nX4=output");
		assertProposalsSize(1);
		checkEntry(0, "output",
				"output = output.replace('\\\\', ',')\n\n",
				expectedReplacementOffset,
				expectedReplacementLength);
	}
	
	@Test
	public void testReplaceXWithXInOutputToRESULTS() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("replace '\\\\' with ',' in output to RESULTS",
				"replace X:OLD with X:NEW in X:STRING to X:RESULTS",
				"X4 = X3.replace(X1, X2)",
				"X1=old\nX2=new\nX3=\"My old hat is very old\"\nX4=output");
		assertProposalsSize(1);
		checkEntry(0, "RESULTS",
				"RESULTS = output.replace('\\\\', ',')\n\n",
				expectedReplacementOffset,
				expectedReplacementLength);
	}
	
	@Test
	public void testReplaceXWithXInOutput() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("replace '\\\\' with ',' in output",
				"replace X:OLD with X:NEW in X:STRING to X:RESULTS",
				"X4 = X3.replace(X1, X2)",
				"X1=old\nX2=new\nX3=\"My old hat is very old\"\nX4=output");
		assertProposalsSize(1);
		checkEntry(0, "output to RESULTS",
				"output = output.replace('\\\\', ',')\n\n",
				expectedReplacementOffset,
				expectedReplacementLength);
	}
	
	@Test
	public void testReplaceXWithXInOutputSpace() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("replace '\\\\' with ',' in output",
				"replace X:OLD with X:NEW in X:STRING to X:RESULTS",
				"X4 = X3.replace(X1, X2)",
				"X1=old\nX2=new\nX3=\"My old hat is very old\"\nX4=output");
		assertProposalsSize(1);
		checkEntry(0, "output to RESULTS",
				"output = output.replace('\\\\', ',')\n\n",
				expectedReplacementOffset,
				expectedReplacementLength);
	}
	
	@Test
	public void testListEmptyFirst() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 6;
		addEntry("is", 
				"is (,string ,text )here", "here");
		assertProposalsSize(3);
		checkEntry(0, "is here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
		checkEntry(1, "is string here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
		checkEntry(2, "is text here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}	
	
	@Test
	public void testListEmptyLast() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 6;
		addEntry("is", 
				"is (string ,text ,)here", "here");
		assertProposalsSize(3);
		checkEntry(0, "is here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
		checkEntry(1, "is string here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
		checkEntry(2, "is text here", 
				"here\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}	

	@Test
	public void testCompareTextToText() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 6;
		addEntry("compare string get to go",
				"compare (string,text) X:TEXT (to,with) X:TEXT", "X1>X2-X2<X1",
				"X1=x1_default\nX2=x2_default");
		assertProposalsSize(2);
		checkEntry(0, "go",
				"get>go-go<get\n\n",
				expectedReplacementOffset, expectedReplacementLength);
//		checkEntry(1, "go with TEXT", 
//				"                         \nget to go>x2_default-x2_default<get to go\n\n",
//				expectedReplacementOffset, expectedReplacementLength);
	}
	
	@Test
	public void testSayX() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("say HELLO and go", "say X and go to the next line",
				"print \"X1\"");
		assertProposalsSize(1);
		checkEntry(0, "go to the next line",
				"print \"HELLO\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}

	@Test
	public void testSayWORDComma() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("say HELLO and go", "say X:WORD and go to the next line",
				"print \"X1\"");
		assertProposalsSize(1);
		checkEntry(0, "go to the next line",
				"print \"HELLO\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}

	@Test
	public void testSayX2() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 11;
		addEntry("say HELLO", "say X and go to the next line", "print \"X1\"");
		assertProposalsSize(1);
		checkEntry(0, "HELLO and go to the next line",
				"print \"HELLO\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}

	@Test
	public void testSay() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 12;
		addEntry("say a line", "say a line of text", "print(\"text\")");
		assertProposalsSize(1);
		checkEntry(0, "line of text", "print(\"text\")\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testSayXSOMETHING1() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 5;
		addEntry("say", "say X:SOMETHING and go to the next line",
				"print \"X1\"");
		assertProposalsSize(1);
		checkEntry(0, "say SOMETHING and go to the next line",
				"print \"X1\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}


	@Test
	public void testOpenFileDefault() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 11;
		addEntry("open file", "open file X:FILE as X:NAME",
				"print \"X1\"", "X1=test");
		assertProposalsSize(1);
		checkEntry(0, "file FILE as NAME",
				"print \"test\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}

	
	@Test
	public void testSayXSOMETHING2() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		addEntry("say HELLO and go", "say X:SOMETHING and go to the next line",
				"print \"X1\"");
		assertProposalsSize(1);
		checkEntry(0, "go to the next line",
				"print \"HELLO\"\n\n", expectedReplacementOffset,
				expectedReplacementLength);
	}

	@Test
	public void testAskXtoX() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 21;
		addEntry("ask \"Name?\" to name", "ask X to X variable",
				"X2 = raw_input(X1)");
		assertProposalsSize(1);
		checkEntry(0, "name variable",
				"name = raw_input(\"Name?\")\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testAskQUESTIONtoVARIABLE() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 21;
		addEntry("ask \"Name?\" to name", "ask X:QUESTION to X:VARIABLE",
				"X2 = raw_input(X1)");
		assertProposalsSize(1);
		checkEntry(0, "name",
				"name = raw_input(\"Name?\")\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testAskXtoXVariable() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 30;
		addEntry("ask \"Name?\" to name variable", "ask X to X variable",
				"X2 = raw_input(X1)");
		assertProposalsSize(1);
		checkEntry(0, "variable",
				"name = raw_input(\"Name?\")\n\n",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testIfXThenXElseXDone() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 45;
		addEntry("if x>0 then print \">\" else print \"not\" done",
				"if X then X else X done", "if X1:\n\tX2\nelse:\n\tX3");
		assertProposalsSize(1);
		checkEntry(
				0,
				// "\"not\" done",
				"done",
				"if x>0:\n\tprint \">\"\nelse:\n\tprint \"not\"\n\t\n\t",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testIfXThenXElseX() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 40;
		addEntry("if x>0 then print \">\" else print \"not\"",
				"if X then X else X", "if X1:\n\tX2\nelse:\n\tX3");
		assertProposalsSize(1);
		checkEntry(
				0,
				"\"not\"",
				"if x>0:\n\tprint \">\"\nelse:\n\tprint \"not\"\n\t\n\t",
				expectedReplacementOffset, expectedReplacementLength);
	}

	@Test
	public void testIfXThenXElseXDefaults() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 4;
		addEntry("if",
				"if X then X else X", "if X1:\n\tX2\nelse:\n\tX3",
				"X1=x>0\nX2=print \">\"\nX3=print \"not\"");
		assertProposalsSize(1);
		checkEntry(
				0,
				"if X then X else X",
				"if x>0:\n\tprint \">\"\nelse:\n\tprint \"not\"\n\t\n\t",
				expectedReplacementOffset, expectedReplacementLength);
	}

//	@Test
//	public void testIfXThenXElifX() {
//		int expectedReplacementOffset = 0;
//		int expectedReplacementLength = 50;
//		addEntry("if x>0 then print \">\" else if x<0 then print \"<\"",
//				"if X then X( else if X then X)*",
//				"if X1:\n\tX2\n(elif X3:\n\tX4)*");
//		assertProposalsSize(2);
//		checkEntry(
//				1,
//				"\"<\"",
//				"if x>0:\n\tprint \">\"\nelif x<0:\n\tprint \"<\"\n\t\n\t",
//				expectedReplacementOffset, expectedReplacementLength);
//	}
//
//	@Test
//	public void testIfXThenXElseXQuestionMark1() {
//		int expectedReplacementOffset = 0;
//		int expectedReplacementLength = 23;
//		addEntry("if x>0 then print \">\"",
//				"if X then X( else X)?",
//				"if X1:\n\tX2\n(else:\n\tX3)?");
//		assertProposalsSize(2);
//		checkEntry(0, "\">\"",
//				"if x>0:\n\tprint \">\"\n\t\n\t",
//				expectedReplacementOffset, expectedReplacementLength);
//		checkEntry(1, "\">\" else X",
//				"if x>0:\n\tprint \">\"\nelse:\n\tX3\n\t\n\t",
//				expectedReplacementOffset, expectedReplacementLength);
//	}
//
//	@Test
//	public void testIfXThenXElseXQuestionMark2() {
//		int expectedReplacementOffset = 0;
//		int expectedReplacementLength = 40;
//		addEntry("if x>0 then print \">\" else print \"not\"",
//				"if X then X( else X)?", "if X1:\n\tX2\n(else:\n\tX3)?");
//		assertProposalsSize(2);
//		checkEntry(
//				1,
//				"\"not\"",
//				"if x>0:\n\tprint \">\"\nelse:\n\tprint \"not\"\n\t\n\t",
//				expectedReplacementOffset, expectedReplacementLength);
//	}

	// @Test
	// public void testIfXThenXElseXStar1() {
	// int expectedReplacementOffset = 0;
	// int expectedReplacementLength = 23;
	// addEntry("if x>0 then print \">\"",
	// "if X then X(( else if X then X)* else X)?",
	// "if X1:\n\tX2\n((\nelif X1*:\n\tX2*)*\nelse:\n\tX3)?");
	// checkEntry(0, "\">\"",
	// "if x>0:\n\tprint \">\"\n                     \n",
	// expectedReplacementOffset, expectedReplacementLength);
	// checkEntry(1, "\">\" else X",
	// "if x>0:\n\tprint \">\"\nelse:\n\tX3                     \n",
	// expectedReplacementOffset, expectedReplacementLength);
	// }

	@Test
	public void testSayY() {
		int expectedReplacementOffset = 0;
		int expectedReplacementLength = 18;
		boolean match = false;
		addEntry("say HELLO and go", "say Y and go to the next line",
				"print \"X1\"");
		assertProposalsSize(0);
		checkEntry(0, "go to the next line", "print \"HELLO\", ",
				expectedReplacementOffset, expectedReplacementLength, match);
	}

	public void addEntry(String typed, String template,
			String templateReplacementString) {
		addEntry( typed,  template,
				 templateReplacementString, "");
	}
	
	public void addEntry(String typed, String template,
			String templateReplacementString, String defaults) {
		String language = "english";
		String displayString = template;
		try {
			// String replacementString =
			// " //\\ //// code completion entries (1+ lines):...";
			String additionalProposalInfo = " //\\ //// code completion entries (1+ lines):...";
			String message = "Make needed changes then save template to appropriately named .txt file.";
			computer.testingLine = typed;
			computer.addEntry(language, displayString, typed,
					templateReplacementString, additionalProposalInfo, message,
					defaults, proposals);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void checkEntry(int entry, String expectedOutput,
			String expectedReplacementString, int expectedReplacementOffset,
			int expectedReplacementLength) {
		boolean match = true; // default
		checkEntry(entry, expectedOutput, expectedReplacementString,
				expectedReplacementOffset, expectedReplacementLength, match);
	}

	public void checkEntry(int entry, String expectedOutput,
			String expectedReplacementString, int expectedReplacementOffset,
			int expectedReplacementLength, Boolean match) {
		if (proposals.size() == 0 && match == false) {
			return;
		}
		try {

			Iterator it = proposals.values().iterator();
			Object proposal = null;
			for( int i = 0; i<=entry; i++) {
				if (it.hasNext()) {
					proposal = it.next();
				}
			}
//			boolean isInstance = proposal instanceof JavaCompletionProposal;
			boolean isInstance = true;
			if (isInstance && proposal != null) {
//				assertEquals("ReplacementOffsets must match",
//						expectedReplacementOffset,
//						((JavaCompletionProposal) proposal)
//								.getReplacementOffset());

				Class<?> myClass = proposal.getClass();
				Field myField = getField(myClass, "fReplacementLength");
				myField.setAccessible(true); // required if field is not
												// normally accessible

				System.out.println("proposal.fReplacementLength: "
						+ myField.get(proposal));
//				assertEquals("ReplacementLengths must match",
//				// expectedReplacementLength,
//				// actualReplacementString.length());
//						expectedReplacementLength, myField.get(proposal));

				myField = getField(myClass, "fReplacementString");
				myField.setAccessible(true); // required if field is not
												// normally accessible
				System.out.println("proposal.fReplacementString: "
						+ myField.get(proposal));
				String actualReplacementString = (String) myField.get(proposal);
				assertEquals("ReplacementStrings must match",
						expectedReplacementString, actualReplacementString);

			}
			assertEquals("Display strings must match", expectedOutput,
					((ICompletionProposal)proposal).getDisplayString());
			System.out.println("Proposals Computer.");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Field getField(Class<?> clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	private void assertProposalsSize(int i) {
		assertEquals("Number of proposals must match", i, proposals.size());
	}

}