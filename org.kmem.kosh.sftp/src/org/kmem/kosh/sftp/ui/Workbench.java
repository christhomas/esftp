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

package org.kmem.kosh.sftp.ui;

import org.kmem.kosh.sftp.properties.WorkbenchPropertyStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;

public class Workbench
	extends PreferencePage
	implements IWorkbenchPreferencePage{
	
	private UserInterface m_interface;
	private WorkbenchPropertyStore m_store;
		
	public Workbench(){	
		super();
		
		setDescription("ESftp plug-in preferences");
		System.out.println("Workbench::Workbench()");
	}
			
	protected Control createContents(Composite parent){
		m_interface = new UserInterface();
		m_store = new WorkbenchPropertyStore();
		return m_interface.open(parent,m_store);
	}	
	
	public void init(IWorkbench workbench) {	}	
	
	public boolean performOk(){
		//System.out.println("Workbench::performOk()");
		
		//	store all the preferences
		if(m_interface.close(m_store) == false){
			//	Open message box informing the user to test these settings before proceeding
			Shell shell = new Shell();
			MessageDialog.openWarning(shell,
					"SFTP Not Verified","The SFTP details are not verified.  It is recommended that you test them before you proceed");
		}
		
		return super.performOk();		
	}
	
	public void performDefaults(){
		m_store.restoreDefaults();
		m_interface.update();
	}
	
	public boolean performCancel(){
		performDefaults();
		return super.performCancel();
	}	
}