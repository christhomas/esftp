package com.antimatterstudios.esftp.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.StructuredSelection;

import com.antimatterstudios.esftp.ui.widgets.File;

public class Directory extends Model
{
	protected ArrayList m_folders;
	protected ArrayList m_files;
	
	private static IModelVisitor adder = new Adder();
	private static IModelVisitor remover = new Remover();
	
	private static class Adder implements IModelVisitor
	{
		public void visitFolder(Model folder, Object argument)
		{
			((Directory) argument).addFolder(folder);
		}
		
		public void visitFile(Model file, Object argument)
		{
			((Directory) argument).addFile(file);
		}
	}

	private static class Remover implements IModelVisitor
	{
		public void visitFolder(Model folder, Object argument)
		{
			((Directory) argument).removeFolder(folder);
			folder.addListener(NullDeltaListener.getSoleInstance());
		}
		
		public void visitFile(Model file, Object argument)
		{
			((Directory) argument).removeFile(file);
		}
	}
	
	public Directory()
	{
		m_folders = new ArrayList();
		m_files = new ArrayList();
	}
	
	public Directory(String name)
	{
		this();
		setName(name);
		setFullName(name);
	}
	
	public List getFolders()
	{
		return m_folders;
	}
	
	public List getFiles()
	{
		return m_files;
	}
	
	protected void reformatName(Model f)
	{
		try{	
			String parent = getFullName();
			String name = f.getName();
			if(name.startsWith(parent)) name = name.substring(parent.length());
			
			f.setName(name);
			f.setFullName(name);
			System.out.println("Adding: "+name);
		}catch(NullPointerException e){
			System.out.println("NPE resetting items name");
		}
	}
	
	protected void addFolder(Model f)
	{
		System.out.print("AddFolder: ");
		reformatName(f);
		m_folders.add((Directory)f);
		f.setParent(this);
		fireAdd(f);
	}
	
	protected void addFile(Model f)
	{
		System.out.print("AddFile: ");
		reformatName(f);
		m_files.add((File)f);
		f.setParent(this);
		fireAdd(f);
	}
	
	public void remove(Model toRemove)
	{
		toRemove.accept(remover, this);
	}
	
	protected void removeFolder(Model f)
	{
		m_folders.remove(f);
		f.addListener(NullDeltaListener.getSoleInstance());
		fireRemove(f);
	}
	
	protected void removeFile(Model f)
	{
		m_files.remove(f);
		f.addListener(NullDeltaListener.getSoleInstance());
		fireRemove(f);
	}

	public void add(Model toAdd)
	{
		toAdd.accept(adder, this);
	}
	
	/** Answer the total number of items the
	 * receiver contains. */
	public int size()
	{
		return m_folders.size();
	}
	
	public boolean expand(String node, TreeViewer tv)
	{
		System.out.println("Directory::expand(), node = "+node+", getName = "+getName());
		
		if(node.startsWith(getName())){
			String child = node.substring(getName().length());
			System.out.println("Searching for child: "+child);
			
			for(int a=0;a<m_folders.size();a++){
				Directory d = (Directory)m_folders.get(a);
				String dirName = d.getName();
				if(dirName.endsWith("/")) dirName = dirName.substring(0,dirName.length()-1);
				
				System.out.println("Checking search: "+child+", against child: "+dirName);
				if(child.startsWith(dirName)){
					System.out.println("Found a match, opening node");
					tv.setExpandedState(d, true);

					ArrayList selection = new ArrayList();
					selection.add(d);
					tv.setSelection(new StructuredSelection(selection));

					if(child == dirName){						
						return true;
					}
				}else System.out.println("Didnt match, search next node");
								
				if(d.expand(child, tv) == true) return true;
			}
		}
		
		return false;
	}
	
	/*
	 * @see Model#accept(ModelVisitorI, Object)
	 */
	public void accept(IModelVisitor visitor, Object passAlongArgument) {
		visitor.visitFolder(this, passAlongArgument);
	}
}
