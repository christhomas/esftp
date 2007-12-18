package com.antimatterstudios.esftp.properties;

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.eclipse.ui.preferences.WorkingCopyManager;
import com.antimatterstudios.esftp.Activator;

public class EsftpPreferences
{
	protected IScopeContext[] m_searchScope;
	
	protected IEclipsePreferences m_preferences;
	
	protected IEclipsePreferences m_original;
	
	protected IPreferencesService m_preferencesService;
	
	protected void setupWorkingCopy()
	{
		setupScope();
		m_preferences = new WorkingCopyManager().getWorkingCopy(m_original);
	}
	
	protected void setupScope(){}
	
	public EsftpPreferences()
	{
		m_preferencesService = Platform.getPreferencesService();	
		m_searchScope = null;	
	}
		
	public void restoreDefaults()
	{
		try{
			remove();
		}catch(BackingStoreException e){
			System.out.println("clear(), BackingStoreException: WTF is that??");
		}
	}
	
	public void save()
	{
		try{
			System.out.println("SAVING PREFERENCES HERE");
			m_preferences.flush();
		}catch(BackingStoreException e){
			System.out.println("save(), BackingStoreException: WTF is that??");
		}
	}
	
	public void restore()
	{
		m_preferences = m_original;
	}
	
	public void remove() throws BackingStoreException
	{
	    String[] children = m_original.childrenNames();
	    // Remove all keys in this node
	    m_original.clear();
	    // Now remove all children nodes
	    for (int i = 0; i < children.length; i++) {
	    	m_original.node(children[i]).removeNode();
	    }
	    // Persist to backing store
	    m_original.flush();
	    setupWorkingCopy();
	}
	
	public void debug()
	{
		System.out.println("EsftpPreferences::debug()");
		System.out.println(IProperty.VERIFIED + "=" + getBoolean(IProperty.VERIFIED));
		System.out.println(IProperty.SERVER + "=" + getString(IProperty.SERVER));
		System.out.println(IProperty.PORT + "=" + getInt(IProperty.PORT));
		System.out.println(IProperty.PROTOCOL + "=" + getInt(IProperty.PROTOCOL));
		System.out.println(IProperty.TIMEOUT + "= " + getInt(IProperty.TIMEOUT));
		System.out.println(IProperty.USERNAME + "=" + getString(IProperty.USERNAME));
		System.out.println(IProperty.PASSWORD + "=" + getString(IProperty.PASSWORD));
		System.out.println(IProperty.SAVEPWD + "=" + getBoolean(IProperty.SAVEPWD));
		System.out.println(IProperty.RECURSE + "=" + getBoolean(IProperty.RECURSE));
		System.out.println(IProperty.EMPTY + "=" + getBoolean(IProperty.EMPTY));
		System.out.println(IProperty.SITEROOT + "=" + getString(IProperty.SITEROOT));
		System.out.print("\n\n");
	}
	
	public boolean getBoolean(String key)
	{
		return m_preferencesService.getBoolean(Activator.PLUGIN_ID, key, false, m_searchScope);
	}
	
	public void putBoolean(String key, boolean value)
	{
		m_preferences.putBoolean(key, value);
	}
	
	public String getString(String key)
	{
		return m_preferencesService.getString(Activator.PLUGIN_ID, key, "CANNOT_FIND", m_searchScope);
	}
	
	public void putString(String key, String value)
	{
		m_preferences.put(key, value);
	}
	
	public int getInt(String key)
	{
		return m_preferencesService.getInt(Activator.PLUGIN_ID, key, -1, m_searchScope);
	}
	
	public void putInt(String key, int value)
	{
		m_preferences.putInt(key, value);
	}
}
