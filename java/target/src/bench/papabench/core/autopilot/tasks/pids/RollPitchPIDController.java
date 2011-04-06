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
package papabench.core.autopilot.tasks.pids;

import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.utils.PPRZUtils;

/**
 * Computes desired_aileron and desired_elevator from attitude estimation and expected attitude.
 *  
 * @author Michal Malohlava
 *
 */
public class RollPitchPIDController extends AbstractPIDController {

	public void control(AutopilotStatus status, Estimator estimator, Navigator navigator) {
		float err = estimator.getAttitude().phi - status.getRoll();
		
		int desiredAileron = (int) PPRZUtils.trimPPRZ(status.getRollPGain() * err);
		
		if (status.getPitchOfRoll() < 0f) {
			status.setPitchOfRoll(0f);
		}
		
		err = -(estimator.getAttitude().theta - status.getPitch() - status.getPitchOfRoll() * Math.abs(estimator.getAttitude().phi));
		
		int desiredElevator = (int) PPRZUtils.trimPPRZ(status.getPitchPGain() * err);
		
		status.setAileron(desiredAileron);
		status.setElevator(desiredElevator);
	}

}
