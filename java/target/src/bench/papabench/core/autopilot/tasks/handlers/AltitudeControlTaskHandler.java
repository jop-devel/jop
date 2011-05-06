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
package papabench.core.autopilot.tasks.handlers;

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.AltitudePIDController;
import papabench.core.autopilot.tasks.pids.PIDController;


/**
 * Altitude control task handler.
 * 
 * f = ? Hz
 * 
 * @author Michal Malohlava
 *
 * @do not edit !
 */
public class AltitudeControlTaskHandler implements Runnable {
	
	// instantiated in mission memory to preserve PID specific attributes
	private AutopilotModule autopilotModule;
	
	// PID controller for altitude - it cannot be only a static method because it can 
	// have inner state (e.g., last error value)
	private AltitudePIDController pidController; 

	public AltitudeControlTaskHandler(AutopilotModule autopilotModule) {		
		this.autopilotModule = autopilotModule;
		this.pidController = new AltitudePIDController();
	}
	
	public void run() {
		if (autopilotModule.getAutopilotMode() == AutopilotMode.AUTO2
				|| autopilotModule.getAutopilotMode() == AutopilotMode.HOME) {
				
				if (autopilotModule.getVerticalFlightMode() == VerticalFlightMode.AUTO_ALTITUDE) {
					pidController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());								
				}
		}		
	}
}
