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

import java.util.Properties;
import java.util.Vector;

import org.kmem.kosh.sftp.directory.FileList;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

public class TransferJsch extends Transfer {
	//	JSch objects and control variables
	protected JSch m_jsch = null;
	protected Session m_session;
	protected ChannelSftp m_channel;
	protected TransferMonitor m_sftpMonitor;
	protected String m_errorPrefix;
	
	public class TransferMonitor implements SftpProgressMonitor {
		protected Transfer m_transfer;
		protected long m_max;
		protected long m_count;
		protected long m_previous;
		
		public TransferMonitor(Transfer t){
			m_transfer = t;
		}
		
		public void init(int op, String src, String dest, long max){
			System.out.println("TransferMonitor::init("+op+", "+src+", "+dest+", "+max+")");
			m_max = max;
			m_count = 0;
			m_previous = System.currentTimeMillis();
		}

		public boolean count(long count){
			m_count += count;
			
			long bytes = count;
			long current = System.currentTimeMillis();
			long diff = current - m_previous;
			m_previous = current;
			
			bytes*=(1000.0/diff);
			
			System.out.println("Count: "+count+", diff: "+diff+", bytes: "+bytes);
			
			m_transfer.setTransferSpeed(bytes,count);
			
			if(m_count == m_max) return true;
			
			return false;
		}

		public void end(){}
	}
	/*************************************/
	/**	End of TransferMonitor class **/
	/*************************************/
	
	public TransferJsch(){
		m_errorPrefix = "ERROR: JSCH: ";
	}
	
	/**	Open a channel to the SFTP site
	 * 
	 * @return	Whether opening the site was successful or not
	 */
	public boolean open(){
		//	Immediately lock the Transfer object
		if(m_locked == false){
			m_locked = true;
			
			//	Debug information
			m_details.debug();
		
			m_jsch = new JSch();
			m_open = false;
			
			try{
				m_session = m_jsch.getSession(	
						m_details.getUsername(),
						m_details.getServer(), 
						m_details.getPort());
			      
				m_session.setPassword(m_details.getPassword());
				Properties p = new Properties();
				p.setProperty("StrictHostKeyChecking", "no");
				m_session.setConfig(p);
					
				m_output.append("CONFIG: Strict host key checking: "+p.getProperty("StrictHostKeyChecking")+"\n");
				m_output.append("CONFIG: Connection timeout: "+m_details.getTimeout()+"\n");
							
				try{
					setTask(TASK_CONNECT,null);
					
					m_session.setTimeout(m_details.getTimeout()*1000);
					m_session.connect(m_details.getTimeout()*1000);
					m_channel = (ChannelSftp) m_session.openChannel("sftp");
					m_channel.connect();
					m_open = true;
				}catch(JSchException e){
					setTask(TASK_ERROR, new String[] { 
							m_errorPrefix+"Failed to open SFTP Site",
							m_errorPrefix+"Auth, Check username and password",
							"Possibility:\nAsk Administrator of SFTP server to enable in sshd_config\nPasswordAuthentication yes"});
					
					
				}
			}catch(JSchException e){
				setTask(TASK_ERROR, new String[] { m_errorPrefix+e.getMessage() });
			}
		}
		return m_open;
	}
	
	public void close(){
		if(m_jsch != null){
			setTask(TASK_COMPLETE,null);
			
			if(m_open == true) m_channel.disconnect();
			
			m_session.disconnect();
			
			m_channel = null;
			m_session = null;
			m_jsch = null;
		}
		m_locked = false;
	}	
	
	protected void createRemoteDirectory(String dir){
		try{
			isDirectory(dir);
			
			m_fileList.setStatus(m_count, FileList.DEXIST);
		}catch(Exception e){
			//	If the item doesnt exist, create a directory with the same name
			try{
				m_channel.mkdir(dir);
				m_fileList.setStatus(m_count, FileList.OK);
			}catch(SftpException mkdir){
				//	If creating the directory causes an exception
				//	even though we have no concrete evidence
				//	thanks to the lack of proper error reporting on 
				//	behalf of Jsch we'll assume the directory failed 
				//	to create cause it already exists 
				//	(or a file with that name does)
				m_fileList.setStatus(m_count,FileList.DEXIST);
			}
			return;
		}
		
		m_fileList.setStatus(m_count, FileList.FEXIST);
	}
	
	public void createMonitor(){
		m_sftpMonitor = new TransferMonitor(this);
	}
	
	public void transfer(String dir, String item) throws SftpException{
		//	Transfer the item according to the mode
		if(m_fileList.getMode() == TransferDetails.GET){
			m_channel.get(dir+item,m_details.getLocalRoot()+dir, m_sftpMonitor);
		}else{
			m_channel.put(dir+item,m_details.getSiteRoot(), m_sftpMonitor);
		}
	}
	
	public void cancelTransfer(){
		//	I dont know how to do this in Jsch
	}

	public boolean setDirectories(){
		boolean success = false;
		try{
			m_channel.lcd(m_details.getLocalRoot());
			
			SftpATTRS attrib = m_channel.stat(m_details.getSiteRoot());
			if(attrib.isDir() == false){
				m_channel.mkdir(m_details.getSiteRoot());
			}
			m_channel.cd(m_details.getSiteRoot());
			success = true;
		}catch(SftpException e){
			System.out.println(m_errorPrefix+e);
		}catch(NullPointerException e){
			System.out.println(m_errorPrefix+"NullPointerException");
		}
		
		return success;
	}
	
	public void list(String directory, Vector files, Vector folders){
		System.out.println("Transfer::getDirectoryList(): "+directory);
		
		//	Is the connection open?
		if(m_open == true){
			try{
				//	build the directory you want to scan, from the root+directory
				String tmp = m_details.getSiteRoot()+"/"+directory;
				
				//	Grab the contents of this directory
				Vector contents = m_channel.ls(tmp);
				//	Loop through all the returned values
				for(int a=0;a<contents.size();a++){
					ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)contents.get(a);

					//	Ignores anything with a . or .. in it
					if(entry.getFilename().endsWith(".")) continue;

					//	Get the attributes for this directory item
					SftpATTRS attrib = entry.getAttrs();
										
					//	Is it a directory or file? add it to the appropriate array
					if(attrib.isDir() == true && m_details.getRecurse() == true){
						folders.add(directory+entry.getFilename()+"/");
					}else{
						files.add(directory+entry.getFilename());
					}
				}
			}catch(SftpException e){
				//	There was an exception scanning this directory
				//	TODO; This could possibly mean that the remote directory doesnt exist
				System.out.println(m_errorPrefix+e.id+": "+e.message);
			}
		}
	}
	
	public boolean isDirectory(String dir) throws Exception{		
		try{
			//	Stat the requested site root
			SftpATTRS a = m_channel.stat(dir);
			
			//	If directory, great, otherwise, show warning
			if(a.isDir()) return true;
		}catch(SftpException e){
			throw new Exception();
		}
		
		return false;
	}
	
	public long isFile(String file) throws Exception{
		long size = -1;
		try{
			//	Stat the requested site root
			SftpATTRS a = m_channel.stat(file);
			
			//	If file, great, return it's filesize, otherwise return -1
			//	If doesnt exist, throw exception
			if(a.isDir() == false) size = a.getSize();
		}catch(SftpException e){
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
			m_output.append("Address: "+m_session.getHost()+"\n");
			m_output.append("SFTP Server: "+m_session.getServerVersion()+"\n");
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
