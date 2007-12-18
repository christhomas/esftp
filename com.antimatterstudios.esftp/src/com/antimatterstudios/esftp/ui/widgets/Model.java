package com.antimatterstudios.esftp.ui.widgets;

public abstract class Model
{
	protected Model m_parent;
	protected String m_name;
	protected String m_fullName;
	protected IDeltaListener m_listener = NullDeltaListener.getSoleInstance();
	
	public Model()
	{
		setParent(null);
		setName("");
		setFullName("");
	}
	
	protected void fireAdd(Object added)
	{
		m_listener.add(new DeltaEvent(added));
	}

	protected void fireRemove(Object removed)
	{
		m_listener.remove(new DeltaEvent(removed));
	}

	public void setFullName(String name)
	{
		m_fullName = name;
	}
	
	public String getFullName()
	{
		String name = "";
		Model parent = getParent();
		if(parent != null) name += parent.getFullName();

		return name+m_fullName;
	}
	
	public void setName(String name)
	{
		m_name = name;
	}
	
	public String getName()
	{
		return m_name;
	}
	
	protected void setParent(Model parent)
	{
		m_parent = parent;
	}
	
	public Model getParent()
	{
		return m_parent;
	}
	
	/* The receiver should visit the toVisit object and
	 * pass along the argument. */
	public abstract void accept(IModelVisitor visitor, Object passAlongArgument);
	
	public void addListener(IDeltaListener listener)
	{
		m_listener = listener;
	}
	
	public void removeListener(IDeltaListener listener)
	{
		if(m_listener.equals(listener)) {
			m_listener = NullDeltaListener.getSoleInstance();
		}
	}
}
