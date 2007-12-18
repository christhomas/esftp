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

package com.antimatterstudios.esftp.properties;

public interface IProperty {
	public static final String VERIFIED = "verified";
	public static final String SERVER = "server";
	public static final String PORT = "port";
	public static final String PROTOCOL = "protocol";
	public static final String TIMEOUT = "timeout";
	public static final String USERNAME = "Username";
	public static final String PASSWORD = "Password";
	public static final String SAVEPWD = "Save Password";
	public static final String RECURSE = "Recurse Subdirectories";
	public static final String EMPTY = "Create Empty Directories";
	public static final String SITEROOT = "Site Root";
	
	//	Protocol sub options
	public static final int PROTOCOL_SFTP = 0;
	public static final int PROTOCOL_FTP = 1;
	public static final int PROTOCOL_FTPTLS = 2;
	public static final int PROTOCOL_FTPSSL = 3;
}
