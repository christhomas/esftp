package com.antimatterstudios.esftp.properties;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.DefaultScope;

public class WorkspacePreferences extends EsftpPreferences
{	
	public WorkspacePreferences()
	{
		super(new InstanceScope().getNode(EsftpPreferences.m_projectID));
	}
	
	public void restoreDefaults(){
		clear();
		m_preferences = new InstanceScope().getNode(EsftpPreferences.m_projectID);
		clone(new EsftpPreferences(new DefaultScope().getNode(EsftpPreferences.m_projectID)));
	}			
}
