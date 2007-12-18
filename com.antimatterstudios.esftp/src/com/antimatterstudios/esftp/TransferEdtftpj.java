package com.antimatterstudios.esftp;

import java.io.IOException;
import java.util.Vector;
import java.text.ParseException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPProgressMonitor;
import com.enterprisedt.net.ftp.FTPTransferType;

import com.antimatterstudios.esftp.directory.FileList;

class TransferEdtftpj extends Transfer
{
	private ESFTPMonitor m_monitor;
	
	private class ESFTPMonitor implements FTPProgressMonitor{
		protected Transfer m_transfer;
		
		long m_count;
		long m_pt;
		
		public ESFTPMonitor(Transfer t){
			m_transfer = t;
		}
		
		public void reset(long total){
//			System.out.println("TRACE-> TransferMonitor::started("+remoteFile+", "+bytesTotal+")");
			m_count = 0;
			m_pt = System.currentTimeMillis();
		}
		
		public void bytesTransferred(long current){
			System.out.println("Monitor::progressed("+current+")");
	    	long diff = current - m_count;
	    	m_count = current;
			
			long ct = System.currentTimeMillis();
			long dt = ct - m_pt;
			m_pt = ct;
			
			long bytesPerSec = (long)(diff * (1000.0/(double)dt));

			m_transfer.update(bytesPerSec,diff);
		}
	}

	protected FTPClient m_ftp;
	
	public TransferEdtftpj(int protocol)
	{
		m_errorPrefix = "ERROR: Edtftpj: ";
		
		//	TODO: Should do some setup here for enabling certain FTP/TLS, FTP/SSL options
	}
	
	public void cancelTransfer()
	{
		// TODO Auto-generated method stub
		
	}

	public void close()
	{
		if(m_ftp != null){
			try{
				m_ftp.quit();
				m_ftp = null;
				m_open = false;
			}catch(Exception e){
				setTask(TASK_ERROR, new String[]{m_errorPrefix+"Failed to disconnect properly"});
			}
		}
	}
	
	public void createMonitor()
	{
		m_monitor = new ESFTPMonitor(this);
		m_ftp.setProgressMonitor(m_monitor);
	}

	protected void createRemoteDirectory(String dir)
	{
		int replyCode = 0;
		String exceptionMessage="";
		
		try{
			//	Attempt to create the directory, catch the ftp exception if it occurs
			m_ftp.mkdir(m_details.getSiteRoot()+dir);
		}catch(FTPException e){
			//	Because an exception occurs, doesnt mean the end of the world
			//	You can grab the reply code and determine what happened
			replyCode = e.getReplyCode();
			exceptionMessage = e.getMessage();
		}catch(IOException e){
			setTask(TASK_ERROR,new String[]{m_errorPrefix+"createRemoteDirectory(): IOException: "+e.getMessage()});			
		}
		
		switch(replyCode){
			case 550:{
				//	This error code means that the directory existed already, so it could not be created
				m_fileList.setStatus(m_count,FileList.MSG_DEXIST);
			}break;
			
			default:{
				//	Could not find a response code which was recognised, so lets mark it as "failed"
				System.out.println("reply code = "+replyCode);
				setTask(TASK_ERROR,new String[]{m_errorPrefix+"createRemoteDirectory(): FTPException: "+exceptionMessage });
			}break;
		}
	}
	
	public boolean isDirectory(String directory) throws Exception
	{
		System.out.println("Checking for the existance of: "+directory);
		
		try{
			m_ftp.chdir(directory);
			return true;
		}catch(IOException ioe){
			System.out.println("TransferEdtftpj::isDirectory(): IOException occurred");
		}catch(FTPException ftpe){
			System.out.println("TransferEdtftpj::isDirectory(): FTPException occurred");
		}
		
		return false;
	}

	public long isFile(String filename) throws Exception
	{
		filename = m_details.getSiteRoot()+filename;
		
		long size = (m_ftp.exists(filename)) ? m_ftp.size(filename) : 0;
		System.out.println("filename = "+filename+", size = "+size);
		return size;
	}

