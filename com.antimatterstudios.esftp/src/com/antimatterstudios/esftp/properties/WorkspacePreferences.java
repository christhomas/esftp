package com.antimatterstudios.esftp.properties;

import org.eclipse.core.runtime.preferences.InstanceScope;

import com.antimatterstudios.esftp.Activator;

public class WorkspacePreferences extends EsftpPreferences
{	
	protected void setupScope()
	{
		m_original = new InstanceScope().getNode(Activator.PLUGIN_ID);
	}
	
	public WorkspacePreferences()
	{
		setupWorkingCopy();
	}
}
