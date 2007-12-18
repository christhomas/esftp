package com.antimatterstudios.esftp.ui.widgets;

public class File extends Model
{
	public File(){}
	
	public File(String name)
	{
		this();
		m_name = name;
	}
	
	/*
	 * @see Model#accept(ModelVisitorI, Object)
	 */
	public void accept(IModelVisitor visitor, Object passAlongArgument)
	{
		visitor.visitFolder(this, passAlongArgument);
	}
}
