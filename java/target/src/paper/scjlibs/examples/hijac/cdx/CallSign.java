/**
 *  This file is part of miniCDx benchmark of oSCJ.
 *
 *   miniCDx is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   miniCDx is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with miniCDx.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2009, 2010
 *   @authors  Daniel Tang, Ales Plsek
 *   Revised by Kun Wei
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package scjlibs.examples.hijac.cdx;

import scjlibs.examples.hijac.cdx.CallSign;
import scjlibs.examples.hijac.cdx.Constants;

/**
 * The class CallSign encodes call signs. They are used to identify aircrafts.
 */
public class CallSign {

	final private byte[] sign;

	public CallSign(final byte[] bytes) {
		sign = bytes;
	}

	public CallSign() {
		sign = new byte[Constants.LENGTH_OF_CALLSIGN];
	}

	public byte[] get() {
		return sign;
	}

	/**
	 * Returns a valid hash code for this object.
	 */
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < sign.length; i++) {
			h += sign[i];
		}
		return h;
	}

	/**
	 * Performs an equality test between this object and another.
	 */
	public boolean equals(final Object other) {
		if (other == this) {
			return true;
		} else if (other instanceof CallSign) {
			
			final byte[] other_sign = ((CallSign) other).sign;
			
			if (other_sign.length != sign.length) {
				return false;
			}
			
			for (int i = 0; i < other_sign.length; i++) {
				if (other_sign[i] != sign[i]) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

}
