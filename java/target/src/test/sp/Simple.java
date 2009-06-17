/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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
 * 
 */
package sp;

import com.jopdesign.sys.Native;

/**
 * A single path programming example on JOP.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Simple {

	public static void main(String[] args) {

		String a = "true";
		String b = "fals";
		String result;
		
		int val;
		
		boolean cond = true;
		
		val = Native.condMove(1, 2, cond);
		System.out.println(val);
		result = (String) Native.condMoveRef(a, b, cond);
		System.out.println(result);

		cond = false;
		
		val = Native.condMove(1, 2, cond);
		System.out.println(val);
		result = (String) Native.condMoveRef(a, b, cond);
		System.out.println(result);
		
	}

}
