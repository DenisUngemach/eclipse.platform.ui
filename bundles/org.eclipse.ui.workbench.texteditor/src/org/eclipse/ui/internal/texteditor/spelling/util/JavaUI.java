/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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

/**
 * Central access point for the Java UI plug-in (id <code>"org.eclipse.jdt.ui"</code>).
 * This class provides static methods for:
 * <ul>
 *  <li> creating various kinds of selection dialogs to present a collection
 *       of Java elements to the user and let them make a selection.</li>
 *  <li> opening a Java editor on a compilation unit.</li>
 * </ul>
 * <p>
 * This class provides static methods and fields only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class JavaUI {


	/**
	 * The id of the Java plug-in (value <code>"org.eclipse.jdt.ui"</code>).
	 */
	public static final String ID_PLUGIN= "org.eclipse.jdt.ui"; //$NON-NLS-1$

}
