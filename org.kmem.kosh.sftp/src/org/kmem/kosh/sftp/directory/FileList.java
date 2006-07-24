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
 *    {Danny Valliant} - Initial Hotkey support and Console output
 *    xenden@users.sourceforge.net
 *******************************************************************************/

package org.kmem.kosh.sftp.directory;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.kmem.kosh.sftp.Transfer;
import org.kmem.kosh.sftp.TransferDetails;

import java.util.*;

public class FileList {
	protected IProject m_project;

	protected TransferDetails m_details;

	protected int m_mode;

	//	The number of bytes to transfer in this list
	protected long m_size;

	protected Vector m_folders, m_files, m_status;

	//	These are status messages for each file that gets transferred
	public static final String OK = "OK";
	public static final String FAILED = "FAILED";
	public static final String NOSTATUS = "NO STATUS";
	public static final String CANCEL = "TRANSFER CANCELLED";

	public static final String DEXCEPTION = "DEXCEPTION";
	public static final String DUNKNOWN = "DIRECTORY UNKNOWN";
	public static final String DEXIST = "DIRECTORY EXISTS";
	public static final String DFEXIST = "DIRECTORY CREATE FAILED, FILE EXISTS";
	public static final String DNOTEXIST = "DIRECTORY DOES NOT EXIST";

	public static final String FZERO = "FILE SIZE ZERO";
	public static final String FEXIST = "FILE EXISTS";
	public static final String FDEXIST = "FILE CREATE FAILED, DIRECTORY EXISTS";
	public static final String FNOTEXIST = "FILE DOES NOT EXIST";

	public FileList(IProject p) {
		//System.out.println("TRACE-> FileList::FileList(IProject:"+p+")");
		m_project = p;

		m_size = 0;
	}

	/**
	 * Initialise the file list with it's transfer details
	 * 
	 * This is to acquire the information needed to transfer the files either to
	 * or from the system
	 */
	public void init(int transferType) {
		m_details = new TransferDetails(m_project);

		m_files = new Vector();
		m_folders = new Vector();
		m_mode = transferType;
	}

	/**
	 * Set the mode of transfer
	 * 
	 * @param transferType
	 *            The mode of transfer for this block of files
	 */
	public int getMode() {
		return m_mode;
	}

	/**
	 * Returns the project this FileList is associated with
	 * 
	 * @return the project resource for this FileList
	 */
	public IProject getProject() {
		return m_project;
	}

	/**
	 * Returns the unique transfer key for this FileList when the Transfer is
	 * being initialised to start
	 * 
	 * This is so the system can quickly identify servers the transfers are
	 * aimed at. If the server is NOT unique and it already present in the list,
	 * then it makes sense to add the files/folders from THIS transfer into THAT
	 * one, therefore reducing the number of times you need to connect to a
	 * server in order to complete the transfer list
	 * 
	 * @return the key that represents the server for this FileList to transfer
	 *         to
	 */
	public String getKey() {
		return m_details.getKey();
	}

	/**
	 * Returns the TransferDetails object to the Transfer object
	 * 
	 * This method is to obtain the Transfer details for the FileList, so the
	 * Transfer object can do it's job
	 * 
	 * @return TransferDetails The details of the sftp server to transfer to
	 */
	public TransferDetails getDetails() {
		return m_details;
	}

	/**
	 * Add a single file to the file list
	 * 
	 * @param file
	 *            the path of the file to transfer
	 */
	public void addFile(String file) {
		//System.out.println("TRACE-> FileList::addFile(" + file + ")");
		m_files.add(file);
	}

	/**
	 * Add a single folder to the file list
	 * 
	 * @param folder
	 *            The folder to transfer
	 */
	public void addFolder(IContainer c) {
		//System.out.println("TRACE-> FileList::addFolder(" + c + ")");

		m_folders.add(c);
	}

