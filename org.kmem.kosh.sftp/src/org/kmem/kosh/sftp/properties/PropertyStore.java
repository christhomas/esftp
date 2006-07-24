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

import org.eclipse.jface.preference.PreferenceStore;

public class PropertyStore extends PreferenceStore {
	/**	Default constructor
	 * 
	 */
	public PropertyStore(){}
	
	/**	The clone constructor
	 * 
	 *	This constructor is used for simply creating a property store and cloning another
	 *	Don't use it for any other reason, it'll crash your app (no m_wbench fallback means null pointer exception)
	 *
	 * @param store	The store to clone into this copy
	 */
	public PropertyStore(PropertyStore store){
		clone(store);
	}
	
	public void debug(){
		System.out.println("PropertyStore::debug()");
		System.out.println(IProperty.SERVER + "=" + getString(IProperty.SERVER));
		System.out.println(IProperty.PORT + "=" + getInt(IProperty.PORT));
		System.out.println(IProperty.TIMEOUT + "= " + getInt(IProperty.TIMEOUT));
		System.out.println(IProperty.USERNAME + "=" + getString(IProperty.USERNAME));
		System.out.println(IProperty.PASSWORD + "=" + getString(IProperty.PASSWORD));
		System.out.println(IProperty.SAVEPWD + "=" + getBoolean(IProperty.SAVEPWD));
		System.out.println(IProperty.RECURSE + "=" + getBoolean(IProperty.RECURSE));
		System.out.println(IProperty.EMPTY + "=" + getBoolean(IProperty.EMPTY));
		System.out.println(IProperty.SITEROOT + "=" + getString(IProperty.SITEROOT));
	}
	
	/**	Backup a property store into a clone object
	 * 
	 * @param store	The store to backup
	 */
	public void clone(PropertyStore store){
		setValue(IProperty.VERIFIED, store.getString(IProperty.VERIFIED));
		setValue(IProperty.SERVER, store.getString(IProperty.SERVER));
		setValue(IProperty.PORT, store.getInt(IProperty.PORT));
		setValue(IProperty.USERNAME, store.getString(IProperty.USERNAME));
		setValue(IProperty.TIMEOUT, store.getInt(IProperty.TIMEOUT));
		setValue(IProperty.PASSWORD, store.getString(IProperty.PASSWORD));
		setValue(IProperty.SAVEPWD, store.getBoolean(IProperty.SAVEPWD));
		setValue(IProperty.RECURSE, store.getBoolean(IProperty.RECURSE));
		setValue(IProperty.EMPTY, store.getBoolean(IProperty.EMPTY));
		setValue(IProperty.SITEROOT, store.getString(IProperty.SITEROOT));
	}
}
