/* $Id: NavigatorCommands.java 606 2010-11-02 19:52:33Z parizek $
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
package papabench.core.commons.data.impl;

import static papabench.core.commons.conf.AirframeParametersConf.CARROT;
import static papabench.core.commons.conf.AirframeParametersConf.NOMINAL_AIRSPEED;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.utils.LogUtils;
import papabench.core.utils.MathUtils;

/**
 * Generalized implementation of navigator commands.
 * 
 * FIXME AbstractFLightPlan should inherit from this method!
 * NOTE: this file is partly based on Paparazzi code (method prefix nav) and partly on Papabench code.
 * 
 * @author Michal Malohlava
 * 
 */
public abstract class NavigatorCommands {
		
	protected abstract Navigator navigator();
	protected abstract Estimator estimator();
	protected abstract AutopilotStatus status();
	protected abstract Position3D WP(int n);
	protected abstract int getLastWPNumber();
	protected abstract Position3D getLastPosition();
	
	private float carrot = 0;

	/**
	 * Set the climb control to auto-throttle with the specified
	 * pitchpre-command
	 */
	protected final void navVerticalAutoThrottleMode(float pitch) {
		// FIXME v_ctl_climb_mode = V_CTL_CLIMB_MODE_AUTO_THROTTLE; \
//		navigator().setDesiredPitch(pitch);
		status().setVerticalFlightMode(VerticalFlightMode.AUTO_GAZ);
		status().setPitch(pitch);
	}

	/**
	 * Set the climb control to auto-pitch with the specified throttle
	 * pre-command
	 */
	protected final void navVerticalAutoPitchMode(int throttle) {
		// _ctl_climb_mode = V_CTL_CLIMB_MODE_AUTO_PITCH;
		// nav_throttle_setpoint = _throttle;
		navigator().setAutoPitch(true);
		navigator().setDesiredGaz(throttle);
	}

	/**
	 * Set the vertical mode to altitude control with the specified altitude
	 * setpoint and climb pre-command.
	 */
	protected final void navVerticalAltitudeMode(float alt, float preClimb) {
		// v_ctl_mode = V_CTL_MODE_AUTO_ALT;
		status().setVerticalFlightMode(VerticalFlightMode.AUTO_ALTITUDE);
		navigator().setDesiredAltitude(alt);
		navigator().setPreClimb(preClimb);
	}

	/** Set the vertical mode to climb control with the specified climb setpoint */
	protected final void navVerticalClimbMode(float climb) {
		// v_ctl_mode = V_CTL_MODE_AUTO_CLIMB;
		// v_ctl_climb_setpoint = _climb;
		status().setVerticalFlightMode(VerticalFlightMode.AUTO_CLIMB); // AUTO_CLIMB -> navigator has to obtain values
		status().setClimb(climb);

	}

	/** Set the vertical mode to fixed throttle with the specified setpoint */
	protected final void navVerticalThrottleMode(int throttle) {
		// v_ctl_mode = V_CTL_MODE_AUTO_THROTTLE;
		// nav_throttle_setpoint = _throttle;
		status().setVerticalFlightMode(VerticalFlightMode.AUTO_GAZ);
		navigator().setDesiredGaz(throttle);
	}

	protected final void navHeading(float course) {
		// lateral_mode = LATERAL_MODE_COURSE;
		// h_ctl_course_setpoint = _course;
		status().setLateralFlightMode(LateralFlightMode.COURSE);
		navigator().setDesiredCourse(course);		
	}

	protected final void navAttitude(float roll) {
		// lateral_mode = LATERAL_MODE_ROLL;
		// h_ctl_roll_setpoint = _roll;
		status().setLateralFlightMode(LateralFlightMode.ROLL);		
		status().setRoll(roll);
	}
	
	/**
	 *  Decide if the UAV is approaching the current waypoint.
	 *  Computes dist2_to_wp and compare it to square carrot.
	 *  Return true if it is smaller. Else computes by scalar products if 
	 *  uav has not gone past waypoint.
	 *  Return true if it is the case.
	 */
	protected final boolean navApproachingFrom(int toWP, int fromWP, float approachingTime) {
		return navApproachingFrom(WP(toWP), WP(fromWP), approachingTime);		
	}
	protected final boolean navApproachingFrom(Position3D toWP, Position3D fromWP, float approachingTime) {
		float pwX = toWP.x - estimator().getPosition().x;
		float pwY = toWP.y - estimator().getPosition().y;
		
		float dist2ToWP = pwX*pwX + pwY*pwY;
		float minDist = approachingTime * estimator().getHorizontalSpeed().module;
		
		if (dist2ToWP < minDist*minDist) {
			return true;
		}
		
		float scalarProduct = MathUtils.scalarProduct(toWP, fromWP, estimator().getPosition());
		
		return (scalarProduct < 0.);						
	}
	
