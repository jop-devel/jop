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
package papabench.core.fbw.devices;

import papabench.core.commons.conf.AirframeParametersConf;
import papabench.core.commons.data.RadioCommands;
import papabench.core.commons.devices.Device;

/**
 * Device for receiving commands from ground controller.
 * 
 * @author Michal Malohlava
 *
 */
public interface PPMDevice extends Device {
	
	/**
	 * Returns array of length equal to {@link AirframeParametersConf#RADIO_CTL_NB}.
	 * 
	 * @return
	 */
	RadioCommands getLastRadioCommands();

	/**
	 * 
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Copy data from ppm buffer to the buffer used by MC0
	 */
	void lastRadioFromPpm();
	
}
