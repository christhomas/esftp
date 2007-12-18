package com.antimatterstudios.esftp.properties;

import com.antimatterstudios.esftp.Activator;
import com.antimatterstudios.esftp.properties.EsftpPreferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class ProjectPreferences extends EsftpPreferences
{	
	protected IProject m_project;
	
	protected void setupScope()
	{
		m_original = new ProjectScope(m_project).getNode(Activator.PLUGIN_ID);
	}
	
	public ProjectPreferences(IProject project)
	{
		m_searchScope = new IScopeContext[3];
		m_searchScope[0] = new ProjectScope(project);
		m_searchScope[1] = new InstanceScope();
		m_searchScope[2] = new DefaultScope();
	
		m_project = project;
		
		setupWorkingCopy();
	}
}
