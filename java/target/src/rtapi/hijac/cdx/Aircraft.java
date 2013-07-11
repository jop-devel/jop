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
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package hijac.cdx;

import hijac.cdx.ASCIIConverter;
import hijac.cdx.Aircraft;
import hijac.cdx.Constants;

/**
 * @author Filip Pizlo, Kun Wai, Frank Zeyda
 */
class Aircraft implements Comparable {
  /** The callsign. Currently, the only data we hold. */
  private final byte[] callsign;

  public Aircraft() {
    callsign = new byte[Constants.LENGTH_OF_CALLSIGN];
  }

  /** Construct with a callsign. */
  public Aircraft(final byte[] _callsign) {
    if (_callsign.length != Constants.LENGTH_OF_CALLSIGN) {
      throw new IllegalArgumentException();
    }
    callsign = _callsign;
  }

  /** Construct a copy of an aircraft. */
  public Aircraft(final Aircraft _aircraft) {
    this(_aircraft.getCallsign());
  }

  /** Give you the callsign. */
  public byte[] getCallsign() {
    return callsign;
  }

  /** Return a valid hash code for this object. */
  public int hashCode() {
    int h = 0;
    for (int i = 0; i < callsign.length; i++) {
      h += callsign[i];
    }
    return h;
  }

  /** Performs a comparison between this object and another. */
  public boolean equals(final Object other) {
    if (other == this) { return true; }
    else if (other instanceof Aircraft) {
      final byte[] cs = ((Aircraft) other).callsign;
      if (cs.length != callsign.length) { return false; }
      for (int i = 0; i < cs.length; i++) {
        if (cs[i] != callsign[i]) { return false; }
      }
      return true;
    }
    else { return false; }
  }

  /** Performs comparison with ordering taken into account. */
  public int compareTo(final Object _other) throws ClassCastException {
    final byte[] cs = ((Aircraft) _other).callsign;
    if (cs.length < callsign.length) return -1;
    if (cs.length > callsign.length) return +1;
    for (int i = 0; i < cs.length; i++) {
      if (cs[i] < callsign[i]) return -1;
      else if (cs[i] > callsign[i]) return +1;
    }
    return 0;
  }

  /** Returns a helpful description of this object. */
  public String toString() {
    return ASCIIConverter.bytesToString(callsign);
  }
}
