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
import com.antimatterstudios.esftp.TransferDetails;
import com.antimatterstudios.esftp.properties.IProperty;
import com.antimatterstudios.esftp.properties.EsftpPreferences;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;

public class UserInterface {
	//	Main interface widgets
	private Text m_server;
	private Spinner m_port;
	private Combo m_protocol;
	private Spinner m_timeout;
	private Text m_username;
	private Text m_password;
	private Button m_savePwd;
	private Button m_recurse;
	private Button m_createEmpty;
	private Text m_siteRoot;
	private Button m_siteRootBrowse;
	private Button m_test;
	private Text m_serverOutput;
	//	Ignore File widgets
	private List m_ignoreFile;
	private Button m_removePattern;
	private Text m_enterPattern;
	private Button m_addPattern;
	private Button m_updatePattern;
	
	EsftpPreferences m_store;
	Listener m_modifyListener, m_testListener, m_siteBrowser;
	
	public UserInterface(){}
	
	public Composite open(Composite parent, EsftpPreferences store){
		TabFolder tabFolder = new TabFolder(parent,SWT.NONE);
		
		setupCallbacks();
		
		m_store = store;
		
		//	create the main esftp preferences tab
		TabItem main = new TabItem(tabFolder, SWT.NULL);
		main.setText("Preferences");
		main.setControl(openMainPreferences(tabFolder));
		
		//	create the keybinding tab
		TabItem keyb = new TabItem(tabFolder,SWT.NULL);
		keyb.setText("Key Bindings");
		keyb.setControl(openKeyBindingPreferences(tabFolder));		
		
		TabItem ignore = new TabItem(tabFolder,SWT.NULL);
		ignore.setText("Ignore Files");
		ignore.setControl(openIgnoreFilePreferences(tabFolder));
		
		updateInterface();
		
		return tabFolder;
	}
	
