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
package papabench.core.autopilot.devices.impl;

import papabench.core.autopilot.devices.IRDevice;
import papabench.core.commons.conf.IRConf;

/**
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class IRDeviceImpl implements IRConf, IRDevice {
	
	protected int irRoll; /* average roll */
	protected int irPitch; /* average pitch */
	
	protected int irContrast = DEFAULT_IR_CONTRAST;
	protected int irRollNeutral = DEFAULT_IR_ROLL_NEUTRAL;
	protected int irPitchNeutral = DEFAULT_IR_PITCH_NEUTRAL;
	
	protected float irRadOfIr = IR_RAD_OF_IR_CONTRAST / DEFAULT_IR_CONTRAST;
	
	protected int simulIrRoll;
	protected int simulIrPitch;

	public void update() {
		irRoll = simulIrRoll - irRollNeutral;
		irPitch = simulIrPitch - irPitchNeutral;
	}
	
	public void calibrate() {
		/* plane nose down -> negative value */
		irContrast = - irPitch;		
		irRadOfIr = IR_RAD_OF_IR_CONTRAST / irContrast;
	}

	public void init() {
		irRadOfIr = IR_RAD_OF_IR_CONTRAST / DEFAULT_IR_CONTRAST;
		// setup ADC channels conversion - however there are not used in simulation - SKIPED - see infrared.c
	}
	
	public void reset() {	
	}

	public int getIrRoll() {
		return irRoll;
	}

	public int getIrPitch() {
		return irPitch;
	}
	
	public int getIrTop() {		
		return 0;
	}

	public int getIrContrast() {
		return irContrast;
	}

	public int getIrRollNeutral() {
		return irRollNeutral;
	}

	public int getIrPitchNeutral() {
		return irPitchNeutral;
	}

	public float getIrRadOfIr() {
		return irRadOfIr;
	}

	public void setSimulIrRoll(int simulIrRoll) {
		this.simulIrRoll = simulIrRoll;
	}

	public void setSimulIrPitch(int simulIrPitch) {
		this.simulIrPitch = simulIrPitch;
	}
	
}
