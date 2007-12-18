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

package com.antimatterstudios.esftp.directory;

import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import com.antimatterstudios.esftp.Transfer;

public class RemoteDirectory extends Directory
{
	protected boolean m_open;
	
	public RemoteDirectory(Transfer t)
	{
		//System.out.println("TRACE-> RemoteDirectory::RemoteDirectory()");
		m_size = 0;
		m_transfer = t;
		m_details = m_transfer.getTransferDetails();
	}	
	
	public boolean process(Object item, Vector files, Vector folders)
	{
		System.out.println("RD::process(object, v, v");
		IContainer c = (IContainer)item;
		String directory = c.getLocation().toPortableString();
		directory = makeRelative(directory, m_details.getLocalRoot());
		System.out.println("RemoteDirectory::process(), directory = '"+directory+"'");
		return process(directory,files,folders);
	}
	
	protected boolean process(String directory, Vector files, Vector folders)
	{
		System.out.println("RD::process('"+directory+"', v, v)");
		//	Cancel button test here
		if(m_transfer.isCancelled()){
			m_transfer.setTask(Transfer.TASK_CANCEL, new String[]{});
			m_transfer.close();
			return false;
		}
		
		Vector tfiles = new Vector();
		Vector tfolders = new Vector();
		
		if(list(directory,tfiles,tfolders) == false) return false;
		
		/*	Possible combinations: 
		 * files = 0 && create empty = true		create
		 * files = 0 && create empty = false	deny
		 * files > 0 && create empty = true		create
		 * files > 0 && create empty = false	create
		 */
		//	If there are no files, but createEmpty is false, clear the folders you found
		if(tfiles.size() == 0 && m_details.getEmptyDirectory() == false){
			tfolders.clear();
		}
		//	Add anything thats left, if nothing, nothing will get added
		files.addAll(tfiles);
		folders.addAll(tfolders);
		
		return true;
	}	
	
	protected boolean list(String directory, Vector files, Vector folders)
	{
		System.out.println("RD::list("+directory+",v,v)");
		
		folders.add( directory );

		m_transfer.setSubTask(Transfer.STASK_SCANNING, new String[] { directory });
		
		Vector subdir = new Vector();
		
		int retries = 3;
		while(retries > 0){		
			if(m_transfer.list(directory,files,subdir) == true){
				for(int a=0;a<subdir.size();a++){
					if(process((String)subdir.get(a), files, folders) == false)	return false;
				}
				
				return true;
			}else{
				files.clear();
				subdir.clear();
			}
			
			retries--;
		}
		
		return false;
	}
	
	public long getNumBytes(Vector files)
	{
		for(int a=0;a<files.size();a++){
			if(m_transfer.isCancelled()) return -1;
			
			String filename = (String)files.get(a);
			try{
				long s = m_transfer.isFile(filename);
				if(s > 0) m_size += s;
			}catch(Exception e){
				//	File doesnt exist
			}
		}
		return m_size;
	}
}
