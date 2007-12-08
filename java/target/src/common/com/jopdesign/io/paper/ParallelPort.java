package com.jopdesign.io.paper;

public final class ParallelPort extends IODevice {
	
	public volatile int data;
	public volatile int control;
	
	int readValue() {
		return data;
	}
	
	
}
