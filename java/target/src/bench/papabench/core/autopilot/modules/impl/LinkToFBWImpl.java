/* $Id: LinkToFBWImpl.java 606 2010-11-02 19:52:33Z parizek $
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

import papabench.core.autopilot.modules.LinkToFBW;
import papabench.core.bus.SPIBus;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.utils.LogUtils;

/**
 * A module providing interface to a link to fly-by-wire module.
 *  
 * In this test scenario FBWLinkDevice directly refers FBW module.
 * 
 * Notes:
 *  - direct link is only for testing purposes - it should be replaced by code handling SPI bus
 *   
 * @author Michal Malohlava
 *
 */
//@SCJAllowed
public class LinkToFBWImpl implements LinkToFBW {
	
	private SPIBus spiBus;
	
	public void init() {
		if (spiBus == null) {
			throw new IllegalArgumentException("FBWLink module is not configured correctly!");
		}
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;		
	}

	public InterMCUMsg getMessageFromFBW() {
		InterMCUMsg msg = new InterMCUMsg();
		
		// async receive -if the message is not fully received the message is marked as not-valid
		// The caller is in this case task, so the message will be allocated in the scope of caller task. 
		this.spiBus.getMessage(msg);
		
		return msg;
	}

	public void sendMessageToFBW(InterMCUMsg msg) {
		//LogUtils.log(this, "Sending msg: " + msg);
		this.spiBus.sendMessage(msg);
	}
}
