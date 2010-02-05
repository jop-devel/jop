package scd_micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;


/**
 * Reduces the set of  possible collisions by using a voxel drawing algorithm.
 * 
 * @author Filip Pizlo, Jeff Hagelberg
 */
class Reducer {

	/** Creates a Vector2d that represents a voxel. */
	protected void voxelHash(Vector3d position, Vector2d voxel) {
		int x_div = (int) (position.x / voxel_size);
		voxel.x = voxel_size * (x_div);
		if (position.x < 0.0f) voxel.x -= voxel_size;

		int y_div = (int) (position.y / voxel_size);
		voxel.y = voxel_size * (y_div);
		if (position.y < 0.0f) voxel.y -= voxel_size;
	}

	/** * Puts a Motion object into the voxel map at a voxel. */
	protected void putIntoMap(HashMap<Vector2d, ArrayList<Motion>> voxel_map, Vector2d voxel, Motion motion) {
		if (!voxel_map.containsKey(voxel)) {
			voxel_map.put(new Vector2d(voxel), new ArrayList<Motion>());
		}
		voxel_map.get(voxel).add(motion);
	}

	/**
	 * Given a voxel and a Motion, determines if they overlap.
	 */
	protected boolean isInVoxel(Vector2d voxel, Motion motion) {
		if (voxel.x > Constants.MAX_X || voxel.x + voxel_size < Constants.MIN_X || voxel.y > Constants.MAX_Y
				|| voxel.y + voxel_size < Constants.MIN_Y) return false;

		// this code detects intersection between a line segment and a square 
		// (geometric intersection, it ignores the time and speeds of aircraft)
		//
		// the intuition is that we transform the coordinates such that the line segment
		// ends up being a line from (0,0) to (1,1) ; in this transformed system, the coordinates of
		// the square (becomes rectangle) are (low_x,low_y,high_x,high_y) ; in this transformed system,
		// it is possible to detect the intersection without further arithmetics (just need comparisons)
		//
		// this algorithm is probably of general use ; I have seen too many online posts advising
		// more complex solution to the problem that involved calculating intersections between rectangle
		// sides and the segment/line
			
		Vector3d init = motion.getFirstPosition();
		Vector3d fin = motion.getSecondPosition();

		float v_s = voxel_size; 
		float r = Constants.PROXIMITY_RADIUS / 2.0f;

		float v_x = voxel.x;
		float x0 = init.x;
		float xv = fin.x - init.x;

		float v_y = voxel.y;
		float y0 = init.y;
		float yv = fin.y - init.y;

		float low_x, high_x;
		low_x = (v_x - r - x0) / xv;
		high_x = (v_x + v_s + r - x0) / xv;

		if (xv < 0.0f) {
			float tmp = low_x;
			low_x = high_x;
			high_x = tmp;
		}

		float low_y, high_y;
		low_y = (v_y - r - y0) / yv;
		high_y = (v_y + v_s + r - y0) / yv;

		if (yv < 0.0f) {
			float tmp = low_y;
			low_y = high_y;
			high_y = tmp;
		}
		// ugliest expression ever.
		boolean result = (
			(
			(xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s) /* no motion in x */ || 
				((low_x <= 1.0f && 1.0f <= high_x) || (low_x <= 0.0f && 0.0f <= high_x) || (0.0f <= low_x && high_x <= 1.0f))
			)
			&& 
			(
			(yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s) /* no motion in y */ || 
				((low_y <= 1.0f && 1.0f <= high_y) || (low_y <= 0.0f && 0.0f <= high_y) || (0.0f <= low_y && high_y <= 1.0f))
			) 
			&& 
			(xv == 0.0f || yv == 0.0f || /* no motion in x or y or both */
					(low_y <= high_x && high_x <= high_y) || (low_y <= low_x && low_x <= high_y) || (low_x <= low_y && high_y <= high_x))
		);
		return result;
	}
	
