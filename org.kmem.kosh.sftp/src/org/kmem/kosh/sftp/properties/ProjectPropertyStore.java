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

package org.kmem.kosh.sftp.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.kmem.kosh.sftp.SftpPlugin;

public class ProjectPropertyStore extends PropertyStore {
	protected IProject m_project;
	protected String m_pageID;
	protected IPreferenceStore m_wbench;

	public ProjectPropertyStore(IProject project){
		//System.out.println("TRACE-> ProjectPropertyStore::ProjectPropertyStore(IProject)");
		
		m_wbench = SftpPlugin.getDefault().getPreferenceStore();

		m_project = project;
		m_pageID = m_project.getName();
		
		//	Restore any previously set values to the page
		setValue(IProperty.VERIFIED, true);
		restoreValue(IProperty.SERVER);
		restoreValue(IProperty.PORT);
		restoreValue(IProperty.TIMEOUT);
		restoreValue(IProperty.USERNAME);
		restoreValue(IProperty.PASSWORD);
		restoreValue(IProperty.SAVEPWD);
		restoreValue(IProperty.RECURSE);
		restoreValue(IProperty.EMPTY);
		restoreValue(IProperty.SITEROOT);		
	}
	
	/**
	 * Restores all the preferences that are being stored in the project resources
	 * @param name the name of the preference to restore
	 */
	private void restoreValue(String name){
		try{
			//	Obtain the preference from the project store
			String value = m_project.getPersistentProperty(new QualifiedName(m_pageID,name));
			
			//	Couldnt find the preference (or it's invalidly set to null), so default to the workbench preferences
			if(value == null) 	value = getDefaultString(name);

			//	Found the value, push it through saveValue() which will put it in the appropriate places
			saveValue(name,value);
			
		}catch(CoreException e){
			System.out.println("Cannot find persistent property, failure or doesn't exist");
		}
	}
	
	/**
	 * Stores the preference in the resource preference store (persists across sessions)
	 * 
	 * @param name	The name of the preference to save
	 * @param value	The value of the preference to save
	 */
	private void saveValue(String name, String value){
		try{
			//	Set the preference into the project preference store and make sure this object remembers it to (setValue call)
			m_project.setPersistentProperty(new QualifiedName(m_pageID, name), value);
			super.setValue(name,value);
		}catch(CoreException e){
			System.out.println("Cannot set persistent property, failure or doesn't exist");
		}
	}
	
	/*	Set methods */
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, int  value){
		saveValue(name,Integer.toString(value));
	}
	
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, boolean value){
		saveValue(name,Boolean.toString(value));
	}
	
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, String value){
		saveValue(name,value);
	}	
	
	/**
	 * This method restores all the values to what is held in the workbench preferences
	 */
	public void restoreDefaults(){
		setValue(IProperty.SERVER, m_wbench.getString(IProperty.SERVER));
		setValue(IProperty.PORT, m_wbench.getInt(IProperty.PORT));
		setValue(IProperty.TIMEOUT, m_wbench.getInt(IProperty.TIMEOUT));
		setValue(IProperty.USERNAME, m_wbench.getString(IProperty.USERNAME));
		setValue(IProperty.PASSWORD, m_wbench.getString(IProperty.PASSWORD));
		setValue(IProperty.SAVEPWD, m_wbench.getBoolean(IProperty.SAVEPWD));
		setValue(IProperty.RECURSE, m_wbench.getBoolean(IProperty.RECURSE));
		setValue(IProperty.EMPTY, m_wbench.getBoolean(IProperty.EMPTY));
		setValue(IProperty.SITEROOT, m_wbench.getString(IProperty.SITEROOT));
	}		
}
