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

package com.antimatterstudios.esftp;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import com.antimatterstudios.esftp.Digest;
import com.antimatterstudios.esftp.properties.*;

public class TransferDetails {
	protected IProject m_project;
	protected String m_key = null;
	protected PropertyStore m_store;
	
    public  static int GET = 0;
	public  static int PUT = 1;	
	
	/**
	 *	initialise the transfer object with sftp details 
	 *	These either come from the project properties, or the workspace preferences
	 */
	public TransferDetails(IProject project){
		//System.out.println("TRACE-> TransferDetails::TransferDetails(IProject = "+project+")");

		m_project = project;
		
		/*if(m_project instanceof IProject){
			System.out.println("instanceof says: this is a IProject object");
		}else{
			System.out.println("instanceof says: this is NOT a IProject object");
		}*/
		
		//	Obtain site details here		
		m_store = new ProjectPropertyStore(m_project);
	}
	
	/**
	 *	Initialise the transfer object with sftp details 
	 *	These either come from the project properties, or the workspace preferences
	 */	
	public TransferDetails(PropertyStore store){
		//System.out.println("TRACE-> TransferDetails::TransferDetails(PropertyStore), are we testing the sftp server?");
		m_store = store;
	}
	
	
	
	/**	Output debugging information about the data stored in the PropertyStore
	 */
	public void debug(){
		m_store.debug();
	}
	
	/**
	 * Obtains the specific key that can be used to uniquely Identify the server this object is associated with
	 * 
	 * This is so the SftpPlugin object can quickly find an existing Transfer object that might be able to take an 
	 * appended batch of files, therefore prevent the same server from being opened twice in one session.
	 * This increases throughput, since multiple attempts to open the same server are avoided. The transfers are
	 * simply concatenated
	 * 
	 * @param hash	The hash object used to create the key
	 * @return	the key to identify this server
	 */
	public String getKey(){
		//System.out.println("TRACE-> TransferDetails::getKey()");
		//	Cache the key so you only have to do this once per object
		if(m_key == null){
			//System,out.println("creating key");
			//	Obtain the hash object from the SftpPlugin object
			Digest h = Activator.getDefault().getHash();
			
			//	Create a single string from all the data for this server (DONT INCLUDE THE PASSWORD)
			String str = m_store.getString(IProperty.SERVER)+":"+m_store.getInt(IProperty.PORT)+":"+m_store.getString(IProperty.USERNAME)+":";
			
			m_key = h.getDigest(str);
		}//else System.out.println("TransferDetails::getKey(), using cached copy");
		return m_key;
	}
	
	public String getServer(){
		return m_store.getString(IProperty.SERVER);
	}
	
	public int getPort(){
		return m_store.getInt(IProperty.PORT);
	}
	
	public int getTimeout(){
		return m_store.getInt(IProperty.TIMEOUT);
	}
		
	public String getUsername(){
		return m_store.getString(IProperty.USERNAME);
	}
	
	public String getPassword(){
		return m_store.getString(IProperty.PASSWORD);
	}
	
	public boolean getSavePwd(){
		return m_store.getBoolean(IProperty.SAVEPWD);
	}
	
	public boolean getRecurse(){
		return m_store.getBoolean(IProperty.RECURSE);
	}
	
	public boolean getEmptyDirectory(){
		return m_store.getBoolean(IProperty.EMPTY);
	}
	
	public String getLocalRoot(){
		//System.out.println("TRACE-> TransferDetails::getLocalRoot()");
		IPath p = m_project.getLocation();
	
		if(p != null){
			return p.toPortableString()+"/";
		}/*else{
			System.out.println("getLocation(): path is NOT valid");
		}*/

		return "";
	}	
	
	public String getSiteRoot(){
		return m_store.getString(IProperty.SITEROOT);
	}
}
