/* $Id: SimulatorFlightModelTaskHandler.java 606 2010-11-02 19:52:33Z parizek $
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
package papabench.core.simulator.tasks.handlers;

import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * Main control task handler of simulator. It periodically updates simulator flight model.
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorFlightModelTaskHandler implements Runnable {
	
	private FlightModel flightModel;
	private AutopilotModule autopilotModule;
	private FBWModule fbwModule;
	
	public SimulatorFlightModelTaskHandler(FlightModel flightModel, AutopilotModule autopilotModule, FBWModule fbwModule) {		
		this.flightModel = flightModel;
		this.autopilotModule = autopilotModule;
		this.fbwModule = fbwModule;
	}
	
	public void run() {
		SimulatedDevice sensors = (SimulatedDevice) fbwModule.getServosController();
		sensors.update(flightModel);
		
		flightModel.updateState();
		
		//LogUtils.log(this, "SIMULATOR - Flight model state:");
		//LogUtils.log(this, flightModel.getState().toString());
	}
}
