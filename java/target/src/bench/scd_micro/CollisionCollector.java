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
	private HashSet collisions = new HashSet();

	/** Add some collisions.  */
	public void addCollisions(List collisions) { this.collisions.addAll(collisions); }

	/** Get the list of collisions.   */
	public ArrayList getCollisions() { return new ArrayList(collisions);  }
}
