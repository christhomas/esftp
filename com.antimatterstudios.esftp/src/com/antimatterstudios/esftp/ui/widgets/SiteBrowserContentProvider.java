package com.antimatterstudios.esftp.ui.widgets;

import java.util.Iterator;
import java.util.Vector;
	
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.antimatterstudios.esftp.Transfer;
import com.antimatterstudios.esftp.ui.widgets.SiteBrowser;

public class SiteBrowserContentProvider  implements ITreeContentProvider, IDeltaListener
{
	protected static Object[] EMPTY_ARRAY = new Object[0];
	
	protected TreeViewer m_viewer;
	
	protected SiteBrowser m_browser;
	
	protected Transfer m_transfer;
	
	protected Directory m_waiting;
	
	public SiteBrowserContentProvider(SiteBrowser browser)
	{
		m_browser = browser;
		m_waiting = new Directory("<downloading folder list>");
	}
	
	/*
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {}
	
	/*
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	/**
	* Notifies this content provider that the given viewer's input
	* has been switched to a different element.
	* <p>
	* A typical use for this method is registering the content provider as a listener
	* to changes on the new input (using model-specific means), and deregistering the viewer 
	* from the old input. In response to these change notifications, the content provider
	* propagates the changes to the viewer.
	* </p>
	*
	* @param viewer the viewer
	* @param oldInput the old input element, or <code>null</code> if the viewer
	*   did not previously have an input
	* @param newInput the new input element, or <code>null</code> if the viewer
	*   does not have an input
	*/
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		System.out.println("****************CONTENTPROVIDER: inputChanged");
		m_viewer = (TreeViewer)viewer;
		m_transfer = m_browser.getTransfer();		
		
		if(oldInput != null) removeListenerFrom((Directory)oldInput);
		if(newInput != null) addListenerTo((Directory)newInput);
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively remove this listener
	 * from each child box of the given box. */
	protected void removeListenerFrom(Directory folder)
	{
		System.out.println("****************CONTENTPROVIDER: removeListenerFrom");
		folder.removeListener(this);
		for (Iterator iterator = folder.getFolders().iterator(); iterator.hasNext();) {
			Directory f = (Directory)iterator.next();
			removeListenerFrom(f);
		}
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively add this listener
	 * to each child box of the given box. */
	protected void addListenerTo(Directory folder)
	{
		System.out.println("****************CONTENTPROVIDER: addListenerTo");
		//folder.addFolder(m_waiting);
		for (Iterator iterator = folder.getFolders().iterator(); iterator.hasNext();) {
			Directory f = (Directory)iterator.next();
			addListenerTo(f);
		}
		folder.addListener(this);
	}
	
	protected Object[] getRemoteChildren(Directory directory, boolean add)
	{
		Object[] children = EMPTY_ARRAY;
		
		if(m_transfer != null && directory.getName().length() > 0){
			Vector files	= new Vector();
			Vector folders	= new Vector();
			
			System.out.println("****************CONTENTPROVIDER: getRemoteChildren(), name = "+directory.getFullName());
			m_transfer.list(directory.getFullName(), files, folders);
		
			if(add){
				//	Adding files like this is actually very wrong
				//	Why???? I have forgotten the reason
				for(int a=0;a<files.size();a++) directory.addFile(new File((String)files.elementAt(a)));
				for(int a=0;a<folders.size();a++) directory.addFolder(new Directory((String)folders.elementAt(a)));
				
				m_browser.updateProgress(directory.getFullName(),files.size(),0);
				
				children = concat(directory.getFolders().toArray(), directory.getFiles().toArray());
			}else{
				m_browser.updateProgress(directory.getFullName(),0,1);
				children = concat(files.toArray(), folders.toArray());
			}
		}
		
		return children;
	}

	/*
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement)
	{
		System.out.println("****************CONTENTPROVIDER: getChildren");
		if(parentElement instanceof Directory) {
			
			Directory parent = (Directory)parentElement;
			
			System.out.println("Querying fullName['"+parent.getFullName()+"'], name['"+parent.getName()+"'] for children");
			
			m_browser.updateProgress("QUERYING...",-1,-1);
			return getRemoteChildren(parent, true);
		}
		System.out.println("ContentProvider: getChildren(), returning EMPTY ARRAY");
		return EMPTY_ARRAY;
	}
	
	/*
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element)
	{
		System.out.println("****************CONTENTPROVIDER: getParent");
		if(element instanceof Model) {
			return ((Model)element).getParent();
		}
		return null;
	}
	
	protected Object[] concat(Object[] array1, Object[] array2)
	{
		Object[] both = new Object[array1.length + array2.length];
		System.arraycopy(array1, 0, both, 0, array1.length);
		System.arraycopy(array2, 0, both, array1.length, array2.length);
	
		return both;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element)
	{
		System.out.println("****************CONTENTPROVIDER: hasChildren");
		if(element instanceof Directory){
			return getRemoteChildren((Directory)element, false).length > 0;			
		}
		
		return false;
	}

	/*
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		System.out.println("****************CONTENTPROVIDER: getElements");
		return getChildren(inputElement);
	}
	
	/*
	 * @see IDeltaListener#add(DeltaEvent)
	 */
	public void add(DeltaEvent event)
	{
		System.out.println("****************CONTENTPROVIDER: add");
	}

	/*
	 * @see IDeltaListener#remove(DeltaEvent)
	 */
	public void remove(DeltaEvent event)
	{
		System.out.println("****************CONTENTPROVIDER: remove");
		add(event);
	}	
}
