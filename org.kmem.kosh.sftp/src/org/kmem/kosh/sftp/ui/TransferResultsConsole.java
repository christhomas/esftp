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
 *    {Danny Valliant} - Initial Hotkey support and Console output
 *    xenden@users.sourceforge.net
 *******************************************************************************/

package org.kmem.kosh.sftp.ui;

import org.kmem.kosh.sftp.SftpPlugin;
import org.kmem.kosh.sftp.Transfer;
import org.kmem.kosh.sftp.directory.FileList;
import java.util.Date;

public class TransferResultsConsole extends TransferResults {

	/**
	 * This method initializes the shell
	 */
	public TransferResultsConsole(Transfer t, FileList fl){
		super(t,fl);

 		for(int a=0;a<m_fileList.getNumItems();a++){
			SftpPlugin.consolePrint(m_fileList.getStatusString(a),1);			
		}
		SftpPlugin.getDefault().remove(m_transfer);	
		
		Date now = new Date();
		SftpPlugin.consolePrintln(now.toString()+ "\t" + m_transfer.getTransferSize(),1);
		SftpPlugin.consolePrintln("\t" + m_transfer.getTransferSize(),1);
		SftpPlugin.consolePrintln("",1);		
	}
}