	protected void setupCallbacks()
	{
		m_modifyListener = new Listener(){
			public void handleEvent(Event e){
				if(e.type == SWT.Modify){
					if((m_store.getBoolean(IProperty.VERIFIED) == true)  && (compare() == false)){
						//System.out.println("Widget was modified");
						m_store.putBoolean(IProperty.VERIFIED, false);
					}
				}
			}
		};
		
		m_testListener = new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_test) test();
			}
		};
		
		m_siteBrowser = new Listener(){
			public void handleEvent(Event e){
				Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				messageBox.setMessage("Coming soon: to an eclipse near you!");
				messageBox.setText("This feature is disabled");
				messageBox.open();
				shell.dispose();
			}
		};
	}
	
	public Composite openMainPreferences(Composite parent)
	{		
		Composite c = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		c.setLayout(layout);
		
		c.setLayoutData(new GridData(GridData.FILL_BOTH));		
				
		Label labelServer = new Label(c, SWT.NONE);
		labelServer.setText("Server");
		m_server = new Text(c, SWT.SINGLE | SWT.BORDER);
		m_server.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		m_server.addListener(SWT.Modify, m_modifyListener);		
		
		new Label(c, SWT.NONE).setText("Port");
		m_port = new Spinner(c, SWT.BORDER);
		m_port.setMinimum(0);
		m_port.setMaximum(65535);
		m_port.addListener(SWT.Modify, m_modifyListener);
	
		new Label(c,SWT.NONE).setText("Protocol");
		String items[] = { "SFTP", "FTP", "FTP/TLS (Disabled)", "FTP/SSL (Disabled)" };
		m_protocol = new Combo(c, SWT.BORDER);		
		m_protocol.setItems(items);
		m_protocol.select(0);
		m_protocol.setLayoutData(new GridData(SWT.FILL, SWT.NONE,true,false,1,1));
		m_protocol.addListener(SWT.Modify, m_modifyListener);
		
		new Label(c, SWT.NONE).setText("Connection Timeout");
		m_timeout = new Spinner(c, SWT.BORDER);
		m_timeout.setMinimum(0);
		m_timeout.setMaximum(65535);		
		m_timeout.addListener(SWT.Modify, m_modifyListener);		
		
		new Label(c, SWT.NONE).setText("Username");
		m_username = new Text(c, SWT.SINGLE | SWT.BORDER);
		m_username.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		m_username.addListener(SWT.Modify, m_modifyListener);		
		
		new Label(c,SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false, 2, 1));
		
		new Label(c, SWT.NONE).setText("Password");
		m_password = new Text(c, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		m_password.setEchoChar('*');
		m_password.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		m_password.addListener(SWT.Modify, m_modifyListener);
		
		m_savePwd = new Button(c, SWT.CHECK);
		m_savePwd.setText("Save Password");
		m_savePwd.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
		m_savePwd.addListener(SWT.Modify, m_modifyListener);		
		
		new Label(c,SWT.NONE);	
		Label warning = new Label(c, SWT.WRAP);
		warning.setText("WARNING: Saving passwords in eclipse preferences can be insecure as they are stored as plain text (HOWEVER AT THIS TIME YOU CANNOT PROCEED WITHOUT USING THIS OPTION)");
		warning.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
						
		new Label(c,SWT.NONE);		
		m_recurse = new Button(c, SWT.CHECK);
		m_recurse.setText("Recurse Directories");
		m_recurse.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
		m_recurse.addListener(SWT.Modify, m_modifyListener);
		
		new Label(c,SWT.NONE);		
		m_createEmpty = new Button(c, SWT.CHECK);
		m_createEmpty.setText("Create Empty Directories");
		m_createEmpty.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 3, 1));
		m_createEmpty.addListener(SWT.Modify, m_modifyListener);		
		
		new Label(c, SWT.NONE).setText("Site Root");
		m_siteRoot = new Text(c, SWT.SINGLE | SWT.BORDER);
		m_siteRoot.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false,2,1));
		m_siteRoot.addListener(SWT.Modify, m_modifyListener);		
		
		m_siteRootBrowse = new Button(c,SWT.NONE);
		m_siteRootBrowse.setText("Browse");
		m_siteRootBrowse.addListener(SWT.Selection,m_siteBrowser);
		
		m_test = new Button(c, SWT.NONE);
		m_test.setText("Test settings");
		m_test.addListener(SWT.Selection, m_testListener);				
		
		m_serverOutput = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		m_serverOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 5));
		m_serverOutput.setText("<ESFTP Plugin (version: "+Activator.getDefault().getVersion()+") > Ready to test");	
		
		return c;
	}	
	
	public Composite openKeyBindingPreferences(Composite parent){
		Composite c = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		c.setLayout(layout);
		
		Label l = new Label(c,SWT.CENTER);
		l.setText("These options are currently not active");
		l.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false,4, 1));
		
		String modifiers[] = { "ALT", "CTRL", "SHIFT", "ALT-GR" };
		Combo combo;
		Text entry;
		
		new Label(c, SWT.NONE).setText("Download File from server");
		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(0);

		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(1);
		
		entry = new Text(c, SWT.SINGLE | SWT.BORDER);
		entry.setText("<Enter Key>");
		
		new Label(c, SWT.NONE).setText("Upload to server");
		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(0);

		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(1);
		
		entry = new Text(c, SWT.SINGLE | SWT.BORDER);
		entry.setText("<Enter Key>");
		
		new Label(c, SWT.NONE).setText("Save file and upload to server");
		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(0);

		combo = new Combo(c, SWT.NONE);
		combo.setItems(modifiers);
		combo.select(1);
		
		entry = new Text(c, SWT.SINGLE | SWT.BORDER);
		entry.setText("<Enter Key>");
		
		return c;
	}
	
	public Composite openIgnoreFilePreferences(Composite parent)
	{
		Composite c = new Composite(parent,SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		c.setLayout(layout);
		
		new Label(c,SWT.NONE).setText("Active Patterns");
		
		m_ignoreFile = new List(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);		
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.heightHint = m_ignoreFile.getItemHeight() * 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		m_ignoreFile.setLayoutData(gridData);

		m_ignoreFile.addSelectionListener(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					List list = (List) e.getSource();
					String[] str = list.getSelection();
					for(int i=0;i<str.length;i++){
						System.out.println("Selection: "+str[i]);
					}
				}
			}
		);
		
		new Label(c,SWT.NONE);
		
		m_removePattern = new Button(c,SWT.NONE);
		m_removePattern.setText("Remove Selected");
		m_removePattern.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false,2,1));
		m_removePattern.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_removePattern){
					Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING);
					messageBox.setMessage("Are you sure you want to remove these ignore Filters?");
					messageBox.setText("Removing Ignore Filters");
					if(messageBox.open() == SWT.OK) 	m_ignoreFile.remove(m_ignoreFile.getSelectionIndices());
					shell.dispose();
				}
			}
		});

		new Label(c, SWT.NONE).setText("Enter Pattern");
		
		m_enterPattern = new Text(c, SWT.SINGLE | SWT.BORDER);
		m_enterPattern.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1,1));
		
		m_addPattern = new Button(c, SWT.NONE);
		m_addPattern.setText("Add");
		m_addPattern.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_addPattern){
					m_ignoreFile.add(m_enterPattern.getText());
					m_enterPattern.setText("");
				}
			}
		});
		
		new Label(c, SWT.NONE);
		
		m_updatePattern = new Button(c,SWT.CHECK);
		m_updatePattern.setText("Update selected Pattern (default)");
		m_updatePattern.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false,2,1));
		
		return c;
	}
	
	public void test(){
		m_serverOutput.setText("<Testing SFTP Site>\nPlease Wait....\n");
		m_serverOutput.update();
		
		updateStore();
		TransferDetails details = new TransferDetails(m_store);
		Transfer transfer = Activator.getDefault().getTransfer();
		transfer.init(details);
		
		try{
			m_store.putBoolean(IProperty.VERIFIED, transfer.test() );
		}catch(NullPointerException e){
			System.out.println("NPE detected whilst setting VERIFIED to the result of transfer.test()");
		}
		m_serverOutput.append( transfer.getTransferOuput() );
	}
	
	/**
	 * You are closing the user interface, you therefore have to grab all the data from 
	 * the interface and store it in the store which is passed to you
	 * 
	 * @param store
	 * @return
	 */
	public boolean close(){
		boolean modify = compare();
		
		if(modify == false) updateStore();
		
		return modify;
	}	
		
	private boolean compare(){
		try{
			if(!m_store.getString(IProperty.SERVER).equals(m_server.getText()) ) return false;
			if( m_store.getInt(IProperty.PORT) != m_port.getSelection() ) return false;
			if( m_store.getInt(IProperty.TIMEOUT) != m_timeout.getSelection() ) return false;
			if(!m_store.getString(IProperty.USERNAME).equals( m_username.getText()) ) return false;
			if(!m_store.getString(IProperty.PASSWORD).equals( m_password.getText()) ) return false;
			if( m_store.getBoolean(IProperty.SAVEPWD) != m_savePwd.getSelection() ) return false;
			if( m_store.getBoolean(IProperty.RECURSE) != m_recurse.getSelection() ) return false;
			if( m_store.getBoolean(IProperty.EMPTY) != m_createEmpty.getSelection() ) return false; 
			if(!m_store.getString(IProperty.SITEROOT).equals( m_siteRoot.getText()) ) return false;
		}catch(NullPointerException e){
			System.out.println("Caught exception comparing preferences, this really shouldnt happen");
			System.out.println("Exception was from here: "+e.getMessage());
			System.out.println("The cause was: "+e.getCause());
			return false;
		}
		//System.out.println("UserInterface::compare(), nothing changed");
	
		return true;
	}
	
	public void updateInterface(){
		if(compare() == false){
			m_server.setText(m_store.getString(IProperty.SERVER));
			m_port.setSelection(m_store.getInt(IProperty.PORT));
			m_timeout.setSelection(m_store.getInt(IProperty.TIMEOUT));
			m_username.setText(m_store.getString(IProperty.USERNAME));
			m_password.setText(m_store.getString(IProperty.PASSWORD));
			m_savePwd.setSelection(m_store.getBoolean(IProperty.SAVEPWD));
			m_recurse.setSelection(m_store.getBoolean(IProperty.RECURSE));
			m_createEmpty.setSelection(m_store.getBoolean(IProperty.EMPTY));
			m_siteRoot.setText(m_store.getString(IProperty.SITEROOT));	
			m_store.save();
		}else System.out.println("Compare(), returned true");
		m_store.debug();
	}
	
	protected void updateStore()
	{
		try{
			m_store.putString(IProperty.SERVER,m_server.getText());
			m_store.putInt(IProperty.PORT,m_port.getSelection());
			m_store.putInt(IProperty.TIMEOUT,m_timeout.getSelection());
			m_store.putString(IProperty.USERNAME,m_username.getText());
			m_store.putString(IProperty.PASSWORD,m_password.getText());
			m_store.putBoolean(IProperty.SAVEPWD,m_savePwd.getSelection());
			m_store.putBoolean(IProperty.RECURSE,m_recurse.getSelection());
			m_store.putBoolean(IProperty.EMPTY,m_createEmpty.getSelection());				
			m_store.putString(IProperty.SITEROOT,m_siteRoot.getText());
			m_store.save();
		}catch(NullPointerException e){
			System.out.println("Caught exception saving preferences, this really shouldnt happen");
			System.out.println("Exception was from here: "+e.getMessage());
			System.out.println("The cause was: "+e.getCause());
		} //	Maybe one of these values is NULL??
	}
	
	public void restoreDefaults(EsftpPreferences newstore){
		m_store.restore();
		updateInterface();
	}
	
	public void update(EsftpPreferences store){
		m_store = store;
		updateInterface();
	}
}
