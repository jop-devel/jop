/* $Id$
 * 
 * This file is a part of jPapaBench providing a Java implementation 
 * of PapaBench project.
 * Copyright (C) 2010  Michal Malohlava <michal.malohlava_at_d3s.mff.cuni.cz>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package papabench.core.autopilot.devices.impl;

import papabench.core.autopilot.devices.GPSDevice;

/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class GPSDeviceImpl implements GPSDevice {
	private int mode;
	private float tow; /* ms */
	private float altitude; /* m */
	private float speed; /* m/s */
	private float climb; /* m/s */
	private float course; /* rad */
	private int utmEast, utmNorth;
	private float east, north; /* m */
	
	private boolean positionAvailable = true;

	public void init() {		
	}
	
	public void reset() {
	}

	public int getMode() {
		return mode;
	}

	public float getTow() {
		return tow;
	}

	public float getAltitude() {
		return altitude;
	}

	public float getSpeed() {
		return speed;
	}

	public float getClimb() {
		return climb;
	}

	public float getCourse() {
		return course;
	}

	public int getUtmEast() {
		return utmEast;
	}

	public int getUtmNorth() {
		return utmNorth;
	}

	public float getEast() {
		return east;
	}

	public float getNorth() {
		return north;
	}

	public boolean isPositionAvailable() {
		return positionAvailable;
	}
}
