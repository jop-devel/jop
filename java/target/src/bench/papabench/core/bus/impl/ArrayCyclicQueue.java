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
package papabench.core.bus.impl;

/**
 * Simple cyclic queue.
 * 
 * @author Michal Malohlava
 *
 */
public class ArrayCyclicQueue /* implements Queue */ {
	
	private Object[] queue;
	// the head of queue
	private int queueStart;
	// the last item of queue
	private int queueEnd;
	private int queueSize;
	
	private int maxSize;
	
	public ArrayCyclicQueue(int maxSize) {
		this.maxSize = maxSize;
		this.queue = new Object[maxSize];
		this.queueEnd = 0; // tail - first empty slot
		this.queueStart = -1; // first element
		this.queueSize = 0;
	}
	
	public synchronized void offer(Object o) {
		this.queue[this.queueEnd] = o;		
		this.queueSize++;
		if (this.queueSize == 1) {
			this.queueStart = this.queueEnd;
		}
		this.queueEnd++;
		if (this.queueEnd == this.maxSize)
			this.queueEnd = 0;
	}
	
	public synchronized Object poll() {
		if (this.queueSize == 0) {
			return null;
		} else {
			Object o = this.queue[this.queueStart];
			this.queue[this.queueStart] = null;
			this.queueStart++;
			if (this.queueStart == this.maxSize)
				this.queueStart = 0;
			this.queueSize--;
			
			return o;
		}
	}
	
	public synchronized Object peek() {
		if (this.queueSize == 0) {
			return null;
		} else {
			return this.queue[this.queueStart];
		}
	}
	
	public synchronized boolean isEmpty() {
		return this.queueSize == 0;
	}
}
