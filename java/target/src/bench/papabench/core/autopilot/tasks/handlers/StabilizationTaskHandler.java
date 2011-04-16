/* $Id: StabilizationTaskHandler.java 606 2010-11-02 19:52:33Z parizek $
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

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.tasks.pids.RollPitchPIDController;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.commons.data.RadioCommands;
import papabench.core.utils.LogUtils;
import papabench.core.utils.PPRZUtils;

/**
 * Task handler responsible for stabilization of airplane according to navigation commands.
 * 
 * f = ? Hz
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class StabilizationTaskHandler implements Runnable {	

	private AutopilotModule autopilotModule;
	
	private RollPitchPIDController pidController;

	public StabilizationTaskHandler(AutopilotModule autopilotModule) {
		this.autopilotModule = autopilotModule;
		this.pidController = new RollPitchPIDController();
	}

	public void run() {

		autopilotModule.getIRDevice().update();
		autopilotModule.getEstimator().updateIRState();
		
		pidController.control(autopilotModule, autopilotModule.getEstimator(), autopilotModule.getNavigator());
		
		InterMCUMsg msg = new InterMCUMsg(true);
		RadioCommands radioCommands = msg.radioCommands;
		
		radioCommands.setPitch(autopilotModule.getElevator());
		radioCommands.setRoll(autopilotModule.getAileron());
		radioCommands.setThrottle(autopilotModule.getGaz());
		radioCommands.setGain1((int) PPRZUtils.trimPPRZ(RadioConf.MAX_PPRZ/0.75f*(autopilotModule.getEstimator().getAttitude().phi)));		
		msg.setValid(true);
		
		//LogUtils.log(this, "Sending msg: " + msg);
		
		autopilotModule.getLinkToFBW().sendMessageToFBW(msg);
	}
}
