package scd_micro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Collects collisions in lists and then returns a list of collisions where
 * no two are equal.
 * @author Filip Pizlo
 */
class CollisionCollector {
	/** A hash set of collisions.  */
	private HashSet<Collision> collisions =
		new HashSet<Collision>(RawFrame.MAX_COLLISIONS_POW2);

	/** Add some collisions.  */
	public void addCollisions(List<Collision> collisions) {
		// Modified for JOP               MAX_COLLIONS=45
		for(Collision c : collisions) { //@WCA loop<=45
			this.collisions.add(c);
		}
	}

	/** Get the list of collisions.   */
	public ArrayList<Collision> getCollisions() { 
		// Modified for JOP
		ArrayList<Collision> copy = new ArrayList<Collision>(collisions.size());
		for(Collision c : collisions) { //@WCA loop<=45
			copy.add(c);
		}
		return copy;
	}
}
