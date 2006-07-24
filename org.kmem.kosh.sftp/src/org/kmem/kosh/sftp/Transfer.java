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

import org.kmem.kosh.sftp.directory.FileList;
import org.kmem.kosh.sftp.ui.TransferResultsDialog;
import org.kmem.kosh.sftp.ui.TransferResultsConsole;

import java.text.DecimalFormat;
import java.util.Vector;
import java.io.File;

import org.eclipse.swt.widgets.Display;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.jface.action.Action;

/**	Class specially customised to calculate time
 * 
 * 	This class can convert a number of milliseconds into
 * 	either minutes, hours, days, weeks, months, years
 * 
 * The number of seconds in 1000 milliseconds = 1
 * The number of minutes in 60000 milliseconds = 1
 * 
 * and so on.
 * 
 * @author Chris
 *
 */
class getTime{
	public static long time;

	public static String run(int duration, String t){
		long value = time;
		if(time > duration){
			value = (long)((float)time/duration);
			time-=(value*duration);
			return (String)(value+t+" ");			
		}
		
		return "";
	}
}

/**	Class specially customised to calculate data sizes
 * 
 * 	This class can take a number of bytes and turn it
 *	into either KB, MB or GB
 *
 * @author Chris
 *
 */
class getSize{
	public static double bytes;
	public static String postfix = "b";
	
	public static void run(String t){
		if(bytes > 1024){
			bytes/=1024;
			postfix = t;
		}
	}
	
	public static String getString(){
		DecimalFormat df = new DecimalFormat("0.##");
		return (String)(df.format(bytes)+postfix);
	}
}

public abstract class Transfer extends Job{
	protected FileList m_fileList = null;
	protected TransferDetails m_details;
	protected StringBuffer m_output = null;
	protected String m_errorPrefix;	
	
	//	Time variables
	protected static final int SECOND = 1;
	protected static final int MINUTE = 60;
	protected static final int HOUR = 60*MINUTE;
	protected static final int DAY = 24*HOUR;
	//	Just for a laugh
	protected static final int WEEK = 7*DAY;
	protected static final int MONTH = 4*WEEK; // average
	protected static final int YEAR = 12*MONTH;
	
	//	Performance calculation variables
	protected int m_fileCount = 0;
	protected int m_count = 0;
	protected long m_speed = 0;
	protected long m_bytes = 0;
	protected long m_bytesTotal = 0;
	protected long m_seconds = 0;
	protected double m_percent = 0;
	
	//	Job identification and control
	protected String m_jobid = "ESFTP";
	protected String m_key;	
	protected boolean m_open = false;
	protected boolean m_locked = false;
	protected IProgressMonitor m_monitor;
	protected String m_title = "SFTP Transfer";
	
	//	Dialog text templates
	public static final int TASK_CONNECT = 0;
	public static final int TASK_SCANNING = 1;
	public static final int TASK_TRANSFER = 2;
	public static final int TASK_COMPLETE = 3;
	public static final int TASK_ERROR = 4;
	
	public static final int STASK_SCANNING = 0;
	public static final int STASK_PREPARE = 1;
	public static final int STASK_TRANSFER = 2;
	
	public Transfer(){
		super("Eclipse SFTP Transfer");
	}
	
	/**	Used in the preferences pages when the user wants to test settings
	 */
	public void init(TransferDetails transfer){
		//System.out.println("TRACE-> Transfer::init(TransferDetails)");
		m_details = transfer;
		
		if(m_output == null) m_output = new StringBuffer();
	}	
	
	/**	Creates a transfer object for the SFTP site
	 * 
	 * @param fl	The FileList object containing the files to transfer
	 * @param key	The key identifying the Transfer object's association with a particular SFTP site
	 */
	public void init(FileList fl, String key){
		//System.out.println("TRACE-> Transfer::init(FileList, key");
		init(fl.getDetails());
		m_fileList = fl;
		m_key = key;
	}
	
	/**	Discovers what family this job belongs to
	 * 
	 * This is a method of letting you identify a job process by family
	 * so whole families of job processes can be stopped/cancelled/etc
	 * 
	 * @param family	The job are you searching against
	 */
	public boolean belongsTo(Object family){
		return m_jobid.equals(family);
	}
		
	public IStatus run(IProgressMonitor monitor){
		//System.out.println("TRACE-> Transfer::start()");
		//	start transferring all the files to the server		
		
		m_monitor = monitor;
		IStatus s = Status.OK_STATUS;
		
		//	optimise the file list, if returns a number of files/folders, open the SFTP server
		if(open() == true && m_fileList.optimise(this) > 0){
			String mode;
			if(m_fileList.getMode() == TransferDetails.GET){
				mode = "Downloading";
			}else{
				mode = "Uploading";
			}
			
			s = TransferItems(mode);

			if(isModal() == true){
				showResults();
			}else{
				setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
				setProperty(IProgressConstants.ACTION_PROPERTY, showResultsCallback());
			}
		}
		
		close();
		return s;
	}
	
