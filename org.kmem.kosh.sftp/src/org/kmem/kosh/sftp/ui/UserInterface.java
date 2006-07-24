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

package org.kmem.kosh.sftp.ui;

import org.kmem.kosh.sftp.*;
import org.kmem.kosh.sftp.properties.IProperty;
import org.kmem.kosh.sftp.properties.PropertyStore;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.jface.preference.IPreferenceStore;

public class UserInterface {
	private Text m_server;
	private Spinner m_port;
	private Spinner m_timeout;
	private Text m_username;
	private Text m_password;
	private Button m_savePwd;
	private Button m_recurse;
	private Button m_createEmpty;
	private Text m_siteRoot;
	private Button m_test;
	private Text m_serverOutput;
	PropertyStore m_store, m_backup;
	Listener m_modifyListener, m_testListener;
	
	public UserInterface(){}
	
	public Composite open(Composite parent, PropertyStore store){
		TabFolder tabFolder = new TabFolder(parent,SWT.NONE);
		
		//	create the main esftp preferences tab
		TabItem main = new TabItem(tabFolder, SWT.NULL);
		main.setText("ESFTP: Preferences");
		main.setControl(openMainPreferences(tabFolder,store));
		
		//	create the keybinding tab
		TabItem keyb = new TabItem(tabFolder,SWT.NULL);
		keyb.setText("ESFTP: Key Bindings");
		keyb.setControl(openKeyBindingPreferences(tabFolder,store));				
		
		return tabFolder;
	}
	
