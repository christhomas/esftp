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

package com.antimatterstudios.esftp.ui;

import com.antimatterstudios.esftp.Activator;
import com.antimatterstudios.esftp.Transfer;
import com.antimatterstudios.esftp.directory.FileList;

import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;

public class TransferResultsDialog extends TransferResults {
	protected Shell m_shell = null;
	protected Label m_text = null;
	protected Text m_results = null;
	protected Button m_button = null;
	/**
	 * This method initializes the shell
	 */
	public TransferResultsDialog(Transfer t, FileList fl){
		super(t,fl);
	
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData data;
		
		m_shell = new Shell(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		m_shell.setText("ESFTP Results Viewer");
		m_shell.setLayout(gridLayout);
		m_shell.setSize(new org.eclipse.swt.graphics.Point(450,300));
		
		m_text = new Label(m_shell, SWT.NONE);
		m_text.setText("The results of the transfer are as follows: ");
		data = new GridData();
		data.horizontalSpan = 2;
		m_text.setLayoutData(data);
		
		m_results = new Text(m_shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		m_results.setLayoutData(data);
				
		m_button = new Button(m_shell, SWT.NONE);
		m_button.setText("OK");
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		data.horizontalSpan = 2;
		data.widthHint = 50;
		m_button.setLayoutData(data);
		
		m_button.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_button){
					m_shell.close();
				}
			}
		});
		
		m_shell.addShellListener(new ShellListener(){
			public void shellActivated(ShellEvent e){}
			public void shellDeactivated(ShellEvent e){}
			public void shellIconified(ShellEvent e){	}
			public void shellDeiconified(ShellEvent e){}
			public void shellClosed(ShellEvent e) {
				Activator.getDefault().remove(m_transfer);	
				m_shell.dispose();
			} 
		});
		m_shell.open();
		
		for(int a=0;a<m_fileList.getNumItems();a++){
			m_results.append(m_fileList.getStatusString(a));
			m_results.update();
		}		
	}
}
