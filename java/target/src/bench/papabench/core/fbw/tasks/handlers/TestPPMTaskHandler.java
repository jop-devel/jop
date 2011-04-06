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
import papabench.core.commons.data.RadioCommands;
import papabench.core.fbw.devices.PPMDevice;
import papabench.core.fbw.modules.FBWModule;

/**
 * Task receiving commands from ground station and checking its availability.
 * 
 * T = 25ms
 *  
 * @author Michal Malohlava
 * 
 */
//@SCJAllowed
public class TestPPMTaskHandler implements Runnable {
	private FBWModule fbwModule;
	
	private int counterFromLastPPM;
	
	public TestPPMTaskHandler(FBWModule fbwModule) {		
		this.fbwModule = fbwModule;
	}
	
	public void run() {
		PPMDevice ppmDevice = fbwModule.getPPMDevice();
		
		if (ppmDevice.isValid()) {
			fbwModule.setRadioOK(true);
			fbwModule.setRadioReallyLost(false);
			counterFromLastPPM++;
			
			ppmDevice.lastRadioFromPpm();
			
			RadioCommands radioCommands = ppmDevice.getLastRadioCommands();
			FBWMode mode = this.fbwModule.getFBWMode();
			if (radioCommands.containsAveragedChannels()) {
				mode = radioCommands.getMode();
			}
			
			if (mode == FBWMode.MANUAL) {
				this.fbwModule.getServosController().setServos(radioCommands);
			}
		} else if (fbwModule.getFBWMode() == FBWMode.MANUAL && fbwModule.isRadioReallyLost()) {
			fbwModule.setFBWMode(FBWMode.AUTO);
		}
		
		if (counterFromLastPPM > RadioConf.STALLED_TIME) {
				fbwModule.setRadioOK(false);
		}
		if (counterFromLastPPM > RadioConf.REALLY_STALLED_TIME) {
			fbwModule.setRadioReallyLost(true);
		}
	}
}
