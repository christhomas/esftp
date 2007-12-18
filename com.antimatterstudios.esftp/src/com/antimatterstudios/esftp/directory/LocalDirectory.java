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
import java.io.File;
import com.antimatterstudios.esftp.Transfer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class LocalDirectory extends Directory
{
	public LocalDirectory(Transfer t)
	{
		//System.out.println("TRACE-> LocalDirectory::LocalDirectory()");
		m_size = 0;
		m_transfer = t;
		m_details = m_transfer.getTransferDetails();
	}
	
	public boolean process(Object item, Vector files, Vector folders)
	{
		if(m_transfer.isCancelled()) return false;
		
		//System.out.println("TRACE-> LocalDirectory::process(Object item, Vector files,Vector folders)");
		Vector tfiles = new Vector();
		Vector tfolders = new Vector();
		list(item,tfiles,tfolders);
		
		/*	Possible combinations: 
		 * files = 0 && create empty = true		create
		 * files = 0 && create empty = false	deny
		 * files > 0 && create empty = true		create
		 * files > 0 && create empty = false	create
		 */
		//	If there are no files, but createEmpty is false, clear the folders you found
		//System.out.println("Return Files:\t"+files.size() + "\tcreateEmpty = "+m_details.getEmptyDirectory());
		if(tfiles.size() == 0 && m_details.getEmptyDirectory() == false){
			tfolders.clear();
		}
		//	Add anything thats left, if nothing, nothing will get added
		files.addAll(tfiles);
		folders.addAll(tfolders);
		
		return true;
	}	

	protected boolean list(Object item, Vector files, Vector folders)
	{
		//System.out.println("TRACE-> LocalDirectory::list(Object item, Vector files,Vector folders)");
		//System.out.println("Number Files: \t"+files.size()+"\tNumber Folders: "+folders.size());
		
		IContainer c = (IContainer)item;
		
		//	append / and make sure any that end with // are converted to /
		String localRoot = m_details.getLocalRoot();
		String directory = c.getLocation().toPortableString();
		if(directory.endsWith("/") == false) directory+="/";
		
		String relative = makeRelative(directory, localRoot);
		if(relative.length() > 0) folders.add( relative );		
		
		m_transfer.setSubTask(Transfer.STASK_SCANNING, new String[] { relative });
		
		try{
			IResource r[] = c.members();
			//System.out.println("This directory has: "+r.length+" members");
			for(int a=0;a<r.length;a++){
				String str = r[a].getLocation().toPortableString();
				switch(r[a].getType()){
					case IResource.FILE:						
						System.out.println("Found file = "+str);						
						
						files.add( makeRelative(str, localRoot) );
					break;
					
					case IResource.FOLDER:
					case IResource.PROJECT:
						/*	TODO Possible duplicated if()
						 * Is this if statement necessary? it seems it's made inside a similar statement above
						 */
						//System.out.println("Test if we can recurse subdirs");
						System.out.println("Found folder = "+str);
						
						if(m_details.getRecurse() == true){
							//System.out.println("yes, we can");
							if(process((Object)r[a],files,folders) == false) return false;
						}//else System.out.println("no we can't");
					break;
				}
			}
			
		}catch(CoreException e){
			//	TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public long getNumBytes(Vector files)
	{
		//	Loop through all the files, adding up the lengths of ALL the files
		//	contained in the file array
		for(int a=0;a<files.size();a++){
			String filename = (String)files.get(a);
			File f = new File(m_details.getLocalRoot()+filename);
			if(f.isFile()){
				m_size += f.length();
			}
		}
		return m_size;
	}
}
