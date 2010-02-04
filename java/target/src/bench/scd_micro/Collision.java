package scd_micro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 * Represents a definite collision that has occured.
 * @author Filip Pizlo
 */
class Collision {
	/** The aircraft that were involved.  */
	private ArrayList aircraft;

	/** The location where the collision happened. */
	private Vector3d location;

	/** Construct a Collision with a given set of aircraft and a location.  */
	public Collision(List aircraft, Vector3d location) {
		this.aircraft = new ArrayList(aircraft);
		Collections.sort(this.aircraft);
		this.location = location;
	}

	/** Construct a Coollision with two aircraft an a location. */
	public Collision(Aircraft one, Aircraft two, Vector3d location) {
		aircraft = new ArrayList();
		aircraft.add(one);
		aircraft.add(two);
		Collections.sort(aircraft);
		this.location = location;
	}

	/** Returns the list of aircraft involved. You are not to modify this list. */
	public ArrayList getAircraftInvolved() { return aircraft; }

	/** Returns the location of the collision. You are not to modify this location. */
	public Vector3d getLocation() { return location; }

	/** Returns a hash code for this object. It is based on the hash codes of the aircraft. */

	public int hashCode() {
		int ret = 0;
		for (Iterator iter = aircraft.iterator(); iter.hasNext();) 
			ret += ((Aircraft) iter.next()).hashCode();	
		return ret;
	}

	/** Determines collision equality. Two collisions are equal if they have the same aircraft.*/

	public boolean equals(Object _other) {
		if (_other == this)  return true;
		if (!(_other instanceof Collision)) return false;
		Collision other = (Collision) _other;
		ArrayList a = getAircraftInvolved();
		ArrayList b = other.getAircraftInvolved();
		if (a.size() != b.size()) return false;
		Iterator ai = a.iterator();
		Iterator bi = b.iterator();
		while (ai.hasNext()) 
			if (!ai.next().equals(bi.next())) return false;		
		return true;
	}

	/** Returns a helpful description of this object. */

	public String toString() {
		StringBuffer buf = new StringBuffer("Collision between ");
		boolean first = true;
		for (Iterator iter = getAircraftInvolved().iterator(); iter.hasNext();) {
			if (first) first = false;
			else buf.append(", ");	    
			buf.append(iter.next().toString());
		}
		buf.append(" at "); buf.append(location.toString());
		return buf.toString();
	}
}
