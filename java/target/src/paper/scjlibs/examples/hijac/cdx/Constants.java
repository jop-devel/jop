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
 *   @authors  Daniel Tang, Ales Plsek, Kun Wei, Frank Zeyda
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
package scjlibs.examples.hijac.cdx;

/**
 * All of relevant global constants for the parallel CDx.
 */
public final class Constants {
	/**
	 * Constants about frames and voxels.
	 */
	public static final float MIN_X = 0.0f;
	public static final float MIN_Y = 0.0f;
	public static final float MIN_Z = 0.0f;
	public static final float MAX_X = 1000.0f;
	public static final float MAX_Y = 1000.0f;
	public static final float MAX_Z = 10.0f;
	public static final float PROXIMITY_RADIUS = 1.0f;
	public static final float GOOD_VOXEL_SIZE = PROXIMITY_RADIUS * 10.0f;

	/**
	 * Constants about the simulator and detectors.
	 */
	/*
	 * TODO: Why are the commented out constants included? Are they really
	 * needed in the program?
	 */
	// public static int SIMULATOR_PRIORITY = 5;
	// public static int SIMULATOR_TIME_SCALE = 1;
	// public static int SIMULATOR_FPS = 50;
	// public static int DETECTOR_STARTUP_PRIORITY = 9;
	public static int DETECTOR_PRIORITY = 9; // DETECTOR_STARTUP_PRIORITY + 1;
	public static long PERSISTENT_DETECTOR_SCOPE_SIZE = 5 * 100 * 1000;
	public static long DETECTOR_PERIOD = 50;
	public static long TRANSIENT_DETECTOR_SCOPE_SIZE = 2048;// 5*100*1000;
	public static long TRANSIENT_DETECTOR_BS_SIZE = 4096;// 5*100*1000;

	public static long INPUT_FRAME_HANDLER_BS_SIZE = 550;
	public static long INPUT_FRAME_HANDLER_SCOPE_SIZE = 550;

	public static long REDUCER_HANDLER_BS_SIZE = 9000;
	public static long REDUCER_HANDLER_SCOPE_SIZE = 9000;

	public static long DETECTOR_HANDLER_BS_SIZE = 600;
	public static long DETECTOR_HANDLER_SCOPE_SIZE = 600;

	public static long OUTPUT_HANDLER_BS_SIZE = 400;
	public static long OUTPUT_HANDLER_SCOPE_SIZE = 400;

	public static int MAX_FRAMES = 15;
	/* Number of parallel detector handlers (instances of DetectorHandler). */
	public static int DETECTORS = 4;

	/**
	 * Constants about planes and callsigns.
	 */
	public static int MAX_OF_PLANES = 250;
	public static int NUMBER_OF_PLANES = 10;
	public static int LENGTH_OF_CALLSIGN = 6;

	/**
	 * Utility method to calculate the number of voxels for a given voxel size.
	 */
	public static int voxels(float voxel_size) {
		return (int) ((((MAX_X - MIN_X) / voxel_size) + 1) * (((MAX_Y - MIN_Y) / voxel_size) + 1));
	}
}
