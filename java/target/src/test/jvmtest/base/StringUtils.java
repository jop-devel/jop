/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jvmtest.base;

/**
 * Some utilities for Strings
 * @author Günther Wimpassinger
 *
 */
public final class StringUtils {
	
	/**
	 * Pad the string <code>s</code> on the left side with <code>pad</code>
	 * until <code>len</code> characters. If <code>s</code> is
	 * longer the left part is truncated.
	 * @param s String to adjust
	 * @param len Number of characters after the adjustment
	 * @param pad Pad/fill character
	 * @return The right adjusted string padded with the <code>pad</code>
	 * character until it is <code>len</code> characters long
	 */
	public static String RightAdjust(String s, int len, char pad) {
		if (len==0)
			return null;

		char[] ca = new char[len];
		int k;
		
		if (s==null) {
			k = len;
		} else {
			k = len - s.length();
		}

		for (int i=0;i<k;i++) {
			ca[i]=pad;
		}
		for (int i=(k>0?k:0);i<len;i++) {
			ca[i]=s.charAt(i-k);
		}
			
		return new String(ca,0,ca.length);		
	}
	
	/**
	 * Pad the string <code>s</code> on the right side with <code>pad</code>
	 * until <code>len</code> characters. If <code>s</code> is
	 * longer the right part is truncated.
	 * @param s String to adjust
	 * @param len Number of characters after the adjustment
	 * @param pad Pad/fill character
	 * @return The adjusted string padded with the <code>pad</code>
	 * character until it is <code>len</code> characters long
	 */
	public static String LeftAdjust(String s, int len, char pad) {
		char[] ca = new char[len];
		int k;
		if (s==null) {
			k = 0;
		} else {
			k = s.length();
		}
		
		for (int i=0;i<len && i<k;i++) {
			ca[i]=s.charAt(i);
		}		
		for (int i=k;i<len;i++) {
			ca[i]=pad;
		}
			
		return new String(ca,0,ca.length);		
	}
	

}
