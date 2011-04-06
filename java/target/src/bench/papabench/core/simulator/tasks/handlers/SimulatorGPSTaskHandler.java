/* $Id: SimulatorGPSTaskHandler.java 606 2010-11-02 19:52:33Z parizek $
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

import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.simulator.devices.SimulatedDevice;
import papabench.core.simulator.devices.impl.SimulatorGPSDeviceImpl;
import papabench.core.simulator.model.FlightModel;
import papabench.core.utils.LogUtils;

/**
 * Task handler updating simulated GPS device.
 * 
 *  @see SimulatorGPSDeviceImpl
 * 
 * @author Michal Malohlava
 *
 */
public class SimulatorGPSTaskHandler implements Runnable {
	private FlightModel flightModel;
	private SimulatedDevice gpsDevice;
	
	public SimulatorGPSTaskHandler(FlightModel flightModel, AutopilotModule autopilotModule) {		
		this.flightModel = flightModel;
		this.gpsDevice = (SimulatedDevice) autopilotModule.getGPSDevice();
	}
	
	public void run() {
		Position3D pos = flightModel.getState().getPosition();
		
		//LogUtils.log(this, "Position: " + pos.x + "," + pos.y + "," + pos.z + "               " + flightModel.getState().getAirSpeed());		
		
		gpsDevice.update(flightModel);
	}
}
