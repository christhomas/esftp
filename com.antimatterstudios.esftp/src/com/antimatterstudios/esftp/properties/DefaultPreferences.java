package com.antimatterstudios.esftp.properties;

import org.eclipse.core.runtime.preferences.DefaultScope;

import com.antimatterstudios.esftp.Activator;

public class DefaultPreferences extends EsftpPreferences
{
	protected void setupScope()
	{
		m_original = new DefaultScope().getNode(Activator.PLUGIN_ID);
	}
	
	protected void setupWorkingCopy()
	{
		setupScope();
		m_preferences = m_original;
	}	
	
	public DefaultPreferences()
	{
		setupWorkingCopy();
		
		putString(IProperty.SERVER, "<Enter server address>");
		putInt(IProperty.PORT,22);
		putInt(IProperty.PROTOCOL,0);
		putInt(IProperty.TIMEOUT, 30);
		putString(IProperty.USERNAME,"<Enter Username>");
		putString(IProperty.PASSWORD,"");
		putBoolean(IProperty.SAVEPWD,true);
		putBoolean(IProperty.RECURSE,true);
		putBoolean(IProperty.EMPTY,true);
		putString(IProperty.SITEROOT,"<Enter Site Root>");
		
		debug();
	}
}