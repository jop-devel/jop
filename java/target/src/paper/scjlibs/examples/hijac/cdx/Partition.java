/**
 * @author Frank Zeyda
 */
package scjlibs.examples.hijac.cdx;

import scjlibs.examples.hijac.cdx.Constants;
import scjlibs.util.ArrayList;
//import hijac.cdx.javacp.utils.LinkedList;
import scjlibs.util.List;

public class Partition {
	
	private final List[] parts;
	private int counter;

	public Partition(int n) {

		parts = new ArrayList[n];

		for (int i = 0; i < n; i++) {
			parts[i] = new ArrayList(
					Constants.voxels(Constants.GOOD_VOXEL_SIZE) / n + 1);
		}

		counter = 0;
	}

	public synchronized void clear() {
		for (int i = 0; i < parts.length; i++) {
			parts[i].clear();
		}
		counter = 0;
	}

	public synchronized void recordMotionList(List motions) {
		parts[counter].add(motions);
		counter = (counter + 1) % parts.length;
	}

	public synchronized List getDetectorWork(int id) {
		return parts[id - 1];
	}
}
