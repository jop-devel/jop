package scd_micro;

/**
 * The <code>Vector2d</code> class implements a 2-dimensional vector that can represent the
 * position or velocity of an object within a 2D space. This implementation uses public, non-final
 * fields to avoid as much object creation as possible. Java does not have value types per se, but
 * these vector classes are the closest thing that is possible.
 * 
 * @author Ben L. Titzer
 */
final class Vector2d {

	public float x, y;

	/**
	 * The default constructor for the <code>Vector2d</code> returns an object representing the
	 * zero vector.
	 */
	public Vector2d() {}

	/**
	 * The main constructor for the <code>Vector2d</code> class takes the two coordinates as
	 * parameters and produces an object representing that vector.
	 * @param x the coordinate on the x (east-west) axis
	 * @param y the coordinate on the y (north-south) axis
	 */
	public Vector2d(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * The secondary constructor for the <code>Vector2d</code> class takes a vector to copy into
	 * this new instance and returns an instance that represents a copy of that vector.
	 * @param v the vale of the vector to copy into this new instance
	 */
	public Vector2d(Vector2d v) {
		this.x = v.x;
		this.y = v.y;
	}

	/**
	 * The <code>set</code> is basically a convenience method that resets the internal values of
	 * the coordinates.
	 * @param x the coordinate on the x (east-west) axis
	 * @param y the coordinate on the y (north-south) axis
	 */
	public void set(Vector2d val) {
		this.x = val.x;
		this.y = val.y;
	}

	/**
	 * The <code>zero</code> method is a convenience method to zero the coordinates of the vector.
	 */
	public void zero() {
		x = y = 0;
	}

	public boolean equals(Object o) throws ClassCastException {
		try {
			return equals((Vector2d) o);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean equals(Vector2d b) {
		if (x != b.x) return false;
		if (y != b.y) return false;
		return true;
	}

	public int hashCode() {
		return (int) ((x + y) * y + (x - y) * x);
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}