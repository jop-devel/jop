package com.jopdesign.io;

public class SerialPort extends IODevice {
	
	// Some constants for the status port
	public static final int MASK_TDRE = 1;
	public static final int MASK_RDRF = 2;

	public volatile int status;
	public volatile int data;
	

	/**
	 * Non-blocking read of a character. If the input buffer
	 * is empty, the last value is returned.
	 * @return
	 */
	public int read() {
		return data;
	}
	
	/**
	 * Non-blocking write of a character. If the transmit buffer
	 * is full, the character is lost.
	 * @param ch
	 */
	public void write(int ch) {
		data = ch;
	}
	/**
	 * A character is available in the input buffer.
	 * @return
	 */
	public boolean rxFull() {
		return (status & MASK_RDRF) != 0;
	}
	
	/**
	 * The transmit buffer is empty.
	 * @return
	 */
	public boolean txEmpty() {
		return (status & MASK_TDRE) != 0;
	}
	
	
}
