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

package org.kmem.kosh.sftp;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.TransferCancelledException;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.FileTransferProgress;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import java.io.*;
import java.util.*;

import org.kmem.kosh.sftp.directory.FileList;

public class TransferSSHTools extends Transfer {
	protected SshClient m_ssh;
	protected SftpClient m_sftp;
	protected TransferMonitor m_sftpMonitor;
	protected FilterWriter m_output;

	/**	Transfer monitor to watch the progress of files
	 * 
	 * @author Chris
	 *
	 */
	public class TransferMonitor implements FileTransferProgress {
		protected Transfer m_transfer;
		protected long m_max;
		protected long m_count;
		protected long m_previous;
		protected boolean m_cancel;
		
		public TransferMonitor(Transfer t){
			//System.out.println("TRACE-> TransferMonitor::TransferMonitor()");
			m_transfer = t;
			m_cancel = false;
		}
		
		public void setCancelled(){
			m_cancel = true;
		}

	    public void started(long bytesTotal, String remoteFile){
	    	//System.out.println("TRACE-> TransferMonitor::started("+remoteFile+", "+bytesTotal+")");
			m_max = bytesTotal;
			m_count = 0;
			m_previous = System.currentTimeMillis();
	    }

	    public boolean isCancelled(){ 
	    	//System.out.println("TRACE-> Monitor::isCancelled()");
	    	return m_cancel;
	    }

	    public void progressed(long bytesSoFar){
	    	m_output.println("Monitor::progressed("+bytesSoFar+")");
	    	long diffBytes = bytesSoFar - m_count;
	    	m_count = bytesSoFar;
			
			long current = System.currentTimeMillis();
			long diff = current - m_previous;
			m_previous = current;
			
			long bytesPerSec = (long)(diffBytes * (1000.0/(double)diff));

			m_transfer.update(bytesPerSec,bytesSoFar);
	    }

	    public void completed(){
	    	//System.out.println("TRACE-> Monitor::completed()");
	    }
	}
	/*************************************/
	/**	End of TransferMonitor class **/
	/*************************************/
	
	public TransferSSHTools(){
		m_output = SftpPlugin.getDefault().getFilterWriter();
		m_errorPrefix = "ERROR: SSHTools: ";
	}

	public boolean open() {
		// Immediately lock the Transfer object
		if (m_locked == false) {
			m_locked = true;
			m_open = false;

			// Debug information
			m_details.debug();

			// TODO: Find out what this does before re-enabling it
			try{
				ConfigurationLoader.initialize(false);
			}catch(IOException e){
				setTask(TASK_ERROR,new String[] {m_errorPrefix+"configuration: "+e.getMessage() });
			}
			m_ssh = new SshClient();

			try{
				m_ssh.setSocketTimeout(m_details.getTimeout()*1000);
				m_ssh.connect(
						m_details.getServer(), 
						m_details.getPort(), 
						new IgnoreHostKeyVerification());
			}catch(IOException e){
				SftpPlugin.consolePrintln(m_errorPrefix+"Connection Error", 1);
				setTask(TASK_ERROR,new String[]{m_errorPrefix+"connect: "+e.getMessage() });
				m_locked = false;
				return m_open;
			}
			// Create a password authentication instance
			PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
			// Set the users name
			pwd.setUsername(m_details.getUsername());
			// Set the password
			pwd.setPassword(m_details.getPassword());

			// Try the authentication
			int result = AuthenticationProtocolState.FAILED;
			try{
				result = m_ssh.authenticate(pwd);
			}catch(IOException e){
				setTask(TASK_ERROR,new String[]{m_errorPrefix+"auth: "+e.getMessage() });
			}

			if (result == AuthenticationProtocolState.COMPLETE) {
				try{
					m_sftp = m_ssh.openSftpClient();
					m_open = true;
				}catch(IOException e){
					setTask(TASK_ERROR,new String[]{m_errorPrefix+"openSftpClient: "+e.getMessage() });
				}
				
			} else {
				setTask(TASK_ERROR,new String[] {
						m_errorPrefix+"Failed to open SFTP Site",
						"Authentication error: Check username and password" });
			}
		}

		return m_open;
	}
	
	public void close() {
		if(m_ssh != null && m_sftp != null){
			try{
				m_sftp.quit();
				m_ssh.disconnect();
			}catch(IOException e){
				setTask(TASK_ERROR, new String[]{m_errorPrefix+"Failed to disconnect properly"});
			}
		}
	}	
	
	protected void createRemoteDirectory(String dir) {
		m_output.println("TransferSSHTools::createRemoteDirectory(String dir)");
		try{
			//	Stat the item, if found directory or file, set the status
			//	This is an error, if the directory/file exists here, you can't
			//	create anything
			m_output.println("[ryan]directory = "+dir);
			FileAttributes attrib = m_sftp.stat(dir);
			m_output.println("[ryan]attrib = "+attrib+", attrib string = "+attrib.toString());
			
			if(attrib.isDirectory() == true){
				m_fileList.setStatus(m_count, FileList.DEXIST);
			}else{
				m_fileList.setStatus(m_count, FileList.FEXIST);
			}
		}catch(IOException e){
			//	If the item doesnt exist, create a directory with the same name
			try{
				m_sftp.mkdir(dir);
				m_fileList.setStatus(m_count, FileList.OK);
			}catch(IOException mk){
				m_output.append(m_errorPrefix+e.getMessage() +": "+e.getCause());
				m_fileList.setStatus(m_count,FileList.DEXIST);
			}
		}
	}

	public void createMonitor() {
		m_sftpMonitor = new TransferMonitor(this);
	}

