package com.github.chameleon.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

/**
 * Created by JESSE on 9/27/2016.
 */
public class OpenProposal {
    public String fDisplayString;
    public String fReplacementString;
    public int fReplacementOffset;
    public int fReplacementLength;
    public int fCursorPosition;
    public Image fImage;
    public IContextInformation fContextInformation;
    public String fAdditionalProposalInfo;

    public OpenProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isTrue(cursorPosition >= 0);
        this.fReplacementString = replacementString;
        this.fReplacementOffset = replacementOffset;
        this.fReplacementLength = replacementLength;
        this.fCursorPosition = cursorPosition;
        this.fImage = image;
        this.fDisplayString = displayString;
        this.fContextInformation = contextInformation;
        this.fAdditionalProposalInfo = additionalProposalInfo;
    }

    public CompletionProposal getCompletionProposal() {
        return new CompletionProposal(fReplacementString,
                fReplacementOffset, fReplacementLength,
                fCursorPosition,
                fImage, fDisplayString, fContextInformation,
                fAdditionalProposalInfo);
    }
}
