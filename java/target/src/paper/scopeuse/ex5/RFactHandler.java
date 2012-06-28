/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package scopeuse.ex5;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;


import com.jopdesign.sys.Memory;

/**
 * 
 * @author jrri
 *
 */

	public class RFactHandler extends PeriodicEventHandler {

		public RFactHandler(PriorityParameters priority,
				PeriodicParameters parameters, StorageParameters scp,
				long scopeSize) {
			super(priority, parameters, scp, scopeSize);

		}

		@Override
		public void handleAsyncEvent() {
			
			System.out.println("*********Handler begin*********");
			
			RunnableFactory factory = new RunnableFactory();
			AuxObj auxObj = new AuxObj();
			
			auxObj.retMem = Memory.getCurrentMemory();
			auxObj.a = 500;
			
			// Currently, entering memory areas generates two "Illegal field reference" 
			//Memory.getCurrentMemory().enterPrivateMemory(512, factory.readTemperature(5, auxObj));
			ManagedMemory.enterPrivateMemory(512, factory.readTemperature(5, auxObj));
			
			System.out.println(auxObj.a);
			System.out.println(auxObj.b);
			System.out.println(auxObj.c);
			
			if(auxObj.arbObj == null){
				System.out.println("null");
			}else{
				System.out.println(auxObj.arbObj.a);
			}
			
			System.out.println("*********Handler exit*********");
		}
	}
	
	// The auxiliary object with references to return area, in/out parameters, in/out
	// objects
	class AuxObj {
		
		// Some return/argument field
		int a,b,c;
		
		// Return memory area
		Memory retMem;
		
		// Return arbitrary object
		ArbObj arbObj = new ArbObj();
		
	}

	class ArbObj {
		
		int a,b,c;
		
	}


//public class Tester extends PeriodicEventHandler{
//	
//	Helper H = new Helper();
//
//	public Tester(PriorityParameters priority, PeriodicParameters parameters,
//			StorageParameters scp, long scopeSize) {
//		super(priority, parameters, scp, scopeSize);
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public void handleAsyncEvent() {
//		// TODO Auto-generated method stub
//		Object O = myMethod();
//		
//	}
//	
//	public Object myMethod(){
//		
//		Object O = null;
//		MyMethod meth = new MyMethod(Memory.getCurrentMemory());
//
//		Memory.getCurrentMemory().enterPrivateMemory(128, meth);
//		
//		return O;
//	}
//
//}
//
//class MyMethod implements Runnable{
//	
//	Memory M;
//	
//	MyMethod(Memory M){
//		
//		this.M = M;
//	}
//
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		System.out.println("yepp");
//		M.executeInArea(logic);
//	}
//	
//}
