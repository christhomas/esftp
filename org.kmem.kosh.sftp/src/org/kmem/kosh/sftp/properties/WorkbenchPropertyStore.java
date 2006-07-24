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

import org.kmem.kosh.sftp.SftpPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class WorkbenchPropertyStore extends PropertyStore {
	protected IPreferenceStore m_wbench;
	
	/**	The main constructor
	 * 
	 * 	This set's the project, unique identifier and workbench preference store to default to
	 * 
	 * @param project	The project currently being viewed
	 * @param pageID	The unique identifier for this project
	 * @param workbench	The workbench preference store to default values to
	 */	
	public WorkbenchPropertyStore() {
		m_wbench = SftpPlugin.getDefault().getPreferenceStore();
	}
	
	/*	Preference value manipulation methods */
	/**
	 * This method restores all the values to what is held in the workbench preferences
	 */
	public void restoreDefaults(){
		setValue(IProperty.SERVER, m_wbench.getDefaultString(IProperty.SERVER));
		setValue(IProperty.PORT, m_wbench.getDefaultInt(IProperty.PORT));
		setValue(IProperty.TIMEOUT, m_wbench.getDefaultInt(IProperty.TIMEOUT));
		setValue(IProperty.USERNAME, m_wbench.getDefaultString(IProperty.USERNAME));
		setValue(IProperty.PASSWORD, m_wbench.getDefaultString(IProperty.PASSWORD));
		setValue(IProperty.SAVEPWD, m_wbench.getDefaultBoolean(IProperty.SAVEPWD));
		setValue(IProperty.RECURSE, m_wbench.getDefaultBoolean(IProperty.RECURSE));
		setValue(IProperty.EMPTY, m_wbench.getDefaultBoolean(IProperty.EMPTY));
		setValue(IProperty.SITEROOT, m_wbench.getDefaultString(IProperty.SITEROOT));
	}		

	/* Get Methods  */
	/**	Obtain the preference, if the store doesnt contain it, default to the workbench preferences
	 * 
	 * @param name	The preference to obtain or default to
	 */
	public String getString(String name) {
		String str;
		//	Obtain the preference, if not contains() then default to workbench
		if(contains(name)) str = super.getString(name);
		else str = m_wbench.getString(name);
		if(str == null) str = "";
		
		return str;
	}

	/**	Obtain the preference, if the store doesnt contain it, default to the workbench preferences
	 * 
	 * @param name	The preference to obtain or default to
	 */
	public int getInt(String name) {
		//	Obtain the preference, if not contains() then default to workbench
		if(contains(name)) return super.getInt(name);
		
		return m_wbench.getInt(name);
	}
	
	/**	Obtain the preference, if the store doesnt contain it, default to the workbench preferences
	 * 
	 * @param name	The preference to obtain or default to
	 */
	public boolean getBoolean(String name){
		if(contains(name))return super.getBoolean(name);
		
		return m_wbench.getBoolean(name);
	}
	
	/*	Set methods */
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, int  value){
		m_wbench.setValue(name,Integer.toString(value));
	}
	
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, boolean value){
		m_wbench.setValue(name,Boolean.toString(value));
	}
	
	/**	Set the value of the preference, but only if it differs from the workbench preferences, then nothing will happen.
	 * 
	 * @param name	The preference to set
	 * @param value	The value to set it to
	 */
	public void setValue(String name, String value){
		m_wbench.setValue(name,value);
	}
	
	/**	Set a preference to the default value held in the workbench preferences
	 * 
	 * @param name	The preference to reset to defaults
	 */
	public void setToDefault(String name){
		setValue(name, getDefaultString(name));
	}
	
	/**	Tests whether the value being held, is the default value (the one in the workbench preferences)
	 * 
	 * @param name	The value to test against Default
	 */
	public boolean isDefault(String name){
		String defaultString = getDefaultString(name);
		if(defaultString == null) return false;
		
		return defaultString.equals(getString(name));
	}
	
	/**	Returns the default value for the preference
	 * 
	 * @param name	The default preference being requested
	 */
	public String getDefaultString(String name){
		return m_wbench.getDefaultString(name);
	}
	
	/**	Returns the default value for the preference
	 * 
	 * @param name	The default preference being requested
	 */
	public int getDefaultInt(String name){
		return m_wbench.getDefaultInt(name);
	}
	
	/**	Returns the default value for the preference
	 * 
	 * @param name	The default preference being requested
	 */
	public boolean getDefaultBoolean(String name){
		return m_wbench.getDefaultBoolean(name);
	}
}