	protected final void navGotoWaypoint(int wp) {
//		  horizontal_mode = HORIZONTAL_MODE_WAYPOINT;
		  flyToWP(WP(wp));
	}
	
	protected final void navSegment(Position3D startWP, Position3D endWP) {
		navRouteXY(startWP, endWP);
	}
	
	protected final void navRouteXY(Position3D startWP, Position3D endWP) {
		float legX = endWP.x - startWP.x;
		float legY = endWP.y - startWP.y;
		float leg2 = Math.max(legX*legX+legY*legY,1f);
		float navLegProgress = ((estimator().getPosition().x - startWP.x)*legX + (estimator().getPosition().y - startWP.y)*legY) / leg2;
		float navLegLength = (float) Math.sqrt(leg2);
		
		/* distance of CARROT */
		float carrot = CARROT * NOMINAL_AIRSPEED;
		
//		navLegProgress += Math.max(carrot/navLegLength, 0);
//		navInSegment = true;
//		navSegment.1 = startWP;
//		navSegment.2 = endWP;
//		
//		horizontal_mode = ROUTE;
//		
//		flyToXY(startWP.x+navLegProgress*legX + navShift*legY/navLegLength, startWP.y+navLegProgress*legY-navShift*legX/navLegLength);
		
	}
	
	protected final void killThrottle() {		
	}
	
	protected final boolean approaching(int wp) {
		return approaching(WP(wp));
	}
	
	protected final boolean approaching(Position3D wp) {
		return approaching(wp, getLastPosition());
	}
	
	protected final boolean approaching(Position3D toWP, Position3D fromWP) {
		//LogUtils.log(this, "Approaching to=" + toWP + " from="+fromWP);
		float pwX = toWP.x - estimator().getPosition().x;
		float pwY = toWP.y - estimator().getPosition().y;
		
		float dist2ToWP = pwX*pwX + pwY*pwY;
		
		carrot = CARROT * estimator().getHorizontalSpeed().module;
		carrot = carrot < 40 ? 40 : carrot; // FIXME what is number 10 (40 in original version)? replace by constant with user friendly name
		
		if (dist2ToWP < carrot*carrot) {
			return true;
		}
		
		float scalarProduct = MathUtils.scalarProduct(toWP, fromWP, estimator().getPosition());

		return (scalarProduct < 0.);
	}
	
	protected final void flyToWP(int wp) {
		flyToWP(WP(wp));		
	}
	
	protected final void flyToWP(Position3D wp) {
		flyToXY(wp.x, wp.y);		
	}
	
	// WARNING - this method is implemented according to PapaBench code (it is not compatible with paparazzi)
	protected final void flyToXY(float x, float y) {
		
		navigator().setDesiredPosition(x,y);		
		navigator().setDesiredCourse((float) (Math.PI/2 - Math.atan2(y-estimator().getPosition().y, x-estimator().getPosition().x)));
		//LogUtils.log(this, "FlyToWP: (" + x + ", "+ y + "), course = " + Math.toDegrees(navigator().getDesiredCourse()));
	}
	
	protected final void routeTo(int fromWP, int toWP) {
		routeTo(WP(fromWP), WP(toWP));		
	}
	
	protected final void routeTo(Position3D fromWP, Position3D toWP) {
		float legX = toWP.x - fromWP.x;
		float legY = toWP.y - fromWP.y;
		float leg2 = Math.max(legX*legX+legY*legY,1f);
		float alpha = ((estimator().getPosition().x - fromWP.x) * legX + (estimator().getPosition().y - fromWP.y)*legY) / leg2;
		alpha = Math.max(alpha, 0);
		float leg = (float) Math.sqrt(leg2);
		alpha += Math.max(carrot/2, 0f ); /* carrot computed in approaching() */
		alpha = Math.min(1, alpha);
		
		flyToXY(fromWP.x + alpha*legX, fromWP.y + alpha*legY);
	}
}
