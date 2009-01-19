/*
This file is part of JOP, the Java Optimized Processor
  see <http://www.jopdesign.com/>

Copyright (C) 2008, Christof Pitter

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

package wcet;

import java.lang.Math;
import java.util.Random;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class BytecodeVerification {

	final static boolean MEASURE = true;
	
	// Shared Variables
	public static int MAX = 10000; // Number of loops
	public static int PRIME = 183;
	public static long SEED = 13;
	
	public static void main(String[] args) {		
		
		// Initialization for measurement 
		int cpu_id;
		
		int[] array = new int[10];
		array[0] = 100;
		
		System.out.println("Bytecode WCET Verification:");

		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		if (cpu_id == 0x00000000)
		{		
			measure(array);
		}
		else
		{
			for(;;);
		}
	}
	
	public static void measure(int [] array) {
		int test = 0;
		
		if (MEASURE){
			int ts = 0;
			int te = 0;
			int to = 0;
			int random = 0;
			int error = 0;
		
			ts = Native.rdMem(Const.IO_CNT);
			te = Native.rdMem(Const.IO_CNT);
			error = te-ts;
			
			// MIN latency in clock cycles
//			ts = Native.rdMem(Const.IO_CNT);
//			for(int j=0; j<0; j++);
//			te = Native.rdMem(Const.IO_CNT);
//			to = te-ts-error;
//			System.out.println("Minimum Latency: " + to + " cycles!");
			
			// MAX latency in clock cycles
//			ts = Native.rdMem(Const.IO_CNT);
//			for(int j=0; j<PRIME-1; j++);
//			te = Native.rdMem(Const.IO_CNT);
//			to = te-ts-error;
//			System.out.println("Maximum Latency: " + to + " cycles!");
		
			// Initialization of random generator
			Random r = new Random(SEED);
		
			for(int i=0; i<MAX; i++){
			
				// Random latency generation
				random =  Math.abs(r.nextInt() % PRIME);
				for(int j=0; j<random; j++);			
			
				ts = Native.rdMem(Const.IO_CNT);
				test = array[0];
				//array[0] = test;
				te = Native.rdMem(Const.IO_CNT);
				to = te-ts-error;
			
				if(to>28)
					System.out.println("Error["+i+"]: " + to + " cycles!");
			}	
		}	
		else{
			test = array[0]; // WCET = 28
			//array[0] = test; // WCET = 47
		}
	}	
}
