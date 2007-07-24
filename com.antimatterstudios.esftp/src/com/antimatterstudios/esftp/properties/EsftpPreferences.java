package com.antimatterstudios.esftp.properties;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.eclipse.ui.preferences.WorkingCopyManager;

public class EsftpPreferences
{
	protected IEclipsePreferences m_preferences;
	
	protected IEclipsePreferences m_original;
	
	public static final String m_projectID = "com.antimatterstudios.esftp";
	
	public EsftpPreferences(IEclipsePreferences preferences)
	{
		m_preferences = new WorkingCopyManager().getWorkingCopy(preferences);
		m_original = preferences;
	}
		
	public void clear()
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
	    String[] children = m_preferences.childrenNames();
	    // Remove all keys in this node
	    m_preferences.clear();
	    // Now remove all children nodes
	    for (int i = 0; i < children.length; i++) {
	    	m_preferences.node(children[i]).removeNode();
	    }
	    // Persist to backing store
	    m_preferences.flush();
	}
	
	public void debug()
	{
		System.out.println("EsftpPreferences::debug()");
		System.out.println(IProperty.VERIFIED + "=" + getBoolean(IProperty.VERIFIED));
		System.out.println(IProperty.SERVER + "=" + getString(IProperty.SERVER));
		System.out.println(IProperty.PORT + "=" + getInt(IProperty.PORT));
		System.out.println(IProperty.TIMEOUT + "= " + getInt(IProperty.TIMEOUT));
		System.out.println(IProperty.USERNAME + "=" + getString(IProperty.USERNAME));
		System.out.println(IProperty.PASSWORD + "=" + getString(IProperty.PASSWORD));
		System.out.println(IProperty.SAVEPWD + "=" + getBoolean(IProperty.SAVEPWD));
		System.out.println(IProperty.RECURSE + "=" + getBoolean(IProperty.RECURSE));
		System.out.println(IProperty.EMPTY + "=" + getBoolean(IProperty.EMPTY));
		System.out.println(IProperty.SITEROOT + "=" + getString(IProperty.SITEROOT));
	}
	
	/**	Backup a property store into a clone object
	 * 
	 * @param store	The store to backup
	 */
	public void clone(EsftpPreferences store)
	{
		putBoolean(IProperty.VERIFIED, store.getBoolean(IProperty.VERIFIED));
		putString(IProperty.SERVER, store.getString(IProperty.SERVER));
		putInt(IProperty.PORT, store.getInt(IProperty.PORT));
		putInt(IProperty.TIMEOUT, store.getInt(IProperty.TIMEOUT));
		putString(IProperty.USERNAME, store.getString(IProperty.USERNAME));
		putString(IProperty.PASSWORD, store.getString(IProperty.PASSWORD));
		putBoolean(IProperty.SAVEPWD, store.getBoolean(IProperty.SAVEPWD));
		putBoolean(IProperty.RECURSE, store.getBoolean(IProperty.RECURSE));
		putBoolean(IProperty.EMPTY, store.getBoolean(IProperty.EMPTY));
		putString(IProperty.SITEROOT, store.getString(IProperty.SITEROOT));
	}
	
	public boolean getBoolean(String key)
	{
		return m_preferences.getBoolean(key,false);
	}
	
	public void putBoolean(String key, boolean value)
	{
		m_preferences.putBoolean(key, value);
	}
	
	public String getString(String key)
	{
		String s = m_preferences.get(key,"monkey");
		
		return s;
	}
	
	public void putString(String key, String value)
	{
		m_preferences.put(key, value);
	}
	
	public int getInt(String key)
	{
		return m_preferences.getInt(key,1000);
	}
	
	public void putInt(String key, int value)
	{
		m_preferences.putInt(key, value);
	}
}
