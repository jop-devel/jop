package com.jopdesign.io;

public class SerialPort extends IODevice {
	
	public int data;
	public int control;
	
	int readValue() {
		return data;
	}
	
	
}
