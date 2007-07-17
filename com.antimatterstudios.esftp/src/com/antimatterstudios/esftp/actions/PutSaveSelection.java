/******************************************************************************
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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import com.antimatterstudios.esftp.Activator;

public class PutSaveSelection extends PutSelection implements IWorkbenchWindowActionDelegate{
	public PutSaveSelection(){ super(); }
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow) 
	 */
	public void init(IWorkbenchWindow window) {}
	public void dispose(){} 
	
	/**
	 * This method is called when the menu item is selected.
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//System.out.println("TRACE-> SecureFTP: Mode[PUT]");
		IEditorPart part = Activator.getActivePage().getActiveEditor();
		if (part != null) {
			if (part.isDirty()) {
				Activator.consolePrintln("Dirty",1);
				part.doSave(new NullProgressMonitor());
			}
		}
		super.run(action);
	}
}
