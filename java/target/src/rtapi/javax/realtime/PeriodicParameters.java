/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_1;
import javax.safetycritical.annotate.MemoryAreaEncloses;

@SCJAllowed
public class PeriodicParameters extends ReleaseParameters {

	RelativeTime start;
	RelativeTime period;
	RelativeTime deadline;
	
	AsyncEventHandler handler;

	/**
	 * TODO: is this a legal SCJ constructor? It is not part of the spec source.
	 * 
	 * @param start
	 * @param period
	 */
	public PeriodicParameters(RelativeTime start, RelativeTime period) {
		if(start == null) {
			this.start = new RelativeTime();
		}
		else {
			this.start = start;
		}
		if(period == null) {
			this.period = new RelativeTime();
		}
		else {
			this.period = period;
		}
	}

	/**
	 * Construct a new object within the current memory area.
	 * 
	 * @param start is relative to the start of the mission. A null value
	 *              defaults to an offset of zero milliseconds.
	 * 
	 * @param period is the time between each release of the associated
	 *            	 schedulable object. The default deadline is the same value as
	 *            	 the period. The default miss deadline handler is null.
	 * 
	 * @throws IllegalArgumentException if period is null.
	 */
	@SCJAllowed
	@BlockFree
	public PeriodicParameters(HighResolutionTime start, RelativeTime period) {
		
		if(start == null){
			this.start =  new RelativeTime();
		}else{
			this.start = (RelativeTime) start;
		}
		
		if (period == null){
			throw new IllegalArgumentException();
		}else{
			this.period = period;
		}
		
	}

	/**
	 * Construct a new object within the current memory area.
	 * 
	 * @param start is relative to the start of the mission. A null value
	 *              defaults to an offset of zero milliseconds.
	 * 
	 * @param period is the time between each release of the associated
	 *               schedulable object.
	 * 
	 * @param deadline 	is an offset from the release time by which the
	 *            		release should finish. A null deadline indicates the same
	 *            		value as the period.
	 *            
	 * @param handler is the async event handler to be release if the
	 *            	  associated schedulable object misses its deadline. A null
	 *            	  parameter indicates that no handler should be release.
	 * 
	 * @throws IllegalArgumentException if period is null.
	 */
	@SCJAllowed
	@BlockFree
	public PeriodicParameters(HighResolutionTime start, RelativeTime period,
			RelativeTime deadline, AsyncEventHandler handler) {
		
		
		this.start = (RelativeTime) start;
		this.period = period;
		this.deadline = deadline;
		
		this.handler = handler;

		if(start == null){
			this.start =  new RelativeTime();
		}else{
			this.start = (RelativeTime) start;
		}
		
		if (period == null){
			throw new IllegalArgumentException();
		}else{
			this.period = period;
		}
		
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public HighResolutionTime getStart() {
		return start;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public RelativeTime getPeriod() {
		return period;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public RelativeTime getDeadline() {
		return deadline;
	}

	/**
	 * @return Returns the object originally passed in to the constructor, which
	 *         is known to reside in a memory area that encloses this.
	 */
	@BlockFree
	@SCJAllowed
	public AsyncEventHandler getHandler() {
		return handler;
	}
}
