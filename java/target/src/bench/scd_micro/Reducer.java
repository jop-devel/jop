package scd_micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


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
	protected void putIntoMap(HashMap voxel_map, Vector2d voxel, Motion motion) {
		if (!voxel_map.containsKey(voxel)) {
			voxel_map.put(new Vector2d(voxel), new ArrayList());
		}
		((ArrayList) voxel_map.get(voxel)).add(motion);
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

	protected void dfsVoxelHashRecurse(Motion motion, Vector2d next_voxel, HashMap voxel_map, HashMap graph_colors) {
		Vector2d tmp = new Vector2d();

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
	protected void performVoxelHashing(Motion motion, HashMap voxel_map, HashMap graph_colors) {
		graph_colors.clear();
		Vector2d voxel = new Vector2d();
		voxelHash(motion.getFirstPosition(), voxel);
		dfsVoxelHashRecurse(motion, voxel, voxel_map, graph_colors);
	}

	/**
	 * Takes a List of Motions and returns an List of Lists of Motions, where the inner lists
	 * implement RandomAccess. Each Vector of Motions that is returned represents a set of Motions
	 * that might have collisions.
	 */
	public LinkedList reduceCollisionSet(LinkedList motions) {

		HashMap voxel_map = new HashMap();
		HashMap graph_colors = new HashMap();

		for (Iterator iter = motions.iterator(); iter.hasNext();)
			performVoxelHashing((Motion) iter.next(), voxel_map, graph_colors);

		LinkedList ret = new LinkedList();
		for (Iterator iter = voxel_map.values().iterator(); iter.hasNext();) {
			LinkedList cur_set = (LinkedList) iter.next();
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
