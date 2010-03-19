/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
    
  Copyright (C) 2010, Thomas Hassler, Lukas Marx

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


/**
 * @author Thomas Hassler	e0425918@student.tuwien.ac.at
 * @author Lukas Marx	lukas.marx@gmail.com
 * @version 1.0
 */

package ttpa.protocol;

import com.jopdesign.io.*;

import util.Timer;

public class Transmit
{
	
	public static SerialPort sp;
	
	/**
	 * initialize serial port connection
	 */
	public static void initConnection()
	{
		IOFactory fact = IOFactory.getFactory();
		sp = fact.getSerialPort();
		sp.setParityMode(SerialPort.PARITY_EVEN);
	}
	
	/**
	 * send the given firework byte
	 * 
	 * @param sendValue fw byte to send
	 */
	public static void sendFWByte(byte sendValue)
	{
		sp.setParityMode(SerialPort.PARITY_ODD);
		sp.write(sendValue);
		sp.setParityMode(SerialPort.PARITY_EVEN);
	}
	
	/**
	 * send data byte
	 * 
	 * @param sendValue send the given data byte
	 */
	public static void sendByte(byte sendValue)
	{
		sp.write(sendValue);
	}
	
	/**
	 * receive a fw byte
	 * 
	 * @return received firework byte
	 */
	public static byte recvFWByte()
	{
		sp.setParityMode(SerialPort.PARITY_ODD);
		while ( !sp.rxFull() ) {	
		}
		if (sp.parityError()) {
			return (byte) 0;
		}
		sp.setParityMode(SerialPort.PARITY_EVEN);
		return (byte) sp.read();
	}
	
	/**
	 * receive a data byte
	 * 
	 * @return received data byte
	 */
	public static byte recvByte()
	{
		/* wait until the byte is sent */
		int t = Timer.getTimeoutUs(1205);
		while (!Timer.timeout(t)) {
			;
		}

		if (sp.parityError()) {
			return (byte) 0;
		}

		return (byte) sp.read();
	}

}
