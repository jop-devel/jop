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
package papabench.core.autopilot.modules;

import papabench.core.autopilot.data.Position2D;
import papabench.core.commons.data.FlightPlan;
import papabench.core.commons.modules.Module;

/**
 * Navigator module access interface.
 * 
 * Note: this interface should be used by flight plan to setup desired properties for autopilot.
 * 
 * @author Michal Malohlava
 *
 */
public interface Navigator extends Module {
	/* predefined flight plan */
	FlightPlan getFlightPlan();
	void setFlightPlan(FlightPlan flightPlan);
	
	void setAutopilotModule(AutopilotModule autopilotModule);
	
	/**
	 * Setup navigation parameters according to given {@link FlightPlan}
	 */
	void autoNavigate();
	
	/* 
	 * Navigator desires. 
	 */	
	Position2D getDesiredPosition();
	void setDesiredPosition(float x,float y);
	
	float getDesiredCourse();
	void setDesiredCourse(float course);
	
	// USE BY FP
	float getDesiredAltitude();
	void setDesiredAltitude(float altitude);
	
	// USED by Course PID
	float getDesiredRoll();
	void setDesiredRoll(float roll);
	
	// USED BY FP
	float getDesiredPitch();
	void setDesiredPitch(float pitch);
	
	// USED BY FP
	float getPreClimb();
	void setPreClimb(float preClimb);
	
	// USED by FP
	boolean isAutoPitch();
	void setAutoPitch(boolean value);

	// USED by FP
	int getDesiredGaz();
	void setDesiredGaz(int gaz);
	
	/**
	 * Returns number of calls of method {@link #autoNavigate()}.
	 *  
	 * @return
	 */
	int getAutoNavigationCycleNumber() ;
	
}
