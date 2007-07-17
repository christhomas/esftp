package com.antimatterstudios.esftp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

class MultiplexWriter extends StringWriter{
	Vector<String> m_allow, m_deny;
	
	boolean m_console, m_file, m_econ;
	
	public MultiplexWriter(){
		m_allow = new Vector<String>();
		m_deny = new Vector<String>();
		
		m_console = false;
		m_file = false;
		m_econ = false;
	}
	
	public boolean checkEnabled(){ 
		return (m_console != false || m_file != false || m_econ != false) ? true : false; 
	}	
	public void enableConsole(boolean status){ m_console = status; }
	//	These two are disabled for now
	public void enableFile(){ m_file = false; }
	public void enableEclipseConsole(){ m_econ = false; }
	
	public void addAllowFilter(String filter){
		m_allow.addElement(filter);
	}
	
	public void addDenyFilter(String filter){
		m_deny.addElement(filter);
	}	
	
	public void flush(){
		super.flush();
		
		StringBuffer sb = getBuffer();
		String str = sb.toString();
		sb.setLength(0);
		
		int a, matches = 0;
		
		for(a=0;a<m_allow.size();a++){
			String s = (String)m_allow.get(a);
			if(str.indexOf(s) >= 0) matches++;
		}
		
		for(a=0;a<m_deny.size();a++){
			String s = (String)m_deny.get(a);
			if(str.indexOf(s) >= 0) return;
		}
		
		if(matches > 0 || m_allow.size() == 0){
			if(m_console == true) System.out.print(str);
			//	Nothing for these two yet
			if(m_file == true){}
			if(m_econ == true){}
		}
	}
}

public class FilterWriter extends PrintWriter {
	protected MultiplexWriter m_writer;
	
	public FilterWriter(){
		//	Just use a dummy object here, keep the stupid java compiler quiet
		super(new MultiplexWriter(), true);
		
		m_writer = (MultiplexWriter)out;
	}
	
	public void enableConsole(boolean status){
		m_writer.enableConsole(status);
	}
	
	public void enableFile(String filename, boolean status){
		//	Create file object here
		if(status){
			//	Open file
		}else{
			//	Close file
		}
	}
	
	public void enableEclipseConsole(boolean status){
		//	Create the eclipse console object here
		if(status){
			//	Enable console
		}else{
			//	Disable console
		}
	}
	
	public void addAllowFilter(String filter){
		m_writer.addAllowFilter(filter);
	}
	
	public void addDenyFilter(String filter){
		m_writer.addDenyFilter(filter);
	}
	
	public String toString()
	{
		return m_writer.getBuffer().toString();
	}	
}
