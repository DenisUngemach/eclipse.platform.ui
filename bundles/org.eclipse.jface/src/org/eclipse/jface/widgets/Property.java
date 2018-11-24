/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import org.eclipse.swt.widgets.Control;

/**
 * Class which implement this interface can apply a property (like a text,
 * enabled, image, ...) to the given control.
 *
 * @param <T>
 *
 */
@FunctionalInterface
public interface Property<T extends Control> {

	/**
	 * Called when the control is created and the property should be applied.
	 * 
	 * @param control
	 */
	void apply(T control);
}