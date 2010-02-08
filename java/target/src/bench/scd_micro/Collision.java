package scd_micro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Represents a definite collision that has occured.
 * @author Filip Pizlo
 */
class Collision {
	/** The aircraft that were involved.  */
	private ArrayList<Aircraft> aircraft;

	/** The location where the collision happened. */
	private Vector3d location;

	/** Construct a Collision with a given set of aircraft and a location.  */
	public Collision(List<Aircraft> aircraft, Vector3d location) {
		if(aircraft.size() > RawFrame.MAX_PLANES) {
			throw new Error("Collision(): airplane count exceeds MAX_PLANES");
		}
		this.aircraft = new ArrayList<Aircraft>(aircraft);
		MergeSort.sort(this.aircraft);
		this.location = location;
	}

	/** Construct a Coollision with two aircraft an a location. */
	public Collision(Aircraft one, Aircraft two, Vector3d location) {
		aircraft = new ArrayList<Aircraft>(2);
		aircraft.add(one);
		aircraft.add(two);
		MergeSort.sort(aircraft);
		this.location = location;
	}

	/** Returns the list of aircraft involved. You are not to modify this list. */
	public ArrayList<Aircraft> getAircraftInvolved() { return aircraft; }

	/** Returns the location of the collision. You are not to modify this location. */
	public Vector3d getLocation() { return location; }

	/** Returns a hash code for this object. It is based on the hash codes of the aircraft. */

	public int hashCode() {
		int ret = 0;
		for (Iterator<Aircraft> iter = aircraft.iterator(); iter.hasNext();) //@WCA loop<=10 
			ret += ((Aircraft) iter.next()).hashCode();	
		return ret;
	}

	/** Determines collision equality. Two collisions are equal if they have the same aircraft.*/

	public boolean equals(Object _other) {
		if (_other == this)  return true;
		if (!(_other instanceof Collision)) return false;
		Collision other = (Collision) _other;
		ArrayList<Aircraft> a = getAircraftInvolved();
		ArrayList<Aircraft> b = other.getAircraftInvolved();
		if (a.size() != b.size()) return false;
		Iterator<Aircraft> ai = a.iterator();
		Iterator<Aircraft> bi = b.iterator();
		while (ai.hasNext()) //@WCA loop<=10
			if (!ai.next().equals(bi.next())) return false;		
		return true;
	}

	/** Returns a helpful description of this object. */

	public String toString() {
		StringBuffer buf = new StringBuffer("Collision between ");
		boolean first = true;
		for (Iterator<Aircraft> iter = getAircraftInvolved().iterator(); iter.hasNext();) { //@WCA loop<=10
			if (first) first = false;
			else buf.append(", ");	    
			buf.append(iter.next().toString());
		}
		buf.append(" at "); buf.append(location.toString());
		return buf.toString();
	}
}
