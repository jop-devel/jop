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
package papabench.core.autopilot.modules;

import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.bus.SPIBus;

/**
 * Devices required by an autopilot module.
 * 
 * @author Michal Malohlava
 *
 */
public interface AutopilotDevices {
	
	void setGPSDevice(GPSDevice gpsDevice);
	GPSDevice getGPSDevice();
	
	void setIRDevice(IRDevice irDevice);
	IRDevice getIRDevice();
	
	void setSPIBus(SPIBus spiBus);
	SPIBus getSpiBus();
}
