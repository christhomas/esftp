package com.antimatterstudios.esftp.properties;

import com.antimatterstudios.esftp.properties.EsftpPreferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class ProjectPreferences extends EsftpPreferences
{	
	protected IProject m_project;
	
	public ProjectPreferences(IProject project)
	{
		super(new ProjectScope(project).getNode("com.antimatterstudios.esftp"));
	
		m_project = project;
	}
	
	public void restoreDefaults(){
		clear();
		m_preferences = new ProjectScope(m_project).getNode(EsftpPreferences.m_projectID);
		clone(new EsftpPreferences(new InstanceScope().getNode("com.antimatterstudios.esftp")));
	}
}
