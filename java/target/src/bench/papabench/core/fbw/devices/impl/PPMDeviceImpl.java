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
package papabench.core.fbw.devices.impl;

import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.data.impl.RadioCommandsImpl;
import papabench.core.fbw.devices.PPMDevice;

/**
 * Device for receiving commands from the ground station.
 * 
 * @author Michal Malohlava
 *
 */
public class PPMDeviceImpl implements PPMDevice {
	
	private RadioCommands lastRadioCommands;

	public RadioCommands getLastRadioCommands() {
		return this.lastRadioCommands;		
	}

	public boolean isLastRadioContainsAvgChannels() {
		return false;
	}

	public void lastRadioFromPpm() {
	}

	public void init() {
		this.lastRadioCommands = new RadioCommandsImpl();
	}

	public void reset() {
	}

	public boolean isValid() {		
		return false;
	}

	public void setValid(boolean value) {				
	}
}
