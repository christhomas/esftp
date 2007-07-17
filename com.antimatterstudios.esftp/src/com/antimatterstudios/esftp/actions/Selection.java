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

package com.antimatterstudios.esftp.actions;

import com.antimatterstudios.esftp.Activator;
import com.antimatterstudios.esftp.directory.FileList;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

abstract public class Selection implements IObjectActionDelegate {
	
	/**
	 * The selection
	 */
    protected IStructuredSelection m_selection;
	protected Shell m_shell;
	protected Vector<FileList> m_fileList;
	
	public Selection() {
		super();

		//	use for all the message dialogs you might want
		m_shell = new Shell();
		m_fileList = new Vector<FileList>();
	}
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
	
	public abstract void run(IAction action);
	
	protected boolean buildList(int transferType){
		//System.out.println("TRACE-> Base::buildList()");
		
		Object res[] = m_selection.toArray();
		
		for(int a=0;a<res.length;a++){
			if (res[a] instanceof IResource) {
				IResource r = (IResource)res[a];

				FileList fl = findProject(r, transferType);
				if(fl == null) return false;
				
				String str = r.getLocation().toPortableString();
				
				switch(r.getType()){
					case IResource.FILE:
						fl.addFile(str);
					break;

					case IResource.FOLDER:
					case IResource.PROJECT:
						fl.addFolder((IContainer)r);
					break;
					//SftpPlugin.consolePrintln(str,1);
				} 				
			}
		}
		
		sendList();
		
		return true;
	}
	
	private void sendList(){
		//	loop through all the FileList objects, 
		//	removing them one by one, optimising 
		//	them and sending them to SftpPlugin to transfer
		while(m_fileList.size() > 0){
			FileList fl = (FileList)m_fileList.remove(0);
			Activator.getDefault().add(fl);
		}
	}
	
	private FileList findProject(IResource r, int transferType){
		//System.out.println("Base::findProject()");
		
		IProject p = r.getProject();
		FileList fl = null;
		
		//	project is null? return null, most probable reason is that resource is a workbench
		if(p == null) return null;
		
		//	loop through the projects you got, look for a match
		for(int a=0;a<m_fileList.size();a++){
			fl = (FileList)m_fileList.get(a);
			if(p == fl.getProject()){
				//System.out.println("Base::findProject(), found FileList, returning it");
				//	found one, return it
				return fl;
			}
		}
		
		//System.out.println("Base::findProject(), couldnt find FileList, create a new one");
		//	didnt find one, create a new one, add it, return it
		fl = new FileList(p);
		fl.init(transferType);
		//System.out.println("Base::findProject(), FileList initialised ok");
		m_fileList.add(fl);
			
		return fl;
	}
	
	/**
	 * Opens an error dialog with the specified message.
	 * @param message the message to display.
	 */
	void showError(String message) {
	    MessageDialog.openError(m_shell,"Error Deploying","Error while deploying file.\nCause:" + message);
	}
	
	/**
	 * Opens an error dialog with the message from the specified exception.
	 * @param e an exception that contains a message
	 */
	void showError(Exception e) {
	    showError(e.getMessage());
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */	
 	public void selectionChanged(IAction action, ISelection s) {
 		if (s != null) {
 			if (s instanceof IStructuredSelection){
 				m_selection = (IStructuredSelection)s;
 				//SftpPlugin.consolePrintln(selection.toString(),1);
 				//SftpPlugin.consolePrintln(ObjectDumper.dumpObject(selection),1);
 			}
 			// added this to capture if selection is the current editor view
 			
 			 if (s instanceof ITextSelection){
 				IEditorPart part = Activator.getActivePage().getActiveEditor();
 				if (part != null) {
 					IEditorInput input = part.getEditorInput();
 					IResource r = (IResource) input.getAdapter(IResource.class);
 					if (r != null) {
 						switch(r.getType()){
 							case IResource.FILE:
 								m_selection = new StructuredSelection(r);
 								//SftpPlugin.consolePrintln("Set:"+this.selection.toString(),1);								
 							break;
 						}
 					}	//	set selection to current editor file;
 				}
 			}else{			
 				//SftpPlugin.consolePrintln("Not Structure",1);								
 				//SftpPlugin.consolePrintln(ObjectDumper.dumpObject(selection),1);
 			}
 		}	
 	}	
}
