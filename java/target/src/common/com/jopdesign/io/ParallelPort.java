package com.jopdesign.io;

/**
 * Not used - just the paper example
 * @author martin
 *
 */

public final class ParallelPort extends IODevice {
	
	public volatile int data;
	public volatile int control;
	
	int readValue() {
		return data;
	}
	
	
}
