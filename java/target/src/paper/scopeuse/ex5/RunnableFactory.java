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

import javax.realtime.AbsoluteTime;
import javax.realtime.RealtimeClock;

/**
 * 
 * @author jrri
 *
 */

// Where could be useful to instantiate this factory? Immortal/Mission 
// memory so all handlers have access to it? Synchronization issues?.
// If in Mission we need to pass a reference to it to the handler(s)
public class RunnableFactory implements IRunnable{
	
	// Each of the methods in this class implements the 
	// functionalities of one of the application's methods.
	@Override
	public Runnable readTemperature(int i, AuxObj auxObjIn) {
		
		class runner implements Runnable{
			
			int i;
			AuxObj auxObjIn;
			
			runner(int i, AuxObj auxObjIn){
				this.i = i;
				this.auxObjIn = auxObjIn;
			}

			@Override
			public void run() {
				
				// Do work...
				System.out.println("Read Temp! "+i);
				log();
				
				// Overwrite primitives OK...
				
				System.out.println("Aux obj A: "+ auxObjIn.a);
				auxObjIn.a = 100;
				auxObjIn.b = 2;
				auxObjIn.c = 3;
				
				auxObjIn.a = 300;
				
				// Illegal
//				ArbObj P = new ArbObj();
//				P.a = 50;
//				auxObjIn.arbObj = P;
//				
				// Change execution context... another runnable... 
				auxObjIn.retMem.executeInArea(new Runnable() {
					
					@Override
					public void run() {
						ArbObj resArbObj = new ArbObj();
						resArbObj.a = 50;
						auxObjIn.arbObj = resArbObj;
					}
				});
			}
		}
		
		return new runner(i, auxObjIn);
		
// ** Alternative version, that needs arguments to be final **
//		return new Runnable() {
//			
//			int ii = i;
//			int h = i+1;
//			
//			@Override
//			public void run() {
//				
//				// Do work...
//				System.out.println("Read Temp! "+ii);
//				log();
//				
//				// Overwrite primitives OK, they are passed
//				// by value.
//				System.out.println("Aux obj A: "+ auxObjIn.a);
//				auxObjIn.a = 100;
//				auxObjIn.b = 2;
//				auxObjIn.c = 3;
//				
//				auxObjIn.a = 300;
//				
//				// Illegal
////				Pobj P = new Pobj();
////				P.a = 50;
////				O.O = P;
////				
//				// Change execution context... another runnable... 
//				auxObjIn.retMem.executeInArea(new Runnable() {
//					
//					@Override
//					public void run() {
//						ArbObj resArbObj = new ArbObj();
//						resArbObj.a = 50;
//						auxObjIn.arbObj = resArbObj;
//					}
//				});
//			}
//		};
	}

	@Override
	public Runnable setTemperature() {
		// TODO Auto-generated method stub
		return new Runnable() {
			
			@Override
			public void run() {
				
				System.out.println("Set Temp!");
				log();
			}
		};
	}

	// A method common to all application methods
	public void log(){
		
		AbsoluteTime time = RealtimeClock.getRealtimeClock().getTime();
		System.out.println("Time: "+time.getMilliseconds()+" : "+time.getNanoseconds());
		
	}

}
