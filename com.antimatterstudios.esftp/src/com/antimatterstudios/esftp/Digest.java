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

package com.antimatterstudios.esftp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
	protected MessageDigest m_digest = null;
	
	public Digest(){
		if(initDigest("SHA-1") == true) return;
		
		if(initDigest("MD5") == true) return; 
				
		initDigest("reply");
	}
	
	public boolean initDigest(String algo){
		//	if reply "algo" (Laughs!!) is chosen, success is guarenteed
		if(algo != "reply"){
			try{
				//	attempt to create a message digest instance
				m_digest = MessageDigest.getInstance(algo);
			}catch(NoSuchAlgorithmException ex){
				//	problems, doesnt exist? return false
				System.err.println(ex);
				return false;
			}
		}
		
		return true;
	}
	
	public String getDigest(String input){
		if(m_digest != null){
			m_digest.digest(input.getBytes());
			return m_digest.toString();
		}else{
			//	m_digest is null, that means MD5 and SHA1 are unavailable for hashing
			//	last resort, hope that the string on it's own has no collisions, reply it directly
			return input;
		}
	}
}
