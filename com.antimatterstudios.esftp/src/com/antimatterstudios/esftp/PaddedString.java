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

public class PaddedString {
	public static String rpad(String original, int length){
		StringBuffer buffer = new StringBuffer(original);
		while (buffer.length() < length) {
			buffer.append(" ");
		}
		return buffer.toString();
	}
	
	public static String lpad(String original, int length){
		StringBuffer buffer = new StringBuffer(original);
		while (buffer.length() < length) {
			buffer.insert(0," ");
		}
		return buffer.toString();
	}
	
	public static String cpad(String original, int length){
		//	Doesnt really work if you use with normal fonts with variables widths
		int pad = length - original.length();
		if(pad > 0){
			int r = pad/2;
			int l = pad-r;
			original = rpad(original,original.length()+r);
			original = lpad(original,original.length()+l);
		}
		return original;
	}
}
