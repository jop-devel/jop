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
import papabench.core.commons.conf.RadioConf;
import papabench.core.utils.MathUtils;
import papabench.core.utils.PPRZUtils;

/**
 * Computes desired_gaz and desired_pitch from desired_climb.
 *  
 * @author Michal Malohlava
 *
 */
public class ClimbPIDController extends AbstractPIDController implements RadioConf {
	 
	 private float maxPitch = MAX_PITCH;
	 private float minPitch = MIN_PITCH;
	 
	 private float climbPitchPGain = CLIMB_PITCH_PGAIN;
	 private float climbPitchIGain = CLIMB_PITCH_IGAIN;
	 
	 private static final float MAX_PITCH_CLIMB_SUM_ERR = 100f;
	 private float climbPitchSumErr = 0.0f; 
	 
	 private static final float MAX_CLIMB_SUM_ERR = 100f;
	 private float climbSumErr = 0.0f;
	 
	 private float pitchOfVz = 0.f;
	 private float pitchOfVzPGain = CLIMB_PITCH_OF_VZ_PGAIN;
	 
	 private float climbPGain = CLIMB_PGAIN;
	 private float climbIGain = CLIMB_IGAIN;
	 
	 public void control(AutopilotStatus status, Estimator estimator, Navigator navigator) {
		 
		 float err = estimator.getSpeed().z - status.getClimb();
		 
		 if (navigator.isAutoPitch()) { /* gas is constant */
			 // set gas
			 status.setGaz(navigator.getDesiredGaz());
			 
			 // compute pitch
			 float desiredPitch = climbPitchPGain * (err + climbPitchIGain *climbPitchSumErr);
			 desiredPitch = MathUtils.asymmetricalLimiter(desiredPitch, minPitch, maxPitch);			 
			 status.setPitch(desiredPitch);
			 
			 climbPitchSumErr += err;
			 climbPitchSumErr = MathUtils.symmetricalLimiter(climbPitchSumErr, MAX_PITCH_CLIMB_SUM_ERR);			 
		 } else {
			 float fGaz;
			 
			 pitchOfVz = status.getClimb() > 0 ? status.getClimb() * pitchOfVzPGain : 0.0f;
			 fGaz = climbPGain * (err + climbIGain * climbSumErr) + CLIMB_LEVEL_GAZ + CLIMB_GAZ_OF_CLIMB * status.getClimb();
			 
			 climbSumErr += err;
			 climbSumErr = MathUtils.symmetricalLimiter(climbSumErr, MAX_CLIMB_SUM_ERR);
			 
			 status.setGaz((int) PPRZUtils.trimuPPRZ(fGaz * MAX_PPRZ)); 
			 status.setPitch(navigator.getDesiredPitch() + pitchOfVz);
		 }
	 }
}
