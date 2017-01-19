package com.github.chameleon.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.CompletionProposal;

public class ChameleonTestCompletionProposalComputer extends ChameleonCompletionProposalComputer {

	@Override
	public DocumentInfo getDocumentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String addExpandedEntry(
			final String language, String displayString,
			String typedString,
			String replacementString, String additionalProposalInfo,
			final String message,
			final String defaults,
			final Map<String, Object> proposals)
			throws IOException, BadLocationException {
		String tempReplacementString = "";
		String returnReplacementString = "";
		if ( language.contains("Intermediate") && typedString == "") {
			System.out.println("Intermediate");
			//For each line... substitute...
			BufferedReader bufReader = new BufferedReader(new StringReader(replacementString));
			String line = null;
			replacementString = "";
			while( (line=bufReader.readLine()) != null){
				returnReplacementString = "";
				Map<String, Object> completions = new HashMap<String, Object>();
				tempReplacementString = getCompletionsFromDirectory(pluginLanguagesDirectory, line, completions);
				if ( returnReplacementString == "") {
					returnReplacementString = tempReplacementString;
				}
				tempReplacementString = getCompletionsFromDirectory(homeLanguagesDirectory, line, completions);
				if ( returnReplacementString == "") {
					returnReplacementString = tempReplacementString;
				}
				if ( returnReplacementString != "" ) {
					replacementString += returnReplacementString+"\n";							
				}
			}
		}
		try {
			DocumentInfo docInfo = new DocumentInfo(testing, testingLine, testingOffset);
			docInfo.typedString = typedString;
			TestCompletionProposalBuilder builder = new TestCompletionProposalBuilder(
					language, displayString, replacementString,
					additionalProposalInfo, message, defaults, proposals, testing,
					testingLine, testingOffset, console, docInfo, typedString);
			if (builder.isMatch()) {
				if ( returnReplacementString == "") {
					returnReplacementString = replacementString;
				}
				CompletionProposal proposal = builder.createProposal();
				proposals.put(displayString, proposal);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return returnReplacementString;
	}

}
