package scd_micro;

/**
 * The <code>Vector3d</code> class implements a 3-dimensional vector that can represent the
 * position or velocity of an object within a 3D space. This implementation uses public, non-final
 * fields to avoid as much object creation as possible. Java does not have value types per se, but
 * these vector classes are the closest thing that is possible.
 * 
 * @author Ben L. Titzer
 */
public final class Vector3d {
	public float x, y, z;

	/**
	 * The default constructor for the <code>Vector3d</code> returns an object representing the
	 * zero vector.
	 */
	public Vector3d() {}

	/**
	 * The main constructor for the <code>Vector3d</code> class takes the three coordinates as
	 * parameters and produces an object representing that vector.
	 * @param x the coordinate on the x (east-west) axis
	 * @param y the coordinate on the y (north-south) axis
	 * @param z the coordinate on the z (elevation) axis
	 */
	public Vector3d(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * The secondary constructor for the <code>Vector3d</code> class takes a vector to copy into
	 * this new instance and returns an instance that represents a copy of that vector.
	 * @param v the vale of the vector to copy into this new instance
	 */
	public Vector3d(Vector3d v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	/**
	 * The <code>set</code> is basically a convenience method that resets the internal values of
	 * the coordinates.
	 * @param x the coordinate on the x (east-west) axis
	 * @param y the coordinate on the y (north-south) axis
	 * @param z the coordinate on the z (elevation) axis
	 */
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * The <code>set</code> is basically a convenience method that resets the internal values of
	 * the coordinates.
	 * @param val the value of the vector
	 */
	public void set(Vector3d val) {
		this.x = val.x;
		this.y = val.y;
		this.z = val.z;
	}

	/**
	 * The <code>zero</code> method is a convenience method to zero the coordinates of the vector.
	 */
	public void zero() {
		x = y = z = 0;
	}

	public boolean equals(Object o) {
		try {
			return equals((Vector3d) o);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public boolean equals(Vector3d b) {
		if (x != b.x) return false;
		if (y != b.y) return false;
		if (z != b.z) return false;
		return true;
	}

	public int hashCode() {
		return (int) ((x + y + z) * y + (x - y + z) * x - (x - y - z) * z);
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
}