	public void transfer(String dir, String item) throws Exception {
		//	Transfer the item according to the mode
		try{
			if(m_fileList.getMode() == TransferDetails.GET){
				m_sftp.get(dir+item,dir+item, m_sftpMonitor);
			}else{
				m_sftp.put(dir+item,dir+item, m_sftpMonitor);
			}
		}catch(TransferCancelledException e){
			//System.out.println("transfer cancelled: file doesnt exist to transfer");
			m_fileList.setStatus(m_count, FileList.FNOTEXIST);
		}
	}
	
	public void cancelTransfer(){
		m_sftpMonitor.setCancelled();
	}

	public boolean setDirectories() {
		boolean success = false;
		try{
			m_sftp.lcd(m_details.getLocalRoot());
			
			//	Make sure the site root exists, if not, try to create it
			try{
				/*	We don't use the return FileAttributes since all we want to know, is whether
				 * the item exists, if it does, try to cd to it, if it's a file, it'll except and we'll
				 * return false anyway, if it succeeds, it'll return true.
				 * 
				 *	if this excepts, it's cause the item doesnt exist, try to create it.
				 *	if creation excepts itself, it'll get caught by the outside hanlder
				 *	and false will return anyway
				 */
				m_sftp.stat(m_details.getSiteRoot());
			}catch(IOException nodir){
				//	If this fails, it'll all fail, so return false so the transfer can't complete
				m_sftp.mkdir(m_details.getSiteRoot());
			}
			
			m_sftp.cd(m_details.getSiteRoot());
			success = true;
		}catch(IOException e){
			System.out.println(m_errorPrefix+e.getMessage());
		}catch(NullPointerException e){
			System.out.println(m_errorPrefix+": NullPointerException");
		}
		
		return success;
	}
	
	public void list(String directory, Vector files, Vector folders){
		//m_output.println("TransferSSHTools::getDirectoryList(): "+directory);
		
		//	Is the connection open?
		if(m_open == true){
			try{
				//	build the directory you want to scan, from the root+directory
				String tmp = m_details.getSiteRoot()+"/"+directory;
				
				//	Grab the contents of this directory
				List contents = m_sftp.ls(tmp);
				
				//	Loop through all the returned values
				Iterator i = contents.iterator();
				
				while(i.hasNext()){
					SftpFile entry = (SftpFile)i.next();
					
					//	Ignores anything with a . or .. in it
					if(entry.getFilename().endsWith(".")) continue;
					//m_output.println("[ryan]entry = "+entry+", entry filename = "+entry.getFilename());
					//	Get the attributes for this directory item
					FileAttributes attrib = entry.getAttributes();
					//m_output.println("[ryan]attrib = "+attrib+", attrib string = "+attrib.toString());
					
					//	Is it a directory or file? add it to the appropriate array
					if(attrib.isDirectory() == true && m_details.getRecurse() == true){
						folders.add(directory+entry.getFilename()+"/");
					}else{
						files.add(directory+entry.getFilename());
					}
				}
			}catch(IOException e){
				//	There was an exception scanning this directory
				//	TODO; This could possibly mean that the remote directory doesnt exist
				m_output.println(m_errorPrefix+e.getMessage()+": "+e.getCause());
			}
		}
	}	
	
	public boolean isDirectory(String dir) throws Exception{
		m_output.println("TransferSSHTools::isDirectory(String dir)");
		try{
			//	Stat the requested site root
			FileAttributes attrib = m_sftp.stat(dir);
			m_output.println("[ryan]attrib = "+attrib+", attrib string = "+attrib.toString());
			
			//	If directory, great, otherwise, show warning
			if(attrib.isDirectory()) return true;
		}catch(IOException e){
			throw new Exception();
		}
		
		return false;
	}	
	
	public long isFile(String file) throws Exception{
		m_output.println("TransferSSHTools::isFile(String file)");
		long size = -1;
		try{
			//	Stat the requested site root
			FileAttributes attrib = m_sftp.stat(file);
			m_output.println("[ryan]attrib = "+attrib+", attrib string = "+attrib.toString());
			
			//	If file, great, return it's filesize, otherwise return -1
			//	If doesnt exist, throw exception
			if(attrib.isFile()) size = attrib.getSize().longValue();
		}catch(IOException e){
			throw new Exception();
		}
		
		return size;
	}

	/**	Test the server details to make sure it exists
	 * 
	 *	This is to test whether the details used are correct, if they are not
	 *	a connection will not be made to the remote server, if they are, then of course
	 *	everything is fine
	 *
	 * @return	True or false, depending on whether the connection was successful
	 */
	public boolean test(){
		//	Open the server
		boolean status = open();
		if(status == true){
			//	Output all server information
			m_output.append("\nChecking Server: \n");
			m_output.append("Address: "+m_details.getServer()+"\n");
			m_output.append("SFTP Server: "+m_ssh.getServerId()+"\n");
			m_output.append("Server connected successfully\n\n");
			
			try{
				m_output.append("Checking Site root: \n");
				status = isDirectory(m_details.getSiteRoot());
				
				if(status == true){
					m_output.append("Site root exists\n");
				}else{
					m_output.append("ILLEGAL: The requested site root, is a recognised file\n");
				}
			}catch(Exception e){
//				Site root doesnt exist, better say so
				m_output.append("WARNING: Site root does NOT exist\n");
				m_output.append("(Possibly this is because it doesnt exist yet)\n");
			}
			
			m_output.append("SERVER TEST: OK\n");
		}else{
			m_output.append("SERVER TEST: FAILED\n");
			status = false;
		}
		//	Close the server and return the status
		close();
		return status;
	}
}