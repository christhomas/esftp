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

import java.util.Comparator;
import org.eclipse.core.resources.*;

public class ResourceCompare implements Comparator {
	public ResourceCompare(){}
	
	public int compare(Object obj1, Object obj2) {
		int result = -1;
		
		if(obj1 instanceof IContainer && obj2 instanceof IContainer){
			IContainer c1 = (IContainer)obj1;
			IContainer c2 = (IContainer)obj2;
			
			String s1 = c1.getLocation().toPortableString();
			String s2 = c2.getLocation().toPortableString();
			
			if(s1.length() > s2.length()) result = 1;
		}
		
		return result;	
	}
}
