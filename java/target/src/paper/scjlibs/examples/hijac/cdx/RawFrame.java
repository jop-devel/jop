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

import scjlibs.examples.hijac.cdx.Constants;
import scjlibs.examples.hijac.cdx.RawFrame;

public class RawFrame {

	public final byte[] callsigns = new byte[Constants.LENGTH_OF_CALLSIGN
			* Constants.NUMBER_OF_PLANES];

	public final float[] positions = new float[3 * Constants.NUMBER_OF_PLANES];

	public int planeCnt;

	/**
	 * Initialise this object from a callsigns and positions array.
	 * 
	 * @param signs
	 *            a sequence of callsigns
	 * @param posns
	 *            a sequnece of 3d positions
	 */
	public synchronized void copy(final byte[] signs, final float[] posns) {
		for (int i = 0; i < posns.length; i++) {
			positions[i] = posns[i];
		}
		for (int j = 0; j < signs.length; j++) {
			callsigns[j] = signs[j];
		}
		planeCnt = posns.length / 3;
	}

	/**
	 * Initialise this object from a another RawFrame object.
	 * 
	 * @param frame
	 *            a {@code RawFrame} object
	 */
	public synchronized void copy(final RawFrame frame) {
		copy(frame.callsigns, frame.positions);
	}
}
