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
package papabench.core.fbw.tasks.handlers;

import papabench.core.commons.conf.FBWMode;
import papabench.core.commons.conf.RadioConf;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.fbw.modules.FBWModule;

/**
 * Task handler checking accessibility of autopilot unit. 
 * If it receives data from autopilot unit it configures servos according to given values.
 *  
 * T = 50ms
 * 
 * @author Michal Malohlava
 *
 */
public class CheckMega128ValuesTaskHandler implements Runnable {
	
	private FBWModule fbwModule;
	
	private int counterSinceLastMega128 = 0;
	
	public CheckMega128ValuesTaskHandler(FBWModule fbwModule) {
		this.fbwModule = fbwModule;
	}
	
	public void run() {
		// there should be condition reflecting SPI state on real hardware
		InterMCUMsg msg = fbwModule.getLinkToAutopilot().getMessageFromAutopilot();
		// message if message is fully received (SPI reception takes a time, however we return message 
		// which is preallocated for the given SPI reception)
		if (msg.isValid()) {
			counterSinceLastMega128 = 0;
			fbwModule.setMega128OK(true);
			if (fbwModule.getFBWMode() == FBWMode.AUTO) {
				this.fbwModule.getServosController().setServos(msg.radioCommands);
			}
		}
		
		if (counterSinceLastMega128 > RadioConf.STALLED_TIME) {
			fbwModule.setMega128OK(false);
		}
	}

}
