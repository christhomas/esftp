package com.antimatterstudios.esftp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.ftp4che.FTPConnection;
import org.ftp4che.FTPConnectionFactory;
import org.ftp4che.exception.*;
import org.ftp4che.util.ftpfile.FTPFile;

import com.antimatterstudios.esftp.directory.FileList;
import com.sshtools.j2ssh.TransferCancelledException;

public class TransferFTP extends Transfer {
	protected FTPConnection m_ftp;
	
	public TransferFTP(){
		m_errorPrefix = "ERROR: ftp4che: ";
	}
	
	@Override
	public void cancelTransfer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		if(m_ftp != null){
			try{
				m_ftp.disconnect();
				m_ftp = null;
				m_open = false;
			}catch(Exception e){
				setTask(TASK_ERROR, new String[]{m_errorPrefix+"Failed to disconnect properly"});
			}
		}
	}

	@Override
	public void createMonitor() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createRemoteDirectory(String dir) {
		try{
			m_ftp.makeDirectory(dir);
			m_fileList.setStatus(m_count, FileList.OK);
		}catch(FtpWorkflowException e){
			setTask(TASK_ERROR,new String[]{m_errorPrefix+"createRemoveDirectory(): FTPWorkFlowException: "+e.getMessage()});
		}catch(FtpIOException e){
			setTask(TASK_ERROR,new String[]{m_errorPrefix+"createRemoveDirectory(): FtpIOException: "+e.getMessage()});
		}catch(IOException e){
			setTask(TASK_ERROR,new String[]{m_errorPrefix+"createRemoveDirectory(): IOException: "+e.getMessage()});
		}
	}
	
	public boolean isDirectory(String directory) throws Exception
	{
		directory = m_details.getSiteRoot()+"/"+directory;
		List<FTPFile> dirlist = m_ftp.getDirectoryListing(directory);
		
		ListIterator<FTPFile> i = dirlist.listIterator();
		while(i.hasNext()){
			FTPFile f = i.next();
			if(f.getName() == directory){
				return f.isDirectory();
			}
		}
		
		return false;
	}

	@Override
	public long isFile(String filename) throws Exception {
		String directory = filename.substring(0, filename.lastIndexOf('/'));
		List<FTPFile> dirlist = m_ftp.getDirectoryListing(directory);
		
		ListIterator<FTPFile> i = dirlist.listIterator();
		while(i.hasNext()){
			FTPFile f = i.next();
			if(f.getName() == filename){
				return f.getSize();
			}
		}		
		
		return 0;
	}

	@Override
	public void list(String directory, Vector<String> files, Vector<String> folders) {
		//	Is the connection open?
		if(m_open == true){
			try{
				//	build the directory you want to scan, from the root+directory
				String tmp = m_details.getSiteRoot()+"/"+directory;
				
				//	Grab the contents of this directory
				List<FTPFile> contents = m_ftp.getDirectoryListing(tmp);
				
				//	Loop through all the returned values
				Iterator<FTPFile> i = contents.iterator();
				
				while(i.hasNext()){
					FTPFile entry = i.next();
					
					//	Ignores anything with a . or .. in it
					if(entry.getName().endsWith(".")) continue;
					
					//	Is it a directory or file? add it to the appropriate array
					if(entry.isDirectory() == true && m_details.getRecurse() == true){
						folders.add(directory+entry.getName()+"/");
					}else{
						files.add(directory+entry.getName());
					}
				}
			}catch(FtpWorkflowException e){
				m_output.println(m_errorPrefix+"TransferFTP::list(): FtpWorkflowException: "+e.getMessage()+": "+e.getCause());
			}catch(FtpIOException e){
				m_output.println(m_errorPrefix+"TransferFTP::list(): FtpIOException: "+e.getMessage()+": "+e.getCause());
			}catch(IOException e){
				//	There was an exception scanning this directory
				//	TODO; This could possibly mean that the remote directory doesnt exist
				m_output.println(m_errorPrefix+e.getMessage()+": "+e.getCause());
			}
		}
	}

	@Override
	public boolean open() {
		m_open = false;
		
		// Immediately lock the Transfer object
		if (m_locked == false) {
			m_locked = true;
			
			// Debug information
			m_details.debug();
			
			try{
				Properties pt = new Properties();
				pt.setProperty("connection.host",m_details.getServer());
				pt.setProperty("connection.port",Integer.toString(m_details.getPort()));
				pt.setProperty("user.login",m_details.getUsername());
				pt.setProperty("user.password", m_details.getPassword());
				pt.setProperty("connection.type","FTP_CONNECTION");
				pt.setProperty("connection.passive","true");
				
				m_ftp = FTPConnectionFactory.getInstance(pt);
				
				try{
					m_ftp.connect();
					m_open = true;
				}catch(NotConnectedException nce) {
					setTask(TASK_ERROR,new String[]{m_errorPrefix+"Failed to connect: "+nce.getMessage() });
	            } catch (IOException ioe) {
	            	setTask(TASK_ERROR,new String[]{m_errorPrefix+"IO Exception occured: "+ioe.getMessage() });
	            } catch (Exception e) {
	            	setTask(TASK_ERROR,new String[]{m_errorPrefix+"General Exception occured: "+e.getMessage() });
	            }
			}catch(ConfigurationException ce){
				setTask(TASK_ERROR,new String[]{m_errorPrefix+"configuration has errors: "+ce.getMessage() });
			}
		}
		
		return m_open;
	}

	@Override
	public boolean setDirectories() {
		boolean success = false;
		/*
		try{
			
			m_sftp.lcd(m_details.getLocalRoot());
			
			//	Make sure the site root exists, if not, try to create it
			try{
				//	Just stat the item, it's enough to know whether it exists or not
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
		}*/
		
		return success;
	}

	@Override
	public void transfer(String dir, String item) throws Exception {
		// //	Transfer the item according to the mode
		FTPFile src = new FTPFile(dir+item, false);
		FTPFile dest = new FTPFile(dir+item, false);
		
		try{
			if(m_fileList.getMode() == TransferDetails.GET){				
				m_ftp.downloadFile(src,dest);
			}else{
				m_ftp.uploadFile(src,dest);
			}
		}catch(FtpWorkflowException e){
			m_output.println(m_errorPrefix+"TransferFTP::transfer(): FtpWorkflowException: "+e.getMessage()+": "+e.getCause());
		}catch(FtpIOException e){
			m_output.println(m_errorPrefix+"TransferFTP::transfer(): FtpIOException: "+e.getMessage()+": "+e.getCause());
		}catch(IOException e){
			//System.out.println("transfer cancelled: file doesnt exist to transfer");
			m_fileList.setStatus(m_count, FileList.FNOTEXIST);
		}
	}
	
	protected String getServerId()
	{
		return "FTP4CHE";
	}

}
