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

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.commons.modules.Module;

/**
 * @author Michal Malohlava
 *
 */
public interface AutopilotStatus extends Module {
	
	/* Autopilot modes */
	AutopilotMode getAutopilotMode();
	void setAutopilotMode(AutopilotMode mode);
	
	LateralFlightMode getLateralFlightMode();
	void setLateralFlightMode(LateralFlightMode mode);
	
	VerticalFlightMode getVerticalFlightMode();
	void setVerticalFlightMode(VerticalFlightMode mode);
	
	/* 
	 * Autopilot navigation parameters which are used to manage FBW 
	 */
	float getRoll();
	void setRoll(float roll);
	
	float getPitch();
	void setPitch(float pitch);	
	
	float getClimb();
	void setClimb(float climb);
	
	float getRollPGain();
	void setRollPGain(float value);
	
	float getPitchPGain();
	void setPitchPGain(float value);
	
	float getPitchOfRoll();
	void setPitchOfRoll(float value);
	
	int getVoltSupply();
	void setVoltSupply(int vSupply);
	
	int getMC1PpmCpt();
	void setMC1PpmCpt(int value);
	
	boolean isLaunched();
	void setLaunched(boolean launch);
	
	/* Airframe parameters */
	void setAileron(int aileron);
	int getAileron();
	
	void setElevator(int elevator);
	int getElevator();
	
	int getGaz();
	void setGaz(int gaz);	
}
