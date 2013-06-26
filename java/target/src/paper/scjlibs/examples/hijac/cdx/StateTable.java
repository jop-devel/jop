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

import scjlibs.examples.hijac.cdx.CallSign;
import scjlibs.examples.hijac.cdx.Constants;
import scjlibs.examples.hijac.cdx.Error;
import scjlibs.examples.hijac.cdx.Vector3d;
import scjlibs.util.HashMap;
import scjlibs.util.Set;
//import hijac.cdx.javacp.utils.Set;
//import java.util.Set;

/**
 * The class StateTable records the state of the previous frame.
 * 
 * Allocated CallSign objects and Vector3d objects for positions are held in
 * mission memory.
 */
public class StateTable {
	/* Mapping CallSign -> Vector3d. We use a customised HashMap here. */
	// final private CHashMap motionVectors;
	final private HashMap<CallSign, Vector3d> motionVectors;

	/* Fields to manage pool of pre-allocation objects in mission memory. */
	public CallSign[] allocatedCallSigns;
	public Vector3d[] allocatedVectors;
	public int usedSlots;

	public StateTable() {
		// motionVectors = new CHashMap(Constants.MAX_OF_PLANES);
		motionVectors = new HashMap<CallSign, Vector3d>(Constants.MAX_OF_PLANES);
		initObjectPool();
	}

	private void initObjectPool() {

		allocatedCallSigns = new CallSign[Constants.NUMBER_OF_PLANES];
		allocatedVectors = new Vector3d[Constants.NUMBER_OF_PLANES];

		/* Pre-allocate all objects during construction. */
		for (int i = 0; i < allocatedVectors.length; i++) {
			allocatedCallSigns[i] = new CallSign();
			allocatedVectors[i] = new Vector3d();
		}

		usedSlots = 0;
	}

	public Set<CallSign> getCallSigns() {
		return motionVectors.keySet();
	}

	public void put(CallSign callsign, final float x, final float y,
			final float z) {

		Vector3d v = (Vector3d) motionVectors.get(callsign);

		if (v == null) {
			/* No position vector yet in the map for callsign. */
			if (usedSlots == allocatedCallSigns.length) {
				Error.abort("Exceeding storage capacity in StateTable.");
			}
			/* Obtain pre-allocated CallSign object from the store. */
			CallSign c = allocatedCallSigns[usedSlots];

			/*
			 * Copy content of callsign to the respective persistent object.
			 * This is valid because we are copying primitive values
			 */
			for (int i = 0; i < callsign.get().length; i++) {
				c.get()[i] = callsign.get()[i];
			}
			
			/* Obtain pre-allocated Vector3d object from the store. */
			v = allocatedVectors[usedSlots++];

			/* Put new entry into the (customised) CHashMap. */
			motionVectors.put(c, v);
		}

		/* Finally update the components of the position vector. */
		v.x = x;
		v.y = y;
		v.z = z;
	}

	public Vector3d get(final CallSign callsign) {
		return (Vector3d) motionVectors.get(callsign);
	}

	public Vector3d remove(final CallSign callsign) {
		return (Vector3d) motionVectors.remove(callsign);
	}
	
	public int getSize(){
		return motionVectors.size();
	}
}
