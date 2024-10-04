/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.spelling.impl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.ui.internal.texteditor.spelling.util.CategorizedProblem;

//import org.eclipse.jdt.core.compiler.CategorizedProblem;

/**
 * Spelling problem to be accepted by problem requesters.
 *
 * @since 3.1
 */
public class CoreSpellingProblem extends CategorizedProblem {

	// spelling 'marker type' name. Only virtual as spelling problems are never persisted in markers.
	// marker type is used in the quickFixProcessor extension point
	public static final String MARKER_TYPE= "org.eclipse.jdt.ui.internal.spelling"; //$NON-NLS-1$

	/** The end offset of the problem */
	private int fSourceEnd= 0;

	/** The line number of the problem */
	private int fLineNumber= 1;

	/** The start offset of the problem */
	private int fSourceStart= 0;

	/** The description of the problem */
	private String fMessage;

	/** The misspelled word */
	private String fWord;

	/** Was the word found in the dictionary? */
	private boolean fMatch;

	/** Does the word start a new sentence? */
	private boolean fSentence;

	/** The associated document */
	private IDocument fDocument;

	/** The originating file name */
	private String fOrigin;

	/**
	 * Initialize with the given parameters.
	 *
	 * @param start the start offset
	 * @param end the end offset
	 * @param line the line
	 * @param message the message
	 * @param word the word
	 * @param match <code>true</code> iff the word was found in the dictionary
	 * @param sentence <code>true</code> iff the word starts a sentence
	 * @param document the document
	 * @param origin the originating file name
	 */
	public CoreSpellingProblem(int start, int end, int line, String message, String word, boolean match, boolean sentence, IDocument document, String origin) {
		super();
		fSourceStart= start;
		fSourceEnd= end;
		fLineNumber= line;
		fMessage= message;
		fWord= word;
		fMatch= match;
		fSentence= sentence;
		fDocument= document;
		fOrigin= origin;
	}
	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getArguments()
	 */
	@Override
	public String[] getArguments() {

		String prefix= ""; //$NON-NLS-1$
		String postfix= ""; //$NON-NLS-1$

		try {

			IRegion line= fDocument.getLineInformationOfOffset(fSourceStart);
			prefix= fDocument.get(line.getOffset(), fSourceStart - line.getOffset());
			int postfixStart= fSourceEnd + 1;
			postfix= fDocument.get(postfixStart, line.getOffset() + line.getLength() - postfixStart);

		} catch (BadLocationException exception) {
			// Do nothing
		}
		return new String[] { fWord, prefix, postfix, fSentence ? Boolean.toString(true) : Boolean.toString(false), fMatch ? Boolean.toString(true) : Boolean.toString(false) };
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getID()
	 */
	@Override
	public int getID() {
		return JavaSpellingReconcileStrategy.SPELLING_PROBLEM_ID;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getMessage()
	 */
	@Override
	public String getMessage() {
		return fMessage;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getOriginatingFileName()
	 */
	@Override
	public char[] getOriginatingFileName() {
		return fOrigin.toCharArray();
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceEnd()
	 */
	@Override
	public int getSourceEnd() {
		return fSourceEnd;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceLineNumber()
	 */
	@Override
	public int getSourceLineNumber() {
		return fLineNumber;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceStart()
	 */
	@Override
	public int getSourceStart() {
		return fSourceStart;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#isError()
	 */
	@Override
	public boolean isError() {
		return false;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#isWarning()
	 */
	@Override
	public boolean isWarning() {
		return true;
	}

	@Override
	public boolean isInfo() {
		return false;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceStart(int)
	 */
	@Override
	public void setSourceStart(int sourceStart) {
		fSourceStart= sourceStart;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceEnd(int)
	 */
	@Override
	public void setSourceEnd(int sourceEnd) {
		fSourceEnd= sourceEnd;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceLineNumber(int)
	 */
	@Override
	public void setSourceLineNumber(int lineNumber) {
		fLineNumber= lineNumber;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.CategorizedProblem#getCategoryID()
	 */
	@Override
	public int getCategoryID() {
		return CAT_JAVADOC;
	}

	/*
	 * @see org.eclipse.jdt.core.compiler.CategorizedProblem#getMarkerType()
	 */
	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}
}
