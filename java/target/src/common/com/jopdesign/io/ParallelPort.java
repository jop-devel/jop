package com.jopdesign.io;

public final class ParallelPort extends IODevice {
	
	public volatile int data;
	public volatile int control;
	
	int readValue() {
		return data;
	}
	
	
}
