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

import papabench.core.autopilot.data.Attitude;
import papabench.core.autopilot.data.HorizSpeed;
import papabench.core.autopilot.data.Position3D;
import papabench.core.autopilot.devices.GPSDevice;
import papabench.core.autopilot.devices.IRDevice;
import papabench.core.commons.modules.Module;

/**
 * Estimator access interface.
 * 
 * @author Michal Malohlava
 *
 */
public interface Estimator extends Module /*RequireGPSDevice, RequireIRDevice */ {
	
	void setGPSDevice(GPSDevice gpsDevice);
	
	void setIRDevice(IRDevice irDevice);
	
	Position3D getPosition();
	
	Attitude getAttitude();
	
	/**
	 * Returns horizontal speed in module(m/s) and direction (rad)
	 *  
	 * @return horizontal speed
	 */
	HorizSpeed getHorizontalSpeed();
	
	Position3D getSpeed(); // -> estimator_z_dot
	
	int getFlightTime();
	void setFlightTime(int time);
	
	void updatePosition(); // -> estimator_update_state_gps
	
	void updateIRState(); // ->  estimator_update_state_infrared
	
	void updateIR(); // -> estimator_update_ir_estim
	
	void updateFlightTime();
}
