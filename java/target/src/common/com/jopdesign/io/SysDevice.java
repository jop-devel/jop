package com.jopdesign.io;

/**
 * Representation of the system device (sc_sy.vhd)
 * @author martin
 *
 */

public final class SysDevice extends IODevice {
	
	/**
	 * counter and interrupt
	 */
	public volatile int cntInt;
	
	/**
	 * us counter and timer
	 */
	public volatile int uscntTimer;
	
	/**
	 * SW interrupt on write
	 * Interrupt number on read
	 */
	public volatile int swInterrupt;
	
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
}