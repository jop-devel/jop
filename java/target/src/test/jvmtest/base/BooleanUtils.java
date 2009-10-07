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
/**
 * BooleanUtils provide some utilities for the type boolean/Boolean 
 * @author Günther Wimpassinger
 */
package jvmtest.base;

/**
 * Some utilities for the type boolean
 * @author Günther Wimpassinger
 */
public class BooleanUtils {
	
	/** convert to primitive boolean to
	 * a string 
	 * @param b The boolean primitive
	 * @return "true" or "false" depending on the value of b
	 */
	public static String toString(boolean b) {
		return b ? "true" : "false";
	}
	
	/** convert to primitive boolean to
	 * a adjusted string of the same length for both
	 * result values
	 * @param b The boolean primitive
	 * @return "true " or "false" depending on the value of b
	 */
	public static String toAdjustString(boolean b) {
		return b ? "true " : "false";
	}	

}
