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
package papabench.core.fbw.modules.impl;

import papabench.core.bus.SPIBus;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.fbw.modules.LinkToAutopilot;

/**
 * Implementation of module communicating with autopilot unit via SPI bus.
 * 
 * @author Michal Malohlava
 *
 */
public class LinkToAutopilotImpl implements LinkToAutopilot {
	
	private SPIBus spiBus;

	/**
	 * Returns message from autopilot. 
	 * 
	 * Message is allocate in this method call!
	 */
	public InterMCUMsg getMessageFromAutopilot() {
		// allocate new message
		InterMCUMsg msg = getEmptyMessage(); 

		// let the bus fill the message
		spiBus.getMessage(msg);		
		
		return msg; 
	}
	
	protected InterMCUMsg getEmptyMessage() {
		return new InterMCUMsg(true);
	}

	public void sendMessageToAutopilot(InterMCUMsg msg) {
		spiBus.sendMessage(msg);
	}

	public void setSPIBus(SPIBus spiBus) {
		this.spiBus = spiBus;		
	}

	public void init() {
		if (spiBus == null) {
			throw new IllegalArgumentException("Module LinkToAutopilot is not configured.");
		}
	}

}
