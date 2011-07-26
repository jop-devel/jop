/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.io;

/**
 * Representation of the system device (sc_sy.vhd)
 * @author martin
 *
 */

public final class SysDevice extends HardwareObject {
	
	/**
	 * counter and interrupt
	 */
	public volatile int cntInt;
	
	/**
	 * us counter and timer
	 */
	public volatile int uscntTimer;
	
	/**
	 * Interrupt number on read
	 * SW interrupt on write
	 * 
	 */
	public volatile int intNr;
	
	/**
	 * Watchdog
	 */
	public volatile int wd;
	
	/**
	 * Exception register
	 */
	public volatile int exception;
	
	/**
	 * Global lock
	 */
	public volatile int lock;
	
	/**
	 * Processor number
	 */
	public volatile int cpuId;
	
	/**
	 * CMP sync???
	 */
	public volatile int signal;
	
	/**
	 * Interrupt mask for individual interrupts
	 * a write only register
	 */
	public volatile int intMask;
	
	/**
	 * Clear all pending interrupts
	 * a write only register
	 */
	public volatile int clearInt;
	
	/**
	 * Deadline port (read is unused ram access counter)
	 */
	public volatile int deadLine;
	
	/**
	 * Number of CPUs
	 */
	public volatile int nrCpu;
	
	/**
	 * Performance counters.
	 * -1 resets all counter
	 * Currently only used in JopSim for data access and cache statistics.
	 */
	public volatile int perfCounter;
}