package com.jopdesign.io;

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
	 * SW interrupt
	 */
	public volatile int swInterrupt;
	/**
	 * Watchdog
	 */
	public volatile int wd;
	// some more...
}