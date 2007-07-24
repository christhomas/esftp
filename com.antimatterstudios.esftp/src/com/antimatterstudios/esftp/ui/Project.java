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

package com.antimatterstudios.esftp.ui;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;

import com.antimatterstudios.esftp.properties.ProjectPreferences;

public class Project extends PropertyPage {
	protected UserInterface m_interface;
	protected ProjectPreferences m_store;
	
	/**
	 * This method initializes 
	 * 
	 */
	public Project() {
		super();
		m_interface= new UserInterface();
	}

	protected Control createContents(Composite parent) { 
		m_store = new ProjectPreferences((IProject)getElement()); // TODO: Move to the constructor
		
		return m_interface.open(parent,m_store);
	}
	
	public boolean performOk() {
		//store all the preferences
		if(m_interface.close() == false){
			//	Open message box informing the user to test these settings before proceeding
			Shell shell = new Shell();
			MessageDialog.openWarning(	
					shell,
					"SFTP Not Verified",
					"The SFTP details are not verified.  It is recommended that you test them before you proceed");
		}
		
		return super.performOk();
	}
	
	public void performDefaults(){
		m_store.restoreDefaults();
		m_interface.updateInterface();
	}
}
