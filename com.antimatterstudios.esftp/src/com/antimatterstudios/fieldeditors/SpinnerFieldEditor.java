/*******************************************************************************
 * Copyright (c) {06/11/2005} {Christopher Thomas} 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contributors:
 *    {Christopher Thomas} - initial API and implementation
 *    chris.alex.thomas@gmail.com
 *******************************************************************************/

package com.antimatterstudios.fieldeditors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Spinner;

/**
 * A field editor for displaying Spinners not associated with other widgets.
 */
public class SpinnerFieldEditor extends FieldEditor {
	private Spinner m_spinner;
	private int m_min;
	private int m_max;
	private boolean m_fill;
	
	// All labels can use the same preference name since they don't
	// store any preference.
	public SpinnerFieldEditor(String name, String value, int min, int max, boolean fill, Composite parent) {
		super(name, value, parent);
		m_min = min;
		m_max = max;
		m_fill = fill;
	}

	// Adjusts the field editor to be displayed correctly
	// for the given number of columns.
	protected void adjustForNumColumns(int numColumns) {
		((GridData) m_spinner.getLayoutData()).horizontalSpan = numColumns-1;
	}

	// Fills the field editor's controls into the given parent.
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		//	Create label
		getLabelControl(parent);
		//	Create spinner
		m_spinner = new Spinner(parent, SWT.BORDER);
		m_spinner.setMinimum(m_min);
		m_spinner.setMaximum(m_max);		
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = numColumns;
		if(m_fill == true){
			gridData.horizontalAlignment = GridData.FILL;
		}
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessVerticalSpace = false;
		
		m_spinner.setLayoutData(gridData);
	}

	// Returns the number of controls in the field editor.
	public int getNumberOfControls() {
		return 2;
	}

	// Labels do not persist any preferences, so these methods are empty.
	protected void doLoad() {
		int value = getPreferenceStore().getInt(getPreferenceName());
		m_spinner.setSelection(value);
	}
	protected void doLoadDefault() {
		int value = getPreferenceStore().getDefaultInt(getPreferenceName());
		m_spinner.setSelection(value);
	}
	protected void doStore() {
		int value = m_spinner.getSelection();
		getPreferenceStore().setValue(getPreferenceName(), value);
	}
}
