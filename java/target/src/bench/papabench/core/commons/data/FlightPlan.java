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
package papabench.core.commons.data;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;


/**
 * @author Michal Malohlava
 *
 */
public interface FlightPlan {
	
	/**
	 * Initialize the flight plan.
	 * 
	 * Allocate desired memory (preferably in mission memory).
	 */
	void init();

	String getName();	
	
	void execute();
	
	void setEstimator(Estimator estimator);
	void setAutopilotStatus(AutopilotStatus status);
	void setNavigator(Navigator navigator);
	void setAutopilotModule(AutopilotModule autopilotModule);
	
	float getGroundAltitude();
	
	float getSecureAltitude();
	
}