	public boolean list(String directory, Vector files, Vector folders)
	{
		//	Is the connection open?
		if(m_open == true){
			try{
				String listDir = m_details.getSiteRoot()+directory;
				
				System.out.println("TransferEdtftpj::list(), directory = '"+listDir+"'");
				//	Grab the contents of this directory
				FTPFile[] contents = m_ftp.dirDetails(listDir);

				for(int a=0;a<contents.length;a++){
					FTPFile file = contents[a];
					String name = file.getName();
					
					System.out.println("ftp list: name = "+name);
					
					//	Ignores any string which is null, or contains a "." or ".." in it
					if(name == null || name.endsWith(".")) continue;
					
					//	Chop off the first / so you can safely concatenate it to the directory
					if(name.startsWith("/")) name = name.substring(1);
					
					name = directory+name;
					
					//	Is it a directory or file? add it to the appropriate array
					
					/*
					 *	There is a logic error with regard to recursing I think where this should
					 *	be decided in the RemoteDirectory layer, not here, the problem is that I think
					 *	this code will result in a list of files being created, but not the respective
					 *	directories which exist, now, you should not recurse into those if it's disabled
					 *	but you should create the basic entries anyway, but I dont think that happens at the
					 *	moment.
					 *
					 */
					if(file.isDir() == true && m_details.getRecurse() == true){
						folders.add(name+"/");
					}else{
						files.add(name);
					}
				}
				
				return true;
				
			}catch(ParseException e){
				System.out.println(m_errorPrefix+"TransferEdtftpj::list("+directory+",v,v): ParseException: "+e.getMessage()+": "+e.getCause());
				e.printStackTrace();
			}catch(FTPException e){
				System.out.println(m_errorPrefix+"TransferEdtftpj::list("+directory+",v,v): FTPException: "+e.getMessage()+": "+e.getCause());
			}catch(IOException e){
				//	There was an exception scanning this directory
				//	TODO; This could possibly mean that the remote directory doesnt exist
				System.out.println(m_errorPrefix+e.getMessage()+": "+e.getCause());
			}catch(NullPointerException npe){
				System.out.println("NPE: "+m_errorPrefix+npe.getMessage()+": "+npe.getCause());
				npe.printStackTrace();
			}
		}
		
		return false;
	}

	public boolean open()
	{
		System.out.println("TransferEdtftpj::open()");
		m_open = false;
		
		// Immediately lock the Transfer object
		if (m_locked == false) {
			m_locked = true;
			System.out.println("TransferEdtftpj::open(), locked and ready to open");
			
			// Debug information
			m_details.debug();
			
			System.out.println("TransferEdtftpj::open(), setting properties");
			
			try{
				m_ftp = new FTPClient();
				m_ftp.setRemoteHost(m_details.getServer());
				m_ftp.setRemotePort(m_details.getPort());
				m_ftp.connect();
				
				System.out.println("TransferEdtftpj::open(), trying to connect");
				m_ftp.login(m_details.getUsername(),m_details.getPassword());
				m_ftp.setType(FTPTransferType.BINARY);
				
				m_open = true;
				System.out.println("TransferEdtftpj::open(), ftp server opened!!");
			}catch(FTPException ftpe) {
				setTask(TASK_ERROR,new String[]{m_errorPrefix+"FTPException (failed to connect??) Failed to connect: "+ftpe.getMessage() });
				System.out.println("TransferEdtftpj::open(), failed to connect");
            } catch (IOException ioe) {
            	setTask(TASK_ERROR,new String[]{m_errorPrefix+"IO Exception occured: "+ioe.getMessage() });
            	System.out.println("TransferEdtftpj::open(), IOException occured");
            } catch (Exception e) {
            	setTask(TASK_ERROR,new String[]{m_errorPrefix+"General Exception occured: "+e.getMessage() });
            	System.out.println("TransferEdtftpj::open(), general exception");
            }
		}
		System.out.println("TransferEdtftpj::open(), returning: "+m_open);
		return m_open;
	}
/*
	@Override
	public boolean setDirectories()
	{
		try{
			m_ftp.chdir(m_details.getSiteRoot());
			System.out.println("TransferEdtftpj::setDirectories(), siteRoot = "+m_details.getSiteRoot());
			return true;
		}catch(IOException ioe){
			System.out.println("TransferEdtftpj::setDirectories(), IOE: "+m_errorPrefix+ioe.getMessage());
		}catch(FTPException ftpe){
			System.out.println("TransferEdtftpj::setDirectories(), FTPException: "+m_errorPrefix+ftpe.getMessage());
		}
		System.out.println("TransferEdtftpj::setDirectories(), failed to set siteRoot");
		
		return false;
	}*/

	public void transfer(String dir, String item) throws Exception
	{
		//	Transfer the item according to the mode		
		try{
			System.out.println("EDTFTP::transfer(), remote = "+m_details.getSiteRoot()+dir+item+", local = "+m_details.getLocalRoot()+dir+item);
			m_monitor.reset(0);
			if(m_fileList.getMode() == TransferDetails.GET){
				m_ftp.get(m_details.getLocalRoot()+dir+item,m_details.getSiteRoot()+dir+item);
			}else{
				m_ftp.put(m_details.getLocalRoot()+dir+item,m_details.getSiteRoot()+dir+item);
			}
		}catch(FTPException ftpe){
			System.out.println(m_errorPrefix+"TransferEdtftpj::transfer(): FTPException: "+ftpe.getMessage()+": "+ftpe.getCause());
		}catch(IOException e){
			//System.out.println("transfer cancelled: file doesnt exist to transfer");
			m_fileList.setStatus(m_count, FileList.MSG_FNOTEXIST);
		}
	}
	
	protected String getServerId()
	{
		try{
			return m_ftp.system();
		}catch(FTPException ftpe){
			System.out.println("TransferEdtftpj::getServerId(), FTPException caught");
		}catch(IOException ioe){
			System.out.println("TransferEdtftpj::getServerId(), IOException caught");
		}
		
		return "<Attempt to obtain server string caused an exception>";
	}

}