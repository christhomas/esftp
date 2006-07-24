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

package org.kmem.kosh.sftp;

import java.util.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.kmem.kosh.sftp.directory.FileList;
import org.kmem.kosh.sftp.properties.IProperty;
import org.kmem.kosh.sftp.ui.ConsoleDisplayMgr;
import org.osgi.framework.BundleContext;
import org.kmem.kosh.sftp.FilterWriter;

/**
 * The main plugin class to be used in the desktop.
 */
public class SftpPlugin extends AbstractUIPlugin {
	//	The shared instance
	private static SftpPlugin m_plugin;
	//	FilterWriter debug class
	private FilterWriter m_output;
	
	protected Digest m_hash;
	protected Vector m_transfer;
	protected boolean m_state = false;
	
	public static ConsoleDisplayMgr cons = ConsoleDisplayMgr.getDefault("ESftp Console");
	
	/**
	 * The constructor.
	 */
	public SftpPlugin() {
		m_plugin = this;
		
		//	Create a new FilterWriter object + enable console output
		m_output = new FilterWriter();
		m_output.enableConsole(true);
		//	create a new Digest object
		m_hash = new Digest();
		//	Create a new Transfer Vector
		m_transfer = new Vector();
	}
	
	public FilterWriter getFilterWriter(){
		return m_output;
	}
	
	public static void consolePrint(String msg, int msgKind) {
		cons.print(msg,msgKind);
	}
	
	public static void consolePrintln(String msg, int msgKind) {
		cons.println(msg,msgKind);
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		return SftpPlugin.getActiveWorkbenchWindow().getActivePage();
	}
	
	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		//System.out.println("TRACE-> initializeDefaultPreferences()");
		store.setDefault(IProperty.SERVER,"<Enter server address>");
		store.setDefault(IProperty.PORT,22);
		store.setDefault(IProperty.TIMEOUT, 30);
		store.setDefault(IProperty.USERNAME,"<Enter Username>");
		store.setDefault(IProperty.PASSWORD,"");
		store.setDefault(IProperty.SAVEPWD,true);
		store.setDefault(IProperty.RECURSE,true);
		store.setDefault(IProperty.EMPTY,true);
		store.setDefault(IProperty.SITEROOT,"");
	}	
	
	public void setDefaultPreferences(){
		IPreferenceStore store = getDefault().getPreferenceStore();
		store.setToDefault(IProperty.SERVER);
		store.setToDefault(IProperty.PORT);
		store.setToDefault(IProperty.TIMEOUT);
		store.setToDefault(IProperty.USERNAME);
		store.setToDefault(IProperty.PASSWORD);
		store.setToDefault(IProperty.SAVEPWD);
		store.setToDefault(IProperty.RECURSE);
		store.setToDefault(IProperty.EMPTY);
		store.setToDefault(IProperty.SITEROOT);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		m_plugin = null;
	}

	/**
	 * Returns the shared instance
	 */
	public static SftpPlugin getDefault() {
		return m_plugin;
	}	

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.kmem.kosh.sftp", path);
	}
	
	/**
	 * Allows TransferDetail objects to obtain the hash object in order to compute their unique I.D string
	 * @return An object to create a hash with
	 */
	public Digest getHash(){
		return m_hash;
	}
	
	public Transfer getTransfer(){
		//return new TransferJsch();
		return new TransferSSHTools();
	}
	
	/**
	 * Add a list of files to be transferred, but only if that server is unique
	 * 
	 * This will find the key in the file list, attempt to find a server which matches it, if it finds one
	 * it will append the list of files to that existing transfer, otherwise, it will add another one.
	 * 
	 * @param fl The list of files to transfer
	 */
	public void add(FileList fl){
		//System.out.println("TRACE-> SftpPlugin::add()");
		String key = fl.getKey();
			
		if(key != null){
			//System.out.println("SftpPlugin::add(), find transfer object");
			for(int a=0;a<m_transfer.size();a++){
				Transfer t = (Transfer)m_transfer.get(a);
				if(key == t.getKey()){
					System.out.println("SftpPlugin::add(), found existing transfer object");
					t.appendFilelist(fl);
					return;
				}
			}
			
			//System.out.println("SftpPlugin::add(), couldnt find existing transfer object, create new");
			Transfer t = getTransfer();
			t.init(fl,key);
			m_transfer.add(t);
			t.setRule(fl.getProject());
			t.setUser(true);
			t.schedule();
		}
	}
	
	/**
	 * Removes a Transfer object from the queue of objects currently running
	 * 
	 * Removal of a transfer occurs either when the transfer was cancelled, either locally or remotely
	 * Or the transfer completed, hence needs to remove itself from the list
	 * 
	 * @param t	The transfer to remove from the list of current transfers
	 */
	public void remove(Transfer t){
		//System.out.println("SftpPlugin::remove(), removing transfer object, it's finished");
		for(int a=0;a<m_transfer.size();a++){
			Transfer tmp = (Transfer)m_transfer.get(a);
			if(t.getKey() == tmp.getKey()){
				//System.out.println("SftpPlugin::remove(), found transfer object, removing");
				m_transfer.remove(a);
			}
		}
	}
}
