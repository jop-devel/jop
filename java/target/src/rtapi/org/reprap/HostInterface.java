package org.reprap;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SerialPort;

public class HostInterface 
{
	private SerialPort SP = IOFactory.getFactory().getSerialPort();
	
	public boolean available()
	{
		return SP.rxFull();
	}
	
	public void write(char character)
	{
		SP.write(character);
	}
	
	public char read()
	{
		return (char)SP.read();
	}
}
