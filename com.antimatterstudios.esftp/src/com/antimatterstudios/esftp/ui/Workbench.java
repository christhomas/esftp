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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;

import com.antimatterstudios.esftp.properties.WorkspacePreferences;

public class Workbench 
	extends PreferencePage
	implements IWorkbenchPreferencePage{
	
	protected UserInterface m_interface;
	protected WorkspacePreferences m_store;
		
	public Workbench()
	{	
		super();
		
		setDescription("ESftp plug-in preferences");
		System.out.println("Workbench::Workbench()");
	}
			
	protected Control createContents(Composite parent)
	{
		m_interface = new UserInterface(); // TODO: Move this to the constructor
		m_store = new WorkspacePreferences(); // TODO: Move this to the constructor also?
		
		return m_interface.open(parent, m_store);
	}	
	
	public void init(IWorkbench workbench){}
	
	public boolean performOk()
	{
		//	store all the preferences
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
	
	public void performDefaults()
	{
		m_store.restoreDefaults();
		m_interface.updateInterface();
	}
}