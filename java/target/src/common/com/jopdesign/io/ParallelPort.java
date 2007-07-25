package com.jopdesign.io;

public class ParallelPort extends IODevice {
	
	public int data;
	public int control;
	
	int readValue() {
		return data;
	}
	
	
}
