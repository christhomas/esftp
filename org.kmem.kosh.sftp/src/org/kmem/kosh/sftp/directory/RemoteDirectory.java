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

package org.kmem.kosh.sftp.directory;

import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.kmem.kosh.sftp.Transfer;

public class RemoteDirectory extends Directory {
	protected boolean m_open;
	
	public RemoteDirectory(Transfer t){
		//System.out.println("TRACE-> RemoteDirectory::RemoteDirectory()");
		m_size = 0;
		m_transfer = t;
		m_details = m_transfer.getTransferDetails();
	}	
	
	public void process(Object item, Vector files, Vector folders) {
		IContainer c = (IContainer)item;
		String directory = c.getLocation().toPortableString() + "/";
		directory = makeRelative(directory, m_details.getLocalRoot());
		
		process(directory,files,folders);
	}
	
	protected void process(String directory, Vector files, Vector folders){
		Vector tfiles = new Vector();
		Vector tfolders = new Vector();
		
		list(directory,tfiles,tfolders);
		
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
	}	
	
	protected void list(String directory, Vector files, Vector folders) {
		//System.out.println("Number Files: \t"+files.size()+"\tNumber Folders: "+folders.size());
		
		if(directory.length() > 0) folders.add( directory );
		folders.add( directory );

		m_transfer.setSubTask(Transfer.STASK_SCANNING, new String[] { directory });
		
		Vector subdir = new Vector();
		m_transfer.list(directory, files, subdir);
			
		for(int a=0;a<subdir.size();a++){
			process((String)subdir.get(a), files, folders);
		}
	}
	
	public long getNumBytes(Vector files){
		for(int a=0;a<files.size();a++){
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
