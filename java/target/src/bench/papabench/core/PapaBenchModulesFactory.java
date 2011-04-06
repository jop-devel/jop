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
package papabench.core;

import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.LinkToFBW;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.autopilot.modules.impl.AutopilotModuleImpl;
import papabench.core.autopilot.modules.impl.EstimatorModuleImpl;
import papabench.core.autopilot.modules.impl.LinkToFBWImpl;
import papabench.core.autopilot.modules.impl.NavigatorImpl;
import papabench.core.bus.SPIBusChannel;
import papabench.core.bus.impl.SPIBusChannelImpl;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.devices.ServosController;
import papabench.core.fbw.devices.impl.PPMDeviceImpl;
import papabench.core.fbw.devices.impl.ServosControllerImpl;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.modules.LinkToAutopilot;
import papabench.core.fbw.modules.impl.FBWModuleImpl;
import papabench.core.fbw.modules.impl.LinkToAutopilotImpl;
import papabench.core.simulator.devices.impl.SimulatorGPSDeviceImpl;
import papabench.core.simulator.devices.impl.SimulatorIRDeviceImpl;
import papabench.core.simulator.model.FlightModel;
import papabench.core.simulator.model.impl.FlightModelImpl;

/**
 * Factory producing instance PapaBench modules.
 * 
 * The modules are not configured nor initialized.
 * 
 * TODO generates this based on AADL design.
 *
 * @author Michal Malohlava
 *
 */
public class PapaBenchModulesFactory {
	
	/*
	 * Autopilot subsystem modules.
	 */
		
	public AutopilotModule getAutopilotModule() {
		return new AutopilotModuleImpl();
	}	
	
	public Navigator getNavigator() {
		return new NavigatorImpl();		
	}
	
	public Estimator getEstimator() {
		return new EstimatorModuleImpl();
	}
	
	public LinkToFBW getLinkToFBW() {
		return new LinkToFBWImpl();		
	}
	
	public GPSDevice getGPSDevice() {
		return new SimulatorGPSDeviceImpl();
	}
	
	public IRDevice getIRDevice() {
		return new SimulatorIRDeviceImpl();
	}
	
	/* 
	 * Fly-by-wire subsystem modules. 
	 */
	
	public FBWModule getFBWModule() {
		return new FBWModuleImpl();		
	}
	
	public LinkToAutopilot getLinkToAutopilot() {
		return new LinkToAutopilotImpl();
	}
	
	public PPMDevice getPPMDevice() {
		return new PPMDeviceImpl();
	}
	
	public ServosController getServosController() {
		return new ServosControllerImpl();
	}	
	
	/* 
	 * SPI bus channel.
	 */
	
	public SPIBusChannel getSPIBusChannel() {
		return new SPIBusChannelImpl();
	}
	
	/* 
	 * Environment simulator.
	 */
	
	public FlightModel getFlightModel() {
		return new FlightModelImpl();
	}
}
