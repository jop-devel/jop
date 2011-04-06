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
package papabench.core.autopilot.modules.impl;

import papabench.core.PapaBench;
import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.autopilot.modules.AutopilotModule;
import papabench.core.autopilot.modules.Estimator;
import papabench.core.autopilot.modules.LinkToFBW;
import papabench.core.autopilot.modules.Navigator;
import papabench.core.bus.SPIBus;

/**
 * Autopilot module implementation.
 * 
 * It manages links to contained modules as well as link to top-level module.
 * 
 * @see PapaBench
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed(members=true)
//@SCJAllowed
public class AutopilotModuleImpl extends AutopilotStatusImpl implements AutopilotModule {
	
	/* Module dependencies: */

	/** GPS device obtaining current position */
	private GPSDevice gpsDevice;
	/** IR device returning roll of airplane */
	private IRDevice irDevice;
	/* Communication SPI bus */
	private SPIBus spiBus;
	
	/* Estimator is responsible for holding/computing current position, altitude, speed */
	private Estimator estimator;
	
	/* Navigator module responsible for autopilot navigation according to the flight plan */
	private Navigator navigator;
	
	/* Communication link to FBW */
	private LinkToFBW linkToFBW;
	
	/* Top-level PapaBench module */
	private PapaBench papaBench;
	
	/* ------------------
	 * module iface impl. 
	 * ------------------ */
	public void init() {
		super.init();
				
		if (gpsDevice == null || irDevice == null || spiBus == null || estimator == null || navigator == null || linkToFBW == null || papaBench == null) {
			throw new IllegalArgumentException("Autopilot module has wrong configuration");
		}
		
		gpsDevice.init();
		irDevice.init();
		
		estimator.setGPSDevice(gpsDevice);
		estimator.setIRDevice(irDevice);
		estimator.init();
		
		navigator.setAutopilotModule(this);
		navigator.init();
		
		linkToFBW.setSPIBus(this.spiBus);
		linkToFBW.init();
	}

	/* ---------------------------
	 * autopilot device iface impl. 
	 * --------------------------- */
	
	public void setGPSDevice(GPSDevice gpsDevice) {
		this.gpsDevice = gpsDevice;
	}
	
	public GPSDevice getGPSDevice() {		
		return this.gpsDevice;
	}	
	
	public void setIRDevice(IRDevice irDevice) {
		this.irDevice = irDevice;
	}
	
	public IRDevice getIRDevice() {
		return this.irDevice;		
	}

	public SPIBus getSpiBus() {		
		return this.spiBus;
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;
	}


	/* ---------------------------
	 * autopilot module iface impl.
	 * --------------------------- */
	
	public Estimator getEstimator() {
		return this.estimator;
	}

	public void setEstimator(Estimator estimator) {
		this.estimator = estimator;
	}
	
	public Navigator getNavigator() {		
		return this.navigator;
	}
	
	public void setNavigator(Navigator navigator) {
		this.navigator = navigator;
	}

	public LinkToFBW getLinkToFBW() {
		return this.linkToFBW;
	}

	public void setLinkToFBW(LinkToFBW fbwLink) {
		this.linkToFBW = fbwLink;
	}
	
	public void setPapaBench(PapaBench papaBench) {
		this.papaBench = papaBench;		
	}
	
	public PapaBench getPapaBench() {
		return this.papaBench;
	}
	
	public void missionFinished() {
		this.papaBench.shutdown();
	}

	@Override
	public String toString() {
		/*
		return "AutopilotModuleImpl [estimator=" + estimator + ", gpsDevice="
				+ gpsDevice + ", irDevice=" + irDevice + ", linkToFBW="
				+ linkToFBW + ", navigator=" + navigator + ", spiBus=" + spiBus
				+ "]"; */		
		return super.toString();
	}
	
}
