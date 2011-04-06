/* $Id: StateImpl.java 606 2010-11-02 19:52:33Z parizek $
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
package papabench.core.simulator.model.impl;
import static papabench.core.commons.conf.AirframeParametersConf.CRUISE_THROTTLE;
import static papabench.core.commons.conf.AirframeParametersConf.G;
import static papabench.core.commons.conf.AirframeParametersConf.MAXIMUM_AIRSPEED;
import static papabench.core.commons.conf.AirframeParametersConf.MAXIMUM_POWER;
import static papabench.core.commons.conf.AirframeParametersConf.NOMINAL_AIRSPEED;
import static papabench.core.commons.conf.AirframeParametersConf.ROLL_RESPONSE_FACTOR;
import static papabench.core.commons.conf.AirframeParametersConf.WEIGHT;
import static papabench.core.commons.conf.AirframeParametersConf.YAW_RESPONSE_FACTOR;
import static papabench.core.commons.conf.RadioConf.MAX_THRUST;
import static papabench.core.commons.conf.RadioConf.MIN_THRUST;
import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.data.Position2D;
import papabench.core.autopilot.data.Position3D;
import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.data.RadioCommands;
import papabench.core.simulator.conf.SimulatorConf;
import papabench.core.simulator.model.State;
import papabench.core.utils.LogUtils;
import papabench.core.utils.MathUtils;

/**
 * Simulated environment state.
 * 
 * @author Michal Malohlava
 *
 */
public class StateImpl implements State {
	
	private Position3D position; // (m,m,m)
	
	private Attitude attitude; // psi/phi/theta in rad
	private Attitude rotSpeed; // rad/s
	private float zDot; // m/s
	private float airSpeed; // m/s
	private float time; // sec
	private float thrust; 
	private Position2D delta;
	
	public void init() {
		this.position = new Position3D(0, 0, 0);
		this.attitude = new Attitude(0, 0, 0);
		this.rotSpeed = new Attitude(0, 0, 0);
		this.delta = new Position2D(0, 0);
		this.zDot = 0;
		this.airSpeed = 0;
		this.time = 0;
		this.thrust = 0;
	}
	
	public void updateState(float dt, Position3D wind) {
		float now = time + dt;
		
		if (airSpeed == 0 && thrust > 0) {
			airSpeed = NOMINAL_AIRSPEED;			
		}
		
		if (airSpeed > 0) {
			float v2 = airSpeed*airSpeed;
			float vn2 = NOMINAL_AIRSPEED * NOMINAL_AIRSPEED;
			
			float phiDotDot = ROLL_RESPONSE_FACTOR * delta.x * v2/vn2 - rotSpeed.phi;
			this.rotSpeed.phi += phiDotDot*dt;
			this.rotSpeed.phi = MathUtils.symmetricalLimiter(this.rotSpeed.phi, SimulatorConf.MAX_PHI_DOT);
			this.attitude.phi = MathUtils.normalizeRadAngle(this.attitude.phi + this.rotSpeed.phi * dt);
			this.attitude.phi = MathUtils.symmetricalLimiter(this.attitude.phi, SimulatorConf.MAX_PHI);
			
			float psiDot = (float) (-G / airSpeed * Math.tan(YAW_RESPONSE_FACTOR * attitude.phi));
			
			this.attitude.psi = MathUtils.normalizeRadAngle(this.attitude.psi + psiDot * dt);
			
			float cM = 5e-7f * this.delta.y;
			float thetaDotDot = cM * v2 - this.rotSpeed.theta;
			this.rotSpeed.theta += thetaDotDot * dt;
			this.attitude.theta += this.rotSpeed.theta * dt;
			
			float gamma = (float) Math.atan2(zDot, this.airSpeed);
			float alpha = attitude.theta - gamma;
			float cZ = 0.2f * alpha + AirframeParametersConf.G / vn2; // FIXME why is there constant 0.2 (copied from paparazzi simulator)
			
			float lift = cZ * airSpeed * airSpeed;
			float zDotDot = (float) (lift/WEIGHT * Math.cos(this.attitude.theta) * Math.cos(this.attitude.phi) - AirframeParametersConf.G);
			this.zDot += zDotDot * dt;
			this.position.z += zDot * dt;
			
			float drag = CRUISE_THROTTLE + (v2 - vn2)*(1 - CRUISE_THROTTLE)/(MAXIMUM_AIRSPEED*MAXIMUM_AIRSPEED - vn2);
			float airSpeedDot = (float) (MAXIMUM_POWER / this.airSpeed * (this.thrust - drag)/WEIGHT - G*Math.sin(gamma));
			this.airSpeed += airSpeedDot * dt;
			this.airSpeed = Math.max(this.airSpeed, NOMINAL_AIRSPEED);
			
			float xDot = (float) (this.airSpeed * Math.cos(attitude.psi) + wind.x);
			float yDot = (float) (this.airSpeed * Math.sin(attitude.psi) + wind.y);
			
			this.position.x += xDot * dt;
			this.position.y += yDot * dt;
			this.position.z += wind.z * dt;			
		}
		
		time = now;
	}
	
	public void updateState(RadioCommands commands) {
		float cLda = 4e-5f;
		
		//LogUtils.log(this, "Radio commands for simulator state update: " + commands.toString());
		this.delta.x = -cLda * commands.getRoll();
		this.delta.y = commands.getPitch();
		this.thrust = (commands.getThrottle() - MIN_THRUST) / (float) (MAX_THRUST - MIN_THRUST);
		//LogUtils.log(this, "State Updated: delta.x="+delta.x + ", delta.y="+delta.y +", thrust="+thrust);
	}

	public Attitude getAttitude() {
		return attitude;
	}

	public Position2D getDelta() {
		return delta;
	}

	public Position3D getPosition() {
		return position;
	}

	public Attitude getRotationalSpeed() {
		return rotSpeed;
	}

	public float getAirSpeed() {		
		return airSpeed;
	}

	public float getThrust() {
		return thrust;
	}

	public float getTime() {		
		return time;
	}

	public float getZDot() {		
		return zDot;
	}

	@Override
	public String toString() {
		return "StateImpl [airSpeed=" + airSpeed + ", attitude=" + attitude
				+ ", delta=" + delta + ", position=" + position + ", rotSpeed="
				+ rotSpeed + ", thrust=" + thrust + ", time=" + time
				+ ", zDot=" + zDot + "]";
	}
}
