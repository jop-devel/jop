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
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package hijac.cdx;

import hijac.cdx.Constants;
import hijac.cdx.Motion;
import hijac.cdx.Vector2d;
import hijac.cdx.Vector3d;
import hijac.cdx.VectorMath;
import hijac.cdx.javacp.utils.ArrayList;
import hijac.cdx.javacp.utils.HashMap;
import hijac.cdx.javacp.utils.Iterator;
import hijac.cdx.javacp.utils.LinkedList;

/**
 * Reduces the set of collisions to be tested using a voxel hashing algorithm.
 *
 * @author Filip Pizlo, Jeff Hagelberg
 */
class Reducer {
  /**
   * The voxel size. Each voxel is a square, so this is the length of a side.
   */
  public final float voxel_size;

  /**
   * The horizontal side of a voxel.
   */
  public final Vector2d horizontal;

  /**
   * The vertical side of a voxel.
   */
  public final Vector2d vertical;

  /**
   * Initialise the reducer with a voxel size.
   */
  public Reducer(float voxel_size) {
    this.voxel_size = voxel_size;
    horizontal = new Vector2d(voxel_size, 0.0f);
    vertical = new Vector2d(0.0f, voxel_size);
  }

  /**
   * Creates a Vector2d that represents a voxel.
   */
  protected void voxelHash(Vector3d position, Vector2d voxel) {
    int x_div = (int) (position.x / voxel_size);
    voxel.x = voxel_size * (x_div);
    if (position.x < 0.0f) { voxel.x -= voxel_size; }

    int y_div = (int) (position.y / voxel_size);
    voxel.y = voxel_size * (y_div);
    if (position.y < 0.0f) { voxel.y -= voxel_size; }
  }

  /**
   * Puts a Motion object into the voxel map at a voxel.
   */
  protected void putIntoMap(
      HashMap voxel_map, Vector2d voxel, Motion motion) {
    if (!voxel_map.containsKey(voxel)) {
      voxel_map.put(new Vector2d(voxel), new ArrayList());
    }
    ((ArrayList) voxel_map.get(voxel)).add(motion);
  }

  /**
   * Given a voxel and a Motion, determines if they overlap.
   */
  protected boolean isInVoxel(Vector2d voxel, Motion motion) {
    if (voxel.x > Constants.MAX_X ||
        voxel.x + voxel_size < Constants.MIN_X ||
        voxel.y > Constants.MAX_Y ||
        voxel.y + voxel_size < Constants.MIN_Y) {
      return false;
    }
    /* This code detects intersection between a line segment and a square
     * (geometric intersection, it ignores the time and speeds of aircraft).
     *
     * The intuition is that we transform the coordinates such that the
     * line segment ends up being a line from (0,0) to (1,1); in this
     * transformed system, the coordinates of the square (becomes rectangle)
     * are (low_x, low_y, high_x, high_y); in this transformed system, it is
     * possible to detect the intersection without further arithmetics
     * (just need comparisons).
     *
     * This algorithm is probably of general use; I have seen too many online
     * posts advising more complex solution to the problem that involved
     * calculating intersections between rectangle sides and line segment. */
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

    float low_x = (v_x - r - x0) / xv;
    float high_x = (v_x + v_s + r - x0) / xv;

    if (xv < 0.0f) {
      float tmp = low_x;
      low_x = high_x;
      high_x = tmp;
    }

    float low_y = (v_y - r - y0) / yv;
    float high_y = (v_y + v_s + r - y0) / yv;

    if (yv < 0.0f) {
      float tmp = low_y;
      low_y = high_y;
      high_y = tmp;
    }

    boolean result = (
      /* no motion in x */
      ((xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s)
        || ((low_x <= 1.0f && 1.0f <= high_x)
          || (low_x <= 0.0f && 0.0f <= high_x)
          || (0.0f <= low_x && high_x <= 1.0f)))
     &&
      /* no motion in y */
      ((yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s)
        || ((low_y <= 1.0f && 1.0f <= high_y)
          || (low_y <= 0.0f && 0.0f <= high_y)
          || (0.0f <= low_y && high_y <= 1.0f)))
     &&
      /* no motion in x or y or both */
      (xv == 0.0f
        || yv == 0.0f
        || (low_y <= high_x && high_x <= high_y)
        || (low_y <= low_x && low_x <= high_y)
        || (low_x <= low_y && high_y <= high_x)));

    return result;
  }

  protected void dfsVoxelHashRecurse(Motion motion, Vector2d next_voxel,
      HashMap voxel_map, HashMap graph_colors) {
    Vector2d tmp = new Vector2d();

    if (!graph_colors.containsKey(next_voxel) &&
      isInVoxel(next_voxel, motion)) {

      graph_colors.put(new Vector2d(next_voxel), "");
      putIntoMap(voxel_map, next_voxel, motion);

      /* Left Boundary */
      VectorMath.subtract(next_voxel, horizontal, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Right Boundary */
      VectorMath.add(next_voxel, horizontal, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Upper Boundary */
      VectorMath.add(next_voxel, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Lower Boundary */
      VectorMath.subtract(next_voxel, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Upper-left Boundary */
      VectorMath.subtract(next_voxel, horizontal, tmp);
      VectorMath.add(tmp, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Upper-right Boundary */
      VectorMath.add(next_voxel, horizontal, tmp);
      VectorMath.add(tmp, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Lower-left Boundary */
      VectorMath.subtract(next_voxel, horizontal, tmp);
      VectorMath.subtract(tmp, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);

      /* Lower-right Boundary */
      VectorMath.add(next_voxel, horizontal, tmp);
      VectorMath.subtract(tmp, vertical, tmp);
      dfsVoxelHashRecurse(motion, tmp, voxel_map, graph_colors);
    }
  }

  /**
   * Colours all of the voxels that overlap with the Motion.
   */
  protected void performVoxelHashing(
      Motion motion, HashMap voxel_map, HashMap graph_colors) {
    graph_colors.clear();
    Vector2d voxel = new Vector2d();
    voxelHash(motion.getFirstPosition(), voxel);
    dfsVoxelHashRecurse(motion, voxel, voxel_map, graph_colors);
  }

  /**
   * Takes a List of Motions and returns an List of Lists of Motions, where
   * the inner lists implement RandomAccess.
   * Each Vector of Motions that is returned represents a set of Motions that
   * might have collisions.
   */
  public LinkedList reduceCollisionSet(LinkedList motions) {
    HashMap voxel_map = new HashMap();
    HashMap graph_colors = new HashMap();

    for (Iterator iter = motions.iterator(); iter.hasNext(); ) {
      performVoxelHashing((Motion) iter.next(), voxel_map, graph_colors);
    }

    LinkedList ret = new LinkedList();

    for (Iterator iter = voxel_map.values().iterator(); iter.hasNext();) {
      LinkedList cur_set = (LinkedList) iter.next();
      if (cur_set.size() > 1) { ret.add(cur_set); }
    }
    return ret;
  }
}
