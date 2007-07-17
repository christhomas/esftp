/*******************************************************************************
 * Copyright (c) {06/11/2005} {Christopher Thomas} 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contributors:
 *    {Christopher Thomas} - initial API and implementation
 *    chris.alex.thomas@gmail.com
 *******************************************************************************/
package com.antimatterstudios.esftp.directory;

import java.util.Vector;
import com.antimatterstudios.esftp.Transfer;
import com.antimatterstudios.esftp.TransferDetails;

public abstract class Directory {
	protected long m_size;	
	protected Transfer m_transfer;
	protected TransferDetails m_details;
	
	/**
	 * Processes all the results from the directory lists
	 *
	 */
	public abstract void process(Object item, Vector files, Vector folders);
	
	/** 
	 * Convert a absolute filesystem name to a relative one
	 * 
	 * @param item	The absolute path to convert
	 * @returns String The new relative path
	 */
	public String makeRelative(String item, String root){
		//System.out.println("TRACE-> Directory::makeRelative("+item+","+root+")");
		//	If it's a directory and has // on the end, reduce it to /
		if(item.endsWith("//")) item = item.substring(0,item.length()-1);
		
		return item.substring(root.length());
	}
	
	/**
	 * Converts a selected absolute path into a relative path
	 * 
	 * @param file	The selected path to convert
	 * @return The new relative path
	 */
	public String makeRelative(String file){
		//System.out.println("TRACE-> Directory::makeRelative("+file+")");
		return makeRelative(file,m_details.getLocalRoot());
	}
	
	/**
	 * Returns the number of bytes the file list has to transfer
	 * 
	 * @return	the number of bytes
	 */
	public abstract long getNumBytes(Vector files);
}
