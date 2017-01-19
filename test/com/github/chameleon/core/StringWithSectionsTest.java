/**
 * 
 */
package com.github.chameleon.core;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.chameleon.core.StringWithSections;

/**
 * @author JESSE
 *
 */
public class StringWithSectionsTest {

	String DELIMITER = ",";
	
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

	public StringWithSections createStringWithSections(String in) {
		return createStringWithSections(in, DELIMITER);
	}

	public StringWithSections createStringWithSections(String in, String delimiter) {
		return new StringWithSections(in, delimiter);
	}

	@Test
	public void testNone() {
		StringWithSections sws = createStringWithSections("This is a test");
		String expectedString = "This is a test";
		assertEquals("No original for sections",
				expectedString, sws.getExpandedStrings().get(0));
	}
	
	@Test
	public void testSimple() {
		StringWithSections sws = createStringWithSections("This (is,was) a test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is a test");
		expectedList.add("This was a test");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
/*
	@Test
	public void testSimpleStar2() {
		StringWithSections sws = createStringWithSections("This (is,was)* a test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is ...");
		expectedList.add("This was ...");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
	
	@Test
	public void testSimpleStar3() {
		StringWithSections sws = createStringWithSections("This (is,was,will be)* a test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is ...");
		expectedList.add("This was ...");
		expectedList.add("This will be ...");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}	
*/
	@Test
	public void testComplex() {
		StringWithSections sws = createStringWithSections("(create,open) (,a )file X:FILE as X:NAME to (write,append) to");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("create file X:FILE as X:NAME to write to");
		expectedList.add("create file X:FILE as X:NAME to append to");
		expectedList.add("create a file X:FILE as X:NAME to write to");
		expectedList.add("create a file X:FILE as X:NAME to append to");
		expectedList.add("open file X:FILE as X:NAME to write to");
		expectedList.add("open file X:FILE as X:NAME to append to");
		expectedList.add("open a file X:FILE as X:NAME to write to");
		expectedList.add("open a file X:FILE as X:NAME to append to");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
	
	
	@Test
	public void testTwoGroups() {
		StringWithSections sws = createStringWithSections("This (is,was) (a,an) test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is a test");
		expectedList.add("This is an test");
		expectedList.add("This was a test");
		expectedList.add("This was an test");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
	
	@Test
	public void testOneSection() {
		StringWithSections sws = createStringWithSections("This is a (big) test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is a (big) test");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
	
	@Test
	public void testTwoGroupsOneSection() {
		StringWithSections sws = createStringWithSections("This (is,was) a (big) test");
		List<String> expectedList = new LinkedList<String>();
		expectedList.add("This is a (big) test");
		expectedList.add("This was a (big) test");
		assertEquals("No original for sections",
				expectedList, sws.getExpandedStrings());
	}
	
}
