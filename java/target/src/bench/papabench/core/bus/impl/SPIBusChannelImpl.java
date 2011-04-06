/* $Id: SPIBusChannelImpl.java 606 2010-11-02 19:52:33Z parizek $
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
package papabench.core.bus.impl;

import papabench.core.bus.SPIBus;
import papabench.core.bus.SPIBusChannel;
import papabench.core.commons.data.InterMCUMsg;
import papabench.core.utils.LogUtils;

/**
 * SPI Bus abstraction. Each bus contains two ends - slave and master.
 * Each end is represented by {@link SPIBus} interface. 
 * 
 * The channel handles messages exchange between both ends.
 * 
 * @see SPIBus
 * 
 * @author Michal Malohlava
 *
 */
public class SPIBusChannelImpl implements SPIBusChannel {
	
	private static final int MAX_MSG_IN_POOL = 20;
	
	private SPIBusImpl masterEnd;
	private SPIBusImpl slaveEnd;
	
	private ArrayCyclicQueue messagePool;
	
	public SPIBus getMasterEnd() {
		return this.masterEnd;
	}

	public SPIBus getSlaveEnd() {
		return this.slaveEnd;
	}

	public void init() {
		// create pool
		messagePool = new ArrayCyclicQueue(MAX_MSG_IN_POOL);		
		for (int i = 0; i < MAX_MSG_IN_POOL; i++) {
			messagePool.offer(new InterMCUMsg(true));
		}
		
		this.masterEnd = new SPIBusImpl(this);
		this.slaveEnd = new SPIBusImpl(this);
		
		this.masterEnd.setSPIBusEnd(slaveEnd);
		this.slaveEnd.setSPIBusEnd(masterEnd);
		
		this.masterEnd.init();
		this.slaveEnd.init();
	}

	public void reset() {
		this.masterEnd.reset();
		this.slaveEnd.reset();
	}
	
	protected void freeMessage(InterMCUMsg msg) {
		this.messagePool.offer(msg);		
	}
	
	protected InterMCUMsg getFreeMessage() {
		return (InterMCUMsg) this.messagePool.poll();	
	}
	
	private class SPIBusImpl implements SPIBus {
		
		private SPIBusImpl spiBusEnd;
		private SPIBusChannelImpl spiBusChannel;
		
		private ArrayCyclicQueue msgQueue;;  
		
		SPIBusImpl(SPIBusChannelImpl spiBusChannel) {
			this.spiBusChannel = spiBusChannel;			
		}

		public boolean getMessage(InterMCUMsg msg) {
			if (!msgQueue.isEmpty()
				&& ((InterMCUMsg) msgQueue.peek()).isValid() 
				) {
				InterMCUMsg internalSPIMsg = (InterMCUMsg) msgQueue.poll();
				
				// copy values from internal SPI bus message
				msg.fillFrom(internalSPIMsg);
				
				// put message back to message poll
				freeMessage(internalSPIMsg);							
				
				return true;
				
			} else {
				msg.setValid(false);
				
				return false;
			}
		}

		public void sendMessage(InterMCUMsg msg) {
			// get message from pool
			//LogUtils.log(this, "get free message");
			InterMCUMsg internalSPIMsg = getFreeMessage();
			
			// copy given message to pool message
			//LogUtils.log(this, "fill from");
			internalSPIMsg.fillFrom(msg);
			
			// put message into the other SPI's end queue
			//LogUtils.log(this, "put message into queu");
			putMessageIntoTheOppositeQueue(internalSPIMsg);									
		}

		public void init() {	
			if (spiBusEnd == null || spiBusChannel == null) {
				throw new IllegalArgumentException("SPIBus has wrong configuration!");
			}
			
			this.msgQueue = new ArrayCyclicQueue(MAX_MSG_IN_POOL);
		}

		public void reset() {
		}
		
		private void freeMessage(InterMCUMsg msg) {
			this.spiBusChannel.freeMessage(msg);			
		}
		
		private InterMCUMsg getFreeMessage() {
			return this.spiBusChannel.getFreeMessage();
		}
		
		private void putMessageIntoTheOppositeQueue(InterMCUMsg msg) {
			//LogUtils.log(this, "putting message into queu");
			this.spiBusEnd.msgQueue.offer(msg);						
		}
		
		private void setSPIBusEnd(SPIBusImpl end) {
			this.spiBusEnd = end;
		}
	}
}
