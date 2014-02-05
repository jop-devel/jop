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

import java.io.EOFException;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;

import com.jopdesign.io.*;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class TestCAM extends PeriodicEventHandler
{
	private HostController hostController;
	private int id;
	
	TestCAM(HostController hostController, int cpuid)
	{
		super(new PriorityParameters(2),
			  new PeriodicParameters(null, new RelativeTime(200,0)),
//			  new StorageParameters(35, new long[]{35},0,0), 0);
			  new StorageParameters(200, null,0,0), 200);
		this.hostController = hostController;
		this.thread.setProcessor(cpuid);
		id = cpuid;
		
	}
	
	@Override
	public void handleAsyncEvent()
	{
			//synchronized (hostController) {
				hostController.print(HostController.intToChar(id));
				hostController.print(HostController.intToChar(id));
				hostController.print(HostController.intToChar(id));
				hostController.print(HostController.intToChar(id));
				char[] test = new char[4];
				//test[6] = 'a';
				
			//}
		
		
	}
}
