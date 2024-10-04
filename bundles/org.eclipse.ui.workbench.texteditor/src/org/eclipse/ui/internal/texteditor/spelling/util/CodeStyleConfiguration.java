/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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

package org.eclipse.ui.internal.texteditor.spelling.util;

import java.util.regex.Pattern;


/**
 * Gives access to the import rewrite configured with the settings as specified in the user interface.
 * These settings are kept in JDT UI for compatibility reasons.
 *
 * <p>
 * This class is not intended to be subclassed or instantiated by clients.
 * </p>
 * @since 1.10
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CodeStyleConfiguration {

	private static final Pattern SEMICOLON_PATTERN= Pattern.compile(";"); //$NON-NLS-1$

	/**
	 * A named preference that holds a list of semicolon separated package names. The list specifies the import order used by
	 * the "Organize Imports" operation.
	 * <p>
	 * Value is of type <code>String</code>: semicolon separated list of package
	 * names
	 * </p>
	 */
	public static final String ORGIMPORTS_IMPORTORDER= "org.eclipse.jdt.ui.importorder"; //$NON-NLS-1$

	/**
	 * A named preference that specifies the number of imports added before a star-import declaration is used.
	 * <p>
	 * Value is of type <code>Integer</code>: positive value specifying the number of non star-import is used
	 * </p>
	 */
	public static final String ORGIMPORTS_ONDEMANDTHRESHOLD= "org.eclipse.jdt.ui.ondemandthreshold"; //$NON-NLS-1$

	/**
	 * A named preference that specifies the number of static imports added before a star-import declaration is used.
	 * <p>
	 * Value is of type <code>Integer</code>: positive value specifying the number of non star-import is used
	 * </p>
	 */
	public static final String ORGIMPORTS_STATIC_ONDEMANDTHRESHOLD= "org.eclipse.jdt.ui.staticondemandthreshold"; //$NON-NLS-1$

	private CodeStyleConfiguration() {
		// do not instantiate and subclass
	}




}