	public Composite openMainPreferences(Composite parent, PropertyStore store){		
		m_store = store;
		m_backup = null; 
		
		m_modifyListener = new Listener(){
			public void handleEvent(Event e){
				if(e.type == SWT.Modify){
					if((m_store.getBoolean(IProperty.VERIFIED) == true)  && (compare() == false)){
						//System.out.println("Widget was modified");
						m_store.setValue(IProperty.VERIFIED, false);
						if(m_backup == null) m_backup = new PropertyStore(m_store);
					}
				}
			}
		};
		
		m_testListener = new Listener(){
			public void handleEvent(Event e){
				if(e.widget == m_test){
					test();
				}
			}
		};
		
		Composite c = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		c.setLayout(layout);
		
		GridData data = new GridData(
				GridData.FILL_HORIZONTAL |	GridData.FILL_VERTICAL | 
				GridData.GRAB_HORIZONTAL |	GridData.GRAB_VERTICAL);
		c.setLayoutData(data);		
				
		Label labelServer = new Label(c, SWT.NONE);
		labelServer.setText("Server");
		m_server = new Text(c, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		m_server.setLayoutData(data);
		
		new Label(c, SWT.NONE).setText("Port");
		m_port = new Spinner(c, SWT.BORDER);
		m_port.setMinimum(0);
		m_port.setMaximum(65535);
		
		new Label(c, SWT.NONE).setText("Username");
		m_username = new Text(c, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		m_username.setLayoutData(data);
		
		new Label(c, SWT.NONE).setText("Connection Timeout");
		m_timeout = new Spinner(c, SWT.BORDER);
		m_timeout.setMinimum(0);
		m_timeout.setMaximum(65535);
		
		new Label(c, SWT.NONE).setText("Password");
		m_password = new Text(c, SWT.SINGLE | SWT.BORDER);
		m_password.setEchoChar('*');
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		m_password.setLayoutData(data);
		
		m_savePwd = new Button(c, SWT.CHECK);
		m_savePwd.setText("Save");
		data = new GridData();
		data.horizontalSpan = 2;
		m_savePwd.setLayoutData(data);
		
		String passwordWarning = "WARNING: Saving passwords in eclipse preferences can be insecure";
		Label warning = new Label(c, SWT.NONE);
		warning.setText(passwordWarning);
		data = new GridData();
		data.horizontalSpan = 4;
		warning.setLayoutData(data);
		
		Composite filler = new Composite(c, SWT.NONE);
		data = new GridData();
		data.horizontalSpan = 4;
		filler.setLayoutData(data);		
				
		m_recurse = new Button(c, SWT.CHECK);
		m_recurse.setText("Recurse Directories");
		data = new GridData();
		data.horizontalSpan = 2;
		m_recurse.setLayoutData(data);
		
		m_createEmpty = new Button(c, SWT.CHECK);
		m_createEmpty.setText("Create Empty Directories");
		data = new GridData();
		data.horizontalSpan = 2;
		m_createEmpty.setLayoutData(data);
		
		new Label(c, SWT.NONE).setText("Site Root");
		m_siteRoot = new Text(c, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = 3;
		m_siteRoot.setLayoutData(data);
		
		m_test = new Button(c, SWT.NONE);
		m_test.setText("Test settings");
		m_serverOutput = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.horizontalSpan = 3;
		data.verticalSpan = 5;
		m_serverOutput.setLayoutData(data);
		m_serverOutput.setText("<ESFTP Plugin> Ready to test");	
		
		update();
		setCallbacks();
		
		return c;
	}	
	
	public Composite openKeyBindingPreferences(Composite parent, PropertyStore store){
		Composite c = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		c.setLayout(layout);
		
		GridData data = new GridData(
				GridData.FILL_HORIZONTAL |	GridData.FILL_VERTICAL | 
				GridData.GRAB_HORIZONTAL |	GridData.GRAB_VERTICAL);
		c.setLayoutData(data);			
		
		Text text = new Text(c, SWT.NONE);
		text.setText("Keybindings");
		
		return c;
	}
	
	public boolean close(IPreferenceStore store){
		boolean modify = compare();
		
		if(modify == false){
			store.setValue(IProperty.SERVER,m_server.getText());
			store.setValue(IProperty.PORT,m_port.getSelection());
			store.setValue(IProperty.TIMEOUT,m_timeout.getSelection());
			store.setValue(IProperty.USERNAME,m_username.getText());
			store.setValue(IProperty.PASSWORD,m_password.getText());
			store.setValue(IProperty.SAVEPWD,m_savePwd.getSelection());
			store.setValue(IProperty.RECURSE,m_recurse.getSelection());
			store.setValue(IProperty.EMPTY,m_createEmpty.getSelection());
			store.setValue(IProperty.SITEROOT,m_siteRoot.getText());
		}
		
		return modify;
	}	
	
	private void setCallbacks(){
		m_server.addListener(SWT.Modify, m_modifyListener);
		m_port.addListener(SWT.Modify, m_modifyListener);
		m_username.addListener(SWT.Modify, m_modifyListener);
		m_timeout.addListener(SWT.Modify, m_modifyListener);
		m_password.addListener(SWT.Modify, m_modifyListener);
		m_savePwd.addListener(SWT.Modify, m_modifyListener);
		m_recurse.addListener(SWT.Modify, m_modifyListener);
		m_createEmpty.addListener(SWT.Modify, m_modifyListener);
		m_siteRoot.addListener(SWT.Modify, m_modifyListener);
		m_test.addListener(SWT.Selection, m_testListener);		
	}
	
	private boolean compare(){
		if( !m_store.getString(IProperty.SERVER).equals( m_server.getText()) ) return false;
		if( m_store.getInt(IProperty.PORT) != m_port.getSelection() ) return false;
		if( m_store.getInt(IProperty.TIMEOUT) != m_timeout.getSelection() ) return false;
		if( !m_store.getString(IProperty.USERNAME).equals( m_username.getText()) ) return false;
		if( !m_store.getString(IProperty.PASSWORD).equals( m_password.getText()) ) return false;
		if( m_store.getBoolean(IProperty.SAVEPWD) != m_savePwd.getSelection() ) return false;
		if( m_store.getBoolean(IProperty.RECURSE) != m_recurse.getSelection() ) return false;
		if( m_store.getBoolean(IProperty.EMPTY) != m_createEmpty.getSelection() ) return false;
		if( !m_store.getString(IProperty.SITEROOT).equals( m_siteRoot.getText()) ) return false;
		
		//System.out.println("UserInterface::compare(), nothing changed");
	
		return true;
	}
	
	public void test(){
		m_serverOutput.setText("<Testing SFTP Site>\nPlease Wait....\n");
		m_serverOutput.update();
		
		close(m_store);
		TransferDetails details = new TransferDetails(m_store);
		Transfer transfer = SftpPlugin.getDefault().getTransfer();
		transfer.init(details);
		
		m_store.setValue(IProperty.VERIFIED, transfer.test() );
		m_serverOutput.append( transfer.getTransferOuput() );
	}
	
	public void update(){
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
		}
		m_store.debug();
	}
	
	public void restoreDefaults(PropertyStore newstore){
		if(m_backup != null){
			m_store.clone(m_backup);
			m_backup = null;		
		}
		update();
	}
	
	public void update(PropertyStore store){
		m_store = store;
		update();
	}
}
