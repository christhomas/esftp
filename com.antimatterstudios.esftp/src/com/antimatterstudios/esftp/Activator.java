package com.antimatterstudios.esftp;

import java.util.Vector;
import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.osgi.service.prefs.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.antimatterstudios.esftp.directory.FileList;
import com.antimatterstudios.esftp.properties.IProperty;
import com.antimatterstudios.esftp.properties.EsftpPreferences;
import com.antimatterstudios.esftp.ui.ConsoleDisplayMgr;
import com.antimatterstudios.esftp.FilterWriter;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin 
{
	//	The shared instance
	private static Activator m_plugin;
	
	//	FilterWriter debug class
	private FilterWriter m_output;
	
	protected Digest m_hash;
	protected Vector<Transfer> m_transfer;
	protected boolean m_state = false;
	protected String m_version;
	
	public static ConsoleDisplayMgr cons = ConsoleDisplayMgr.getDefault("ESftp Console");
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.antimatterstudios.esftp";
	
	/**
	 * The constructor
	 */
	public Activator() {
		m_plugin = this;
		
//		Create a new FilterWriter object + enable console output
		m_output = new FilterWriter();
		m_output.enableConsole(true);
		//	create a new Digest object
		m_hash = new Digest();
		//	Create a new Transfer Vector
		m_transfer = new Vector<Transfer>();
	}
	
	public FilterWriter getFilterWriter(){
		return m_output;
	}
	
	public static void consolePrint(String msg, int msgKind) {
		cons.print(msg,msgKind);
	}
	
	public static void consolePrintln(String msg, int msgKind) {
		cons.println(msg,msgKind);
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		return Activator.getActiveWorkbenchWindow().getActivePage();
	}
	
	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 */
	protected void initialiseDefaultPreferences() {
		System.out.println("INITIALISING DEFAULTS");
		IEclipsePreferences store = new DefaultScope().getNode("com.antimatterstudios.esftp");
		
		store.put(IProperty.SERVER, "<Enter server address>");
		store.putInt(IProperty.PORT,22);
		store.putInt(IProperty.PROTOCOL,0);
		store.putInt(IProperty.TIMEOUT, 30);
		store.put(IProperty.USERNAME,"<Enter Username>");
		store.put(IProperty.PASSWORD,"");
		store.putBoolean(IProperty.SAVEPWD,true);
		store.putBoolean(IProperty.RECURSE,true);
		store.putBoolean(IProperty.EMPTY,true);
		store.put(IProperty.SITEROOT,"<Enter Site Root>");
		
		EsftpPreferences p = new EsftpPreferences(store);
		p.debug();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		m_plugin = this;
		Dictionary headers = context.getBundle().getHeaders();
		String version = "UNKNOWN";
		try{
			version = (String)headers.get("Bundle-Version");
		}catch(NullPointerException e){}
		
		m_plugin.setVersion(version);
		
		initialiseDefaultPreferences();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		m_plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return m_plugin;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("com.antimatterstudios.esftp", path);
	}
	
	/**
	 * Allows TransferDetail objects to obtain the hash object in order to compute their unique I.D string
	 * @return An object to create a hash with
	 */
	public Digest getHash(){
		return m_hash;
	}
	
	public Transfer getTransfer(){
		return new TransferSSHTools();
	}
	
	public void setVersion(String version)
	{
		m_version = version;
	}
	
	public String getVersion()
	{
		return m_version;
	}
	
	/**
	 * Add a list of files to be transferred, but only if that server is unique
	 * 
	 * This will find the key in the file list, attempt to find a server which matches it, if it finds one
	 * it will append the list of files to that existing transfer, otherwise, it will add another one.
	 * 
	 * @param fl The list of files to transfer
	 */
	public void add(FileList fl){
		//System.out.println("TRACE-> SftpPlugin::add()");
		String key = fl.getKey();
			
		if(key != null){
			//System.out.println("SftpPlugin::add(), find transfer object");
			for(int a=0;a<m_transfer.size();a++){
				Transfer t = (Transfer)m_transfer.get(a);
				if(key == t.getKey()){
					System.out.println("SftpPlugin::add(), found existing transfer object");
					t.appendFilelist(fl);
					return;
				}
			}
			
			//System.out.println("SftpPlugin::add(), couldnt find existing transfer object, create new");
			Transfer t = getTransfer();
			t.init(fl,key);
			m_transfer.add(t);
			t.setRule(fl.getProject());
			t.setUser(true);
			t.schedule();
		}
	}
	
	/**
	 * Removes a Transfer object from the queue of objects currently running
	 * 
	 * Removal of a transfer occurs either when the transfer was cancelled, either locally or remotely
	 * Or the transfer completed, hence needs to remove itself from the list
	 * 
	 * @param t	The transfer to remove from the list of current transfers
	 */
	public void remove(Transfer t){
		//System.out.println("SftpPlugin::remove(), removing transfer object, it's finished");
		for(int a=0;a<m_transfer.size();a++){
			Transfer tmp = (Transfer)m_transfer.get(a);
			if(t.getKey() == tmp.getKey()){
				//System.out.println("SftpPlugin::remove(), found transfer object, removing");
				m_transfer.remove(a);
			}
		}
	}
}
