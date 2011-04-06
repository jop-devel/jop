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
package papabench.jop;

import papabench.core.PapaBench;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.impl.AutopilotModuleImpl;
import papabench.core.autopilot.modules.impl.EstimatorModuleImpl;
import papabench.core.autopilot.modules.impl.LinkToFBWImpl;
import papabench.core.autopilot.modules.impl.NavigatorImpl;
import papabench.core.bus.SPIBusChannel;
import papabench.core.bus.impl.SPIBusChannelImpl;
import papabench.core.fbw.devices.impl.PPMDeviceImpl;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.modules.impl.FBWModuleImpl;
import papabench.core.fbw.modules.impl.LinkToAutopilotImpl;
import papabench.core.simulator.devices.impl.SimulatorGPSDeviceImpl;
import papabench.core.simulator.devices.impl.SimulatorIRDeviceImpl;
import papabench.core.simulator.devices.impl.SimulatorServosControllerImpl;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.model.impl.FlightModelImpl;

/**
 * Factory creating a JOP instance of PapaBench.
 * 
 * The factory does not initialize created modules !
 * 
 * @author Michal Malohlava
 *
 */
public class PapaBenchJopFactory {
	
	public static AutopilotModule createAutopilotModule(PapaBench topLevelModule) {
		
		AutopilotModule autopilotModule = new AutopilotModuleImpl();
		
		autopilotModule.setLinkToFBW(new LinkToFBWImpl());
		autopilotModule.setGPSDevice(new SimulatorGPSDeviceImpl());
		autopilotModule.setIRDevice(new SimulatorIRDeviceImpl());
		autopilotModule.setNavigator(new NavigatorImpl());
		autopilotModule.setEstimator(new EstimatorModuleImpl());
		autopilotModule.setPapaBench(topLevelModule);
		
		return autopilotModule;
	}
	
	public static FBWModule createFBWModule() {
		FBWModule fbwModule = new FBWModuleImpl();
		
		fbwModule.setLinkToAutopilot(new LinkToAutopilotImpl());
		fbwModule.setPPMDevice(new PPMDeviceImpl());
		fbwModule.setServosController(new SimulatorServosControllerImpl());
		
		return fbwModule;		
	}
	
	public static SPIBusChannel createSPIBusChannel() {
		return new SPIBusChannelImpl(); 
	}
	
	public static FlightModel createSimulator() {
		return new FlightModelImpl();
	}
}
