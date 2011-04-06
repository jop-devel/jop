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
package papabench.core.simulator.devices.impl;

import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.MathUtils;
import papabench.core.utils.StatsUtils;

/**
 * Simulated GPS device implementation. It provides data computed according last airplane position and state.
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorGPSDeviceImpl implements GPSDevice, SimulatedDevice {
	
	private float altitude;
	private float climb;
	private float course;
	private float east;
	private float north;
	private int utmEast;
	private int utmNorth;
	private float speed;
	private int mode;
	private float tow; // time
	private boolean positionAvailable = false;
	
	/* last values */	
	private Position3D lastPosition;	
	private float lastSpeed = 0;
	private float lastCourse = 0;
	private float lastClimb = 0;
	private float lastTime = 0;

	public void init() {
		lastPosition = new Position3D(0, 0, 0);
		positionAvailable = false;
		mode = 1 << 5;
	}

	public void reset() {
	}
	
	public void update(FlightModel flightModel) {
		update(flightModel.getState().getPosition(), /*FIXME*/ 125, flightModel.getState().getTime());		
	}

	// FIXME should reflects initial UTM position of airplane : now initial position is (0,0). The same for altitude
	protected void update(Position3D position, float groundAltitude, float time) {
		float dt = time - lastTime;
		
		if (dt > 0f) {
			float dx = position.x - lastPosition.x;
			float dy = position.y - lastPosition.y;
			lastSpeed = (float) (Math.sqrt(dx*dx + dy*dy) / dt);
			lastCourse = MathUtils.normalizeRadAngle((float) (Math.PI - Math.atan2(dy, dx)));
			lastClimb = (position.z - lastPosition.z) / dt;
		}

// FIXME following code transforms (x,y) position to UTM position reflecting initial position 		
//	   let utm0 = utm_of WGS84 !pos0 in
//	   let utm = utm_add utm0 (x, y) in
//	   let wgs84 = of_utm WGS84 utm

	
		lastPosition.x = position.x;
		lastPosition.y = position.y;
		lastPosition.z = position.z;
		lastTime = time;
		
		
		// update state of GPSDevice
		altitude = groundAltitude + position.z;
		course = (float) (lastCourse < 0 ? lastCourse + MathUtils.TWO_PI : lastCourse);
		climb = climbNoise(lastClimb);
		speed = lastSpeed;
		east = position.x;
		north = position.y;
		// utm position ignored
		
		
		this.positionAvailable = true;
	}


	private float climbNoise(float climb) {
		return climb + StatsUtils.randNormalDistribution(0.9f, 0.1f);
	}

	public float getAltitude() {		
		return altitude;
	}

	public float getClimb() {		
		return climb;
	}

	public float getCourse() {
		return course;
	}

	public float getEast() {
		return east;
	}

	public int getMode() {		
		return mode;
	}

	public float getNorth() {
		return north;
	}

	public float getSpeed() {
		return speed;
	}

	public float getTow() {
		return tow;
	}

	public int getUtmEast() {
		return utmEast;
	}

	public int getUtmNorth() {
		return utmNorth;
	}

	public boolean isPositionAvailable() {
		return positionAvailable;
	}

}