	/**
	 * Obtains a file from the list
	 * 
	 * In getting a file from this object, it's removed from it
	 * 
	 * @returns String containing the filename to transfer
	 */
	public String getFile(int a) {
		//System.out.println("TRACE-> FileList::getFile(" + a + ")");

		if (a < m_files.size()) {
			return (String) m_files.get(a);
		}
		return null;
	}

	public void setStatus(int a, String result) {
		try {
			if (a < m_status.size()) {
				m_status.set(a, result);
			}
		} catch (Exception e) {
			System.out.println("setStatus exception" + e.getMessage());
		}
	}
	
	public String getStatus(int a) {
		String s = NOSTATUS;
		if (a < m_status.size()) {
			String tmp = (String) m_status.get(a);
			if (tmp != null) s = tmp;			
		}
		return s;
	}

	public String getStatusString(int a) {
		if (a < m_status.size()) {
			String s = (String) m_status.get(a);
			if (s == null) s = "NO STATUS";
			
			String transferType = "Put File: ";
			if(m_mode == TransferDetails.GET) transferType = "Get File: ";
			
			return transferType + (String) m_files.get(a) + "\t\t" + s + "\n";
		}
		return "Cannot find file index";
	}
	
	/**
	 * Optimises the temporary data structures into a FINAL list of files
	 * 
	 */
	public int optimise(Transfer transferjob) {
		//System.out.println("TRACE-> FileList::optimise()");

		transferjob.setTask(Transfer.TASK_SCANNING, null);

		/**
		 * Remove duplicated files from the file array:
		 * 
		 * Have to look at each folder and find files which exist inside those
		 * folders. If they exist, since the folder is being transferred, all
		 * the files inside are ALREADY going to be transferred, so remove the
		 * file from the list it'll be added later when the directory is
		 * scanned.
		 * 
		 * Also, if recursion is enabled, remove any files which exist within a
		 * directory marked for scanning, since again, that file will be added
		 * again when the directory is recursed and scanned fully.
		 * 
		 * however, if the file exists in a subdirectory of the marked directory
		 * and recursion isnt enabled, then of course, keep the file, since it
		 * will not be added to the list later.
		 */
		for (int a = 0; a < m_folders.size(); a++) {
			IContainer c = (IContainer) m_folders.get(a);
			String folder = c.getLocation().toPortableString();

			for (int b = 0; b < m_files.size(); b++) {
				String file = (String) m_files.get(b);
				String path = file.substring(0, file.lastIndexOf("/") + 1);

				/**
				 * Two cases where you should remove the file
				 * 
				 * a) If the path of the file, equals the folder being compared
				 * against b) If recurse is enabled, and the path of the file
				 * starts with the folder being compared against
				 */
				if (path.equals(folder)	|| ((m_details.getRecurse() == true) && (path.startsWith(folder) == true))) {
					m_files.remove(b);
					b--;
				}
			}
		}

		/**
		 * Remove duplicated folders from the folder array
		 * 
		 * If recurse is enabled, you must remove any subdirectories
		 * of those which are selected, because further scanning
		 * will add them later anyway, so remove them from here
		 * so they don't duplicate
		 * 
		 * 	NOTE: is this Unicode/UTF-8 safe? or will it break on non
		 * 					ASCII/english platforms?
		 */
		if (m_details.getRecurse() == true) {
			Collections.sort(m_folders, new ResourceCompare());
			IContainer c;
			for (int a = 0; a < m_folders.size(); a++) {
				c = (IContainer) m_folders.get(a);
				String src = c.getLocation().toPortableString();
				for (int b = a + 1; b < m_folders.size(); b++) {
					c = (IContainer) m_folders.get(a);
					String dst = c.getLocation().toPortableString();

					if (dst.startsWith(src)) {
						m_folders.remove(a);

						c = (IContainer) m_folders.get(a);
						src = c.getLocation().toPortableString();

						b--;
					}
				}
			}
		}

		/**
		 * Remove any empty folders from the selection
		 * 
		 * If there are some directories selected which are empty
		 * they must be removed, since they are not permitted to
		 * transfer empty directories
		 */
		if (m_details.getEmptyDirectory() == false) {
			for (int a = 0; a < m_folders.size(); a++) {
				IContainer c = (IContainer) m_folders.get(a);

				try {
					IResource r[] = c.members();
					if (r.length == 0) {
						m_folders.remove(a);
						a--;
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * Finalise the selected files
		 * 
		 * Now that duplicated files, folders and empty directories
		 * are optionally removed.  Scanning of each directory chosen
		 * must happen to build a final file list.
		 * 
		 * This process scans all directories and optionally recurses
		 * and builds a list of files/folders to transfer
		 */
		//System.out.println("Finalise the selected files into a single filelist");
		Vector files = new Vector();
		Vector folders = new Vector();
		Directory d;

		if (m_mode == TransferDetails.GET) {
			//System.out.println("This is a GET request");
			d = new RemoteDirectory(transferjob);
		} else {
			//System.out.println("This is a PUT request");
			d = new LocalDirectory(transferjob);
		}
		
		/** Process all locally selected files into relative names
		 * 
		 * 	We need to do this, cause otherwise, files selected separately
		 *	from directories, will not be shortened to relative names
		 */
		//System.out.println("We are going to convert all absolute paths to relative paths");
		for(int a=0;a<m_files.size();a++){
			String str = (String)m_files.get(a);
			str = d.makeRelative(str);
			m_files.set(a, str);
		}		

		//System.out.println("We are now going to process directories for all files and folders");
		for (int a = 0; a < m_folders.size(); a++){
			d.process(m_folders.get(a), files, folders);
		}

		/**
		 * 	Prune any unwanted directory paths
		 * 
		 * After scanning there might be some directories which are
		 * included in the list, that are parents for other directories
		 * like in "Remove duplicated folders from the folder array" code
		 * block above, they must be removed, since they are created 
		 * implicitly by creating their subdirectories.
		 * 
		 * Reduces the number of steps that occur and speeds up
		 * the transfer of data by not performing unnecessary creation
		 * and failure of directories
		 */
		//System.out.println("Now we are going to prune unwanted directory paths, which are subsequently not needed");
		if (m_details.getRecurse() == true) {
			Collections.sort(folders);

			for (int a = 0; a < folders.size(); a++) {
				String src = (String) folders.get(a);
				for (int b = a + 1; b < folders.size(); b++) {
					String dst = (String) folders.get(b);

					if (dst.startsWith(src)) {
						folders.remove(a);
						src = (String) folders.get(a);
						b--;
					}
				}
			}
		}

		/**
		 * Finalise all arrays
		 * acquire the size of the transfer in bytes
		 * add all the files and folders to the transfer array (m_files)
		 * 
		 * clear any temporary processing data (m_folders)
		 * 
		 * Sort the array into alphabetical order
		 * 
		 * create the status array to hold the results of transfer and 
		 * return the number of items in the array to be transferred
		 */
		//System.out.println("We can now build the final file list");
		m_files.addAll(files);
		m_files.addAll(folders);
		m_size = d.getNumBytes(m_files);
		// Clear the folders vector, since it's contents have been transferred
		m_folders.clear();

		//System.out.println("Sorting the file list into alphabetical order");
		Collections.sort(m_files);

		/*System.out.println("FINAL FILES["+m_files.size()+"]: ");
		for (int a = 0; a < m_files.size(); a++) {
			System.out.println("File = " + m_files.get(a));
		}*/

		// Set the status array to have that many elements
		// So the transfer code can set the transfer status upon completion
		m_status = new Vector();
		m_status.setSize(m_files.size());

		return m_files.size();
	}

	public long getSize() {
		//System.out.println("TRACE-> FileList::getSize(): returning:"+m_size);
		return m_size;
	}

	public int getNumItems() {
		return m_files.size();
	}
}
