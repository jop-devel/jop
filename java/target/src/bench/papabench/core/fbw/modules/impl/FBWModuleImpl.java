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
package papabench.core.fbw.modules.impl;

import papabench.core.bus.SPIBus;
import papabench.core.commons.conf.FBWMode;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.devices.ServosController;
import papabench.core.fbw.modules.FBWModule;
import papabench.core.fbw.modules.LinkToAutopilot;

/**
 * Fly-by-wire module implementation.
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class FBWModuleImpl extends FBWStatusImpl implements FBWModule {
	
	private LinkToAutopilot linkToAutopilot;
	
	private PPMDevice ppmDevice;
	private ServosController servosController;
	private SPIBus spiBus;

	public void init() {
		if (ppmDevice == null || servosController == null || linkToAutopilot == null || spiBus == null) {
			throw new IllegalArgumentException("Module FBWModule has wrong configuration");
		}
		
		// FIXME this is hard-written switch to AUTO mode
		setFBWMode(FBWMode.AUTO);
		linkToAutopilot.setSPIBus(this.spiBus);
		linkToAutopilot.init();
		
		ppmDevice.init();
		servosController.init();				
	}

	public LinkToAutopilot getLinkToAutopilot() {
		return this.linkToAutopilot;
	}

	public PPMDevice getPPMDevice() {
		return this.ppmDevice;
	}

	public SPIBus getSPIBus() {
		return this.spiBus;
	}

	public ServosController getServosController() {
		return this.servosController;
	}

	public void setPPMDevice(PPMDevice ppmDevice) {
		this.ppmDevice = ppmDevice;
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;
	}

	public void setServosController(ServosController servosController) {
		this.servosController = servosController;
	}

	public void setLinkToAutopilot(LinkToAutopilot linkToAutopilot) {
		this.linkToAutopilot = linkToAutopilot;		
	}
}
