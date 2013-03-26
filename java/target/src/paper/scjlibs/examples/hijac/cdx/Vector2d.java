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
package scjlibs.examples.hijac.cdx;

import scjlibs.examples.hijac.cdx.Vector2d;

/**
 * The <code>Vector2d</code> class implements a 2-dimensional vector that
 * can represent the position or velocity of an object within a 2D space.
 * This implementation uses public, non-final fields to avoid as much object
 * creation as possible. Java does not have value types per se, but these
 * vector classes are the closest thing that is possible.
 *
 * @author Ben L. Titzer
 */
final class Vector2d {
  /**
   * Fields for the x and y coordinates of the vector.
   */
  public float x, y;

  /**
   * The default constructor for the <code>Vector2d</code> class returns an
   * object representing the zero vector.
   */
  public Vector2d() {
    this(0, 0);
  }

  /**
   * The main constructor for the <code>Vector2d</code> class takes the two
   * coordinates as parameters and produces an object representing that vector.
   *
   * @param x the coordinate on the x (east-west) axis
   * @param y the coordinate on the y (north-south) axis
   */
  public Vector2d(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * The secondary constructor for the <code>Vector2d</code> class takes a
   * vector to copy into this new instance and returns an instance that
   * represents a copy of that vector.
   *
   * @param v the vector to copy into this new instance
   */
  public Vector2d(Vector2d v) {
    this.x = v.x;
    this.y = v.y;
  }

  /**
   * The <code>set</code> method is basically a convenience method that sets
   * the internal values of the coordinates.
   *
   * @param x the coordinate on the x (east-west) axis
   * @param y the coordinate on the y (north-south) axis
   */
  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * The <code>set</code> method is basically a convenience method that sets
   * the internal values of the coordinates copying them from another vector.
   *
   * @param x the coordinate on the x (east-west) axis
   * @param y the coordinate on the y (north-south) axis
   */
  public void set(Vector2d v) {
    this.x = v.x;
    this.y = v.y;
  }

  /**
   * The <code>zero</code> method is a convenience method to zero the
   * coordinates of the vector.
   */
  public void zero() {
    x = y = 0;
  }

  public boolean equals(Object o) throws ClassCastException {
    try {
      return equals((Vector2d) o);
    }
    catch (ClassCastException e) {
      return false;
    }
  }

  public boolean equals(Vector2d b) {
    if (x != b.x) { return false; }
    if (y != b.y) { return false; }
    return true;
  }

  public int hashCode() {
    long rawBytes =
      ((long) Float.floatToIntBits(y) << 32) | Float.floatToIntBits(x);
    int hash = 0xAAAAAAAA;
    for (int i = 0; i < 8; i++, rawBytes >>= 8) {
      byte curByte = (byte) (rawBytes & 0xFF);
      hash ^= ((i & 1) == 0) ?
        ((hash << 7) ^ curByte * (hash >>> 3)) :
        (~((hash << 11) + curByte ^ (hash >>> 5)));
    }
    return hash;
  }

  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