	public boolean isModal(){
		Boolean m = (Boolean)getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
		
		if(m == null) return false;
		return m.booleanValue();
	}
	
	protected Action showResultsCallback(){
		final Transfer t = this;
		return new Action("View Transfer results") {
			public void run() {
				new TransferResultsConsole(t, m_fileList);
			}
		};
	}
	
	protected void showResults(){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				showResultsCallback().run();
			}
		});
	}
	
	public abstract boolean open();
	
	public abstract void close();	
	
	protected abstract void createRemoteDirectory(String dir);
	
	protected void createLocalDirectory(String dir){
		try{
			//	Open a file handle to the item, if it doesnt exist, throw an exception
			File f = new File(m_details.getLocalRoot()+dir);
			if(f.exists() == false) throw new Exception();
			
			//	If the item is a directory, set the status to exist, same for files
			if(f.isDirectory() == true){
				m_fileList.setStatus(m_count, FileList.DEXIST);
			}else{
				m_fileList.setStatus(m_count, FileList.DFEXIST);
				return;
			}
		}catch(Exception e){
			//	If the item doesnt exist, create a directory with the same name
			File f = new File(m_details.getLocalRoot()+dir);
			f.mkdir();
			m_fileList.setStatus(m_count, FileList.OK);
		}
	}

	protected IStatus TransferItems(String mode){
		IStatus s = Status.OK_STATUS;
		
		//		Grab the number of items to transfer
		m_fileCount = m_fileList.getNumItems();
		
		//	Now you have a list of files/folders and the server is opened, 
		//	set the local and remote directories
		if(setDirectories() == false){
			setTask(TASK_ERROR, new String[]{m_errorPrefix+"Failed to change remote directory to the Site root, or failed to create it"});
			return Status.CANCEL_STATUS; 
		}
		//	Start the monitor
		/*	10,000 means that you can track <1% updates
		 *	if you have completed 0.04% of the workload, eclipse
		 *	doesnt let you update by 0.04%, cause it's less than 1
		 *	you can't update at all.  So what I've done, is multiply each
		 *	unit by 100, which means 0.04 becomes 4, but therefore
		 *	to accomodate all 100% you have to also multiply that by 100
		 *	so 100% complete becomes 10,000 units of work, each 1% of the
		 *	work becomes 100 units, so therefore you can track <1% updates
		 *	(IS THIS A SHIT EXPLANATION? tell me a better one)
		 */		
		m_monitor.beginTask("Transferring files",10000);
		
		//	Set the monitor to show preparation step
		setSubTask(STASK_PREPARE,null);
		//	Create a new sftp monitor
		createMonitor();
		//	grab the full number of bytes to transfer
		m_bytesTotal = m_fileList.getSize();
		System.out.println("Number of bytes to transfer: "+m_bytesTotal);

		String item = null;
		for(m_count=0;m_count<m_fileCount;m_count++){			
			//	Pull an item from the list to transfer it
			item = m_fileList.getFile(m_count);
			
			//	Set the task and subtask strings to show transfer information
			setTask(TASK_TRANSFER, new String[] { mode, item });				
			
			int i;
			
			//	Build the necessary directories
			String dir = "";			
			while((i = item.indexOf("/")) > 0){
				//	Get the directory to create (attempt to)
				dir += item.substring(0,i)+"/";
				//	Remove that directory from the item being created
				item = item.substring(i+1);
				
				//	Create either a local or remote directory
				if(m_fileList.getMode() == TransferDetails.GET){
					createLocalDirectory(dir);
				}else{
					createRemoteDirectory(dir);
				}
			}
			
			//	If the item is left with a trailing string, it'll be a file, create it
			if(!item.equals("/") && item.length() > 0){
				try{
					transfer(dir,item);
					//	if no exception, means we are ok
					m_fileList.setStatus(m_count,FileList.OK);
				}catch(Exception e){
					//System.out.println("transfer exception: "+e.getMessage()+": cause: "+e.getCause());
					//	If exception, failed to put the file
					m_fileList.setStatus(m_count,FileList.FAILED);
				}
			}else{
				//	If the item was a directory it will set a status whether it created 
				//	or not, however if we have no status information for this directory
				//	something unknown happened, so set DUNKNOWN 
				//	say it, D unknown, Dee unknown, the unknown (get it?)
				if(m_fileList.getStatus(m_count).equals(FileList.NOSTATUS)){
					m_fileList.setStatus(m_count, FileList.DUNKNOWN);
				}
			}
			
			//	If the monitor is cancelled, you return 
			//	here, you don't want to continue
			if (m_monitor.isCanceled()){
				m_fileList.setStatus(m_count,FileList.CANCEL);
				return Status.CANCEL_STATUS; 
			}			
		}
		
		return s;
	}
	
	public abstract void createMonitor();
	
	public abstract void transfer(String dir, String item) throws Exception;
	
	public abstract void cancelTransfer();
	
	public abstract boolean setDirectories();
	
	//	TODO: pauses the transfer (not in mid-file, mid-batch)
	public void pause(){}

	public abstract void list(String directory, Vector files, Vector folders);
	
	public abstract long isFile(String filename) throws Exception;
	
	public abstract boolean test();
	
	//	TODO: Transfer::appendFile() This isnt implemented yet
	public void appendFilelist(FileList fl){
		//	Takes a file list and appends it to the end of the current list of files going to the host
		//System.out.println("TRACE-> Transfer::appendFileList()");
	}	
	
	public void update(long bytes, long bytesTotal)
	{
		if(m_monitor.isCanceled()){
			cancelTransfer();
		}else{
			setTransferSpeed(bytes,bytesTotal);
	
			setSubTask(TASK_TRANSFER, new String[] {
					getNumberFiles(),
					getTransferSize(),
					getPercentComplete(),
					getTransferSpeed(),
					getTimeLeft()
			});
		}
	}

	public void setTask(int template, String task[]){
		if(m_monitor != null){
			switch(template){
				case TASK_CONNECT:
					m_monitor.setTaskName("Connecting to SFTP Site");
				break;
				
				case TASK_SCANNING:
					m_monitor.setTaskName("Scanning directories");
				break;
				
				case TASK_TRANSFER:
					if(task.length == 2){ 
						m_monitor.setTaskName(task[0]+":\t"+task[1]);
					}
				break;
				
				case TASK_COMPLETE:
					m_monitor.setTaskName("Transfer completed");
				break;
				
				case TASK_ERROR:
					if(task.length >= 1){
						m_monitor.setTaskName("ERROR: "+task[0]);
						for(int a=0;a<task.length;a++){
							m_output.append(task[a]+"\n");
							System.out.println(task[a]);
						}
					}
				break;
			}
		}
	}
	
	public void setSubTask(int template, String subtask[]){
		if(m_monitor != null){
			switch(template){
				case STASK_SCANNING:
					if(subtask.length == 1) m_monitor.subTask(subtask[0]);
				break;
				
				case STASK_PREPARE:
					m_monitor.subTask("Preparing to transfer");
				break;
				
				case STASK_TRANSFER:
					if(subtask.length == 5){
						//	need percentage
						//	need time remaining
						//	need data transfer rate
						String text = "";
						text += subtask[0]+"\t"+subtask[1]+"\n";
						text += subtask[2]+"\t"+subtask[3]+"\t"+subtask[4];
						m_monitor.subTask(text);
					}else{
						m_monitor.subTask("Starting transfer....");
					}
				break;
			}			
		}
	}
	
	//	return the SHA1 key
	public String getKey(){
		return m_key;
	}
	
	public TransferDetails getTransferDetails(){
		return m_details;
	}
	
	public String getTransferOuput(){
		return m_output.toString();
	}	
	
	public String getNumberFiles(){
		return PaddedString.rpad("File: "+m_count+"/"+m_fileCount,25); 
	}
	
	public String getTransferSize(){
		return PaddedString.rpad(("Transferred: "+interpretBytes(m_bytes)+"/"+interpretBytes(m_bytesTotal)),25);
	}
	
	public String getPercentComplete(){
		double p =  ((double)m_bytes/m_bytesTotal)*100.0f;

		m_monitor.worked((int)((p-m_percent)*100));
		
		m_percent = p;

		DecimalFormat df= new DecimalFormat("0.##");
		return PaddedString.rpad(( df.format(m_percent) +" % Complete"),25);
	}
	
	public String getTimeLeft(){
		//	prevent divide by 0 zero
		if(m_speed != 0){
			//	Averages the speed + prevents divide by zero safe
			long time = (m_bytesTotal - m_bytes)/m_speed;
			
			String duration = "Time Remaining: ";
			
			getTime.time = time;			
			duration += getTime.run(YEAR,"y");
			duration += getTime.run(MONTH,"mo");
			duration += getTime.run(WEEK,"w");
			duration += getTime.run(DAY,"d");
			duration += getTime.run(HOUR,"h");
			duration += getTime.run(MINUTE,"m");
			duration += getTime.run(SECOND,"s");
						
			return PaddedString.rpad(duration,40);
		}
		
		return "Infinite time";
	}
	
	public void setTransferSpeed(long speed, long bytes){
		m_speed=speed;
		m_bytes+=bytes;
	}
	
	public String getTransferSpeed(){
		return PaddedString.rpad("Speed: "+interpretBytes(m_speed)+"/sec", 20);
	}
	
	protected String interpretBytes(long input){
		getSize.bytes = input;
		getSize.run("KB");
		getSize.run("MB");
		getSize.run("GB");
	
		return getSize.getString();
	}
}
