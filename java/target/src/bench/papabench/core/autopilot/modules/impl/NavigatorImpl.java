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
package papabench.core.autopilot.modules.impl;

import papabench.core.autopilot.data.Position2D;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.data.FlightPlan;

/**
 * Navigator module is responsible for navigation according given flightplan.
 * 
 * @author Michal Malohlava
 *
 */
public class NavigatorImpl implements Navigator {
	
	protected static final float MIN_HEIGHT_CARROT = 50;
	protected static final float MAX_HEIGHT_CARROT = 150;
	
	private FlightPlan flightPlan;	
	
	private AutopilotModule autopilotModule;
	
	private Position2D desiredPosition;
	private float desiredCourse;
	private float desiredAltitude;
	private float desiredRoll;
	private float desiredPitch;
	private float preClimb;
	private boolean autoPitch;
	private int desiredGaz;
	
	private int autoNavigateCounter;
		
	public void init() {
		if (flightPlan == null || autopilotModule == null) {
			throw new IllegalArgumentException("Navigator module has wrong configuration!");
		}
		
		flightPlan.setEstimator(autopilotModule.getEstimator());
		flightPlan.setAutopilotStatus(autopilotModule);
		flightPlan.setNavigator(this);
		flightPlan.setAutopilotModule(autopilotModule);
		
		flightPlan.init();
		
		this.desiredPosition = new Position2D(0, 0);
		this.desiredPitch = AirframeParametersConf.NAV_PITCH;
		this.desiredAltitude = flightPlan.getGroundAltitude() + MIN_HEIGHT_CARROT;
		this.autoNavigateCounter = 0;
	}
	
	public void autoNavigate() {
		flightPlan.execute();
	}
	
	public FlightPlan getFlightPlan() {
		return flightPlan;
	}

	public void setFlightPlan(FlightPlan flightPlan) {
		this.flightPlan = flightPlan;
	}
	
	public float getDesiredCourse() {
		return desiredCourse;
	}


	public void setDesiredCourse(float desiredCourse) {
		this.desiredCourse = desiredCourse;
	}


	public float getDesiredAltitude() {
		return desiredAltitude;
	}


	public void setDesiredAltitude(float desiredAltitude) {
		this.desiredAltitude = desiredAltitude;	
	}	
	public void setDesiredRoll(float roll) {
		this.desiredRoll = roll;
	}
	public float getDesiredRoll() {
		return this.desiredRoll;
	}
	
	public float getDesiredPitch() {
		return this.desiredPitch;
	}
	
	public void setDesiredPitch(float pitch) {
		this.desiredPitch = pitch;
	}


	public float getPreClimb() {
		return preClimb;
	}


	public void setPreClimb(float preClimb) {
		this.preClimb = preClimb;
	}


	public boolean isAutoPitch() {
		return autoPitch;
	}


	public void setAutoPitch(boolean autoPitch) {
		this.autoPitch = autoPitch;
	}


	public int getDesiredGaz() {
		return desiredGaz;
	}


	public void setDesiredGaz(int desiredGaz) {
		this.desiredGaz = desiredGaz;
	}


	public void setAutopilotModule(AutopilotModule autopilotModule) {
		this.autopilotModule = autopilotModule;
	}

	public Position2D getDesiredPosition() {
		return this.desiredPosition;
	}

	public void setDesiredPosition(float x, float y) {
		this.desiredPosition.x = x;
		this.desiredPosition.y = y;
	}
	
	public int getAutoNavigationCycleNumber() {
		return autoNavigateCounter;
	}


}