	// Iterative version of dfsVoxelHashRecurse
	// Easier to understand AND more efficient, I claim
	protected void dfsVoxelHashIter(Motion motion,
			Vector2d start_voxel, 
			HashMap<Vector2d, ArrayList<Motion>> voxel_map,
			HashMap<Vector2d, String> graph_colors) {
		Stack<Vector2d> pendingVoxels = new Stack<Vector2d>();
		pendingVoxels.add(start_voxel);
		while(! pendingVoxels.isEmpty()) {
			Vector2d next_voxel = pendingVoxels.pop();
			if(graph_colors.containsKey(next_voxel)) continue; // memo map
			if(!isInVoxel(next_voxel, motion)) continue;
			Vector2d tmp = new Vector2d();
			
			// left boundary
			VectorMath.subtract(next_voxel, horizontal, tmp);

			// right boundary
			VectorMath.add(next_voxel, horizontal, tmp);
			pendingVoxels.add(tmp);

			// upper boundary
			VectorMath.add(next_voxel, vertical, tmp);
			pendingVoxels.add(tmp);

			// lower boundary
			VectorMath.subtract(next_voxel, vertical, tmp);
			pendingVoxels.add(tmp);

			// upper-left
			VectorMath.subtract(next_voxel, horizontal, tmp);
			VectorMath.add(tmp, vertical, tmp);
			pendingVoxels.add(tmp);

			// upper-right
			VectorMath.add(next_voxel, horizontal, tmp);
			VectorMath.add(tmp, vertical, tmp);
			pendingVoxels.add(tmp);

			// lower-left
			VectorMath.subtract(next_voxel, horizontal, tmp);
			VectorMath.subtract(tmp, vertical, tmp);
			pendingVoxels.add(tmp);

			// lower-right
			VectorMath.add(next_voxel, horizontal, tmp);
			VectorMath.subtract(tmp, vertical, tmp);
			pendingVoxels.add(tmp);
		}
	}

	protected void dfsVoxelHashRecurse(Motion motion,
			Vector2d next_voxel, 
			HashMap<Vector2d, ArrayList<Motion>> voxel_map,
			HashMap<Vector2d, String> graph_colors) {

		Vector2d tmp = new Vector2d();
		// This is a dynamic programming algorithm
		if (isInVoxel(next_voxel, motion) && !graph_colors.containsKey(next_voxel)) {
			graph_colors.put(new Vector2d(next_voxel), "");
			putIntoMap(voxel_map, next_voxel, motion);

			// left boundary
			VectorMath.subtract(next_voxel, horizontal, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// right boundary
			VectorMath.add(next_voxel, horizontal, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// upper boundary
			VectorMath.add(next_voxel, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// lower boundary
			VectorMath.subtract(next_voxel, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// upper-left
			VectorMath.subtract(next_voxel, horizontal, tmp);
			VectorMath.add(tmp, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// upper-right
			VectorMath.add(next_voxel, horizontal, tmp);
			VectorMath.add(tmp, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// lower-left
			VectorMath.subtract(next_voxel, horizontal, tmp);
			VectorMath.subtract(tmp, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

			// lower-right
			VectorMath.add(next_voxel, horizontal, tmp);
			VectorMath.subtract(tmp, vertical, tmp);
			dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);
		}
	}

	/**
	 * Colors all of the voxels that overla the Motion.
	 */
	protected void performVoxelHashing(Motion motion, HashMap<Vector2d, ArrayList<Motion>> voxel_map, HashMap<Vector2d, String> graph_colors) {
		graph_colors.clear();
		Vector2d voxel = new Vector2d();
		voxelHash(motion.getFirstPosition(), voxel);
		dfsVoxelHashIter(motion, voxel, voxel_map, graph_colors);
	}

	/**
	 * Takes a List of Motions and returns an List of Lists of Motions, where the inner lists
	 * implement RandomAccess. Each Vector of Motions that is returned represents a set of Motions
	 * that might have collisions.
	 */
	public LinkedList<ArrayList<Motion>> reduceCollisionSet(List<Motion> motions) {

		HashMap<Vector2d, ArrayList<Motion>> voxel_map = new HashMap<Vector2d, ArrayList<Motion>>();
		HashMap<Vector2d, String> graph_colors = new HashMap<Vector2d, String>();

		for (Iterator<Motion> iter = motions.iterator(); iter.hasNext();)
			performVoxelHashing(iter.next(), voxel_map, graph_colors);

		LinkedList<ArrayList<Motion>> ret = new LinkedList<ArrayList<Motion>>();
		for (Iterator<ArrayList<Motion>> iter = voxel_map.values().iterator(); iter.hasNext();) {
			ArrayList<Motion> cur_set = iter.next();
			if (cur_set.size() > 1) ret.add(cur_set);
		}
		return ret;
	}
	/** The voxel size. Each voxel is a square, so the is the length of a side. */
	public float voxel_size;

	/** The horizontal side of a voxel. */
	public Vector2d horizontal;

	/** The vertical side of a voxel. */
	public Vector2d vertical;

	/** Initialize with a voxel size. */
	public Reducer(float voxel_size) {
		this.voxel_size = voxel_size;
		horizontal = new Vector2d(voxel_size, 0.0f);
		vertical = new Vector2d(0.0f, voxel_size);
	}
}
