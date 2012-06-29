/*
  Copyright (C) 2012, Tórur Biskopstø Strøm (torur.strom@gmail.com)

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
package org.reprap;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import com.jopdesign.io.*;

public class IICController extends PeriodicEventHandler
{
	static IICController instance;
	
	ExpansionHeaderFactory EHF = ExpansionHeaderFactory.getExpansionHeaderFactory();
	ExpansionHeader EH = EHF.getExpansionHeader();
	IICFactory fact = IICFactory.getIICFactory();
	IIC iic = fact.getIIC();
	int status = 0; // 0 init 1 write address 2 write data 3 read address 4 read data
	int rdcnt = 0;
	int slave_cnt = 0;
	boolean started = false;
	
	private IICController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(500,0)),
			  new StorageParameters(10, null, 0, 0), 5);
	}
	
	@Override
	public void handleAsyncEvent()
	{
		boolean allok = true;
		
		if(started)
		{
			if((iic.CR_SR & 0x00000002) > 0) // Transfer in progress
			{
				System.out.println("Transfer in progress");
				//allok = false;
			}
			if((iic.CR_SR & 0x00000080) > 0) // No ACK received
			{
				System.out.println("No ACK received");
				//allok = false;
			}
			if((iic.CR_SR & 0x00000020) > 0) // Arbitration lost
			{
				System.out.println("Arbitration lost");
				//allok = false;
			}
		}
		
		System.out.print("state");
		System.out.print(status);
		System.out.print(":");
		System.out.println(iic.TXR_RXR);
		
		switch(status)
		{
			case 0:
				iic.PRERlo = 0x000000FF; // set clk to 183 Hz
				iic.PRERhi = 0x000000FF; // set clk to 183 Hz
				iic.CTR = 0x00000080; // start i2c core
				started = true;
				status = 1;
				break;
			case 1:
				iic.TXR_RXR = 0x00000090; // slave address to transmit register
				iic.CR_SR = 0x00000090; // start and write bit
				status = 2;
				break;
			case 2:
				iic.TXR_RXR = 0x00000007; // data to transmit register
				iic.CR_SR = 0x00000010; // write bit
				status = 3;
				break;
			case 3:
				iic.TXR_RXR = 0x00000091; // slave address+read to transmit register
				iic.CR_SR = 0x00000090; // start and write bit
				status = 4;
				break;
			case 4:
				iic.CR_SR = 0x00000020; // read and ack bit
				System.out.print("Byte:");
				System.out.println(iic.TXR_RXR);
				break;
			default:
				break;
		}
		
		/*System.out.print(iic.TXR_RXR);
		System.out.print(" ");
		System.out.print(iic.CR_DEBUG);
		System.out.print(" ");
		System.out.print(iic.PRERlo);
		System.out.println();*/
	}
}