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

import papabench.core.autopilot.conf.AutopilotMode;
import papabench.core.autopilot.conf.LateralFlightMode;
import papabench.core.autopilot.conf.VerticalFlightMode;
import papabench.core.autopilot.modules.AutopilotStatus;
import papabench.core.commons.conf.AirframeParametersConf;

/**
 * A module holding airplane status.
 * 
 * It manages modes, position attitude, engine settings.
 * 
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class AutopilotStatusImpl implements AutopilotStatus {
	
	/** autopilot mode */
	private AutopilotMode autopilotMode;
	/** */
	private LateralFlightMode lateralFlightMode;
	/** */
	private VerticalFlightMode verticalFlightMode;
	
	private float roll;
	private float pitch;
	private float climb;
	private float rollPGain;
	private float pitchPGain;
	private float pitchOfRoll;
	
	
	private int gaz;
	private int aileron;
	private int elevator;
	
	
	private int voltSupply;
	private int MC1PpmCpt;
	
	private boolean launched = false;
	
	public void init() {
		autopilotMode = AutopilotMode.AUTO2;
		lateralFlightMode = LateralFlightMode.MANUAL;
		verticalFlightMode = VerticalFlightMode.MANUAL;
		
		rollPGain = AirframeParametersConf.ROLL_PGAIN;
		pitchPGain = AirframeParametersConf.PITCH_PGAIN;
	}

	public AutopilotMode getAutopilotMode() {
		return autopilotMode;
	}
	public void setAutopilotMode(AutopilotMode mode) {
		this.autopilotMode = mode;		
	}

	public LateralFlightMode getLateralFlightMode() {
		return lateralFlightMode;		
	}
	public void setLateralFlightMode(LateralFlightMode mode) {
		this.lateralFlightMode = mode;		
	}

	public VerticalFlightMode getVerticalFlightMode() {
		return verticalFlightMode;
	}
	public void setVerticalFlightMode(VerticalFlightMode mode) {
		this.verticalFlightMode = mode;
	}
	public float getRoll() {
		return roll;
	}
	public void setRoll(float roll) {
		this.roll = roll;
	}
	public float getPitch() {
		return pitch;
	}
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	public float getClimb() {
		return climb;
	}
	public void setClimb(float climb) {
		this.climb = climb;
	}
	public float getRollPGain() {
		return rollPGain;
	}
	public void setRollPGain(float rollPGain) {
		this.rollPGain = rollPGain;
	}
	public float getPitchPGain() {
		return pitchPGain;
	}
	public void setPitchPGain(float pitchPGain) {
		this.pitchPGain = pitchPGain;
	}
	public float getPitchOfRoll() {
		return pitchOfRoll;
	}
	public void setPitchOfRoll(float pitchOfRoll) {
		this.pitchOfRoll = pitchOfRoll;
	}	
	public int getVoltSupply() {
		return voltSupply;
	}
	public void setVoltSupply(int voltSupply) {
		this.voltSupply = voltSupply;
	}
	public int getMC1PpmCpt() {
		return MC1PpmCpt;
	}
	public void setMC1PpmCpt(int mC1PpmCpt) {
		MC1PpmCpt = mC1PpmCpt;
	}
	public boolean isLaunched() {
		return launched;
	}
	
	public void setLaunched(boolean launch) {
		this.launched = launch;
	}
	/* Airframe parameters */
	public int getAileron() {
		return this.aileron;
	}
	public void setAileron(int aileron) {
		this.aileron = aileron;		
	}
	public void setElevator(int elevator) {
		this.elevator = elevator;		
	}
	public int getElevator() {
		return this.elevator;
	}
	public int getGaz() {
		return gaz;
	}
	public void setGaz(int gaz) {
		this.gaz = gaz;
	}

	@Override
	public String toString() {
		return "AutopilotStatusImpl [aileron=" + aileron + ", autopilotMode="
				+ autopilotMode + ", climb=" + climb + ", elevator=" + elevator
				+ ", gaz=" + gaz + ", lateralFlightMode=" + lateralFlightMode
				+ ", launched=" + launched + ", pitch=" + pitch + ", roll="
				+ roll + ", verticalFlightMode=" + verticalFlightMode + "]";
	}	
}
