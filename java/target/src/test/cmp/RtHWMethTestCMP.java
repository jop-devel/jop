/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2008, Jack Whitham

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
 * 
 */
package cmp;

import java.util.Vector;
import java.util.Random;

import joprt.RtThread;

import test.test_cp;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;


/**
 * A real-time threaded CMP version of HWMethTest
 * 
 * @author jwhitham
 *
 */
public class RtHWMethTestCMP extends RtThread {
	
	public RtHWMethTestCMP(int prio, int us, int mn) {
		super(prio, us);
        method_number = mn;
	}

    int method_number;
	
	public static boolean failure;
	public static int[] test_count;
	public static String error;
	
	final static int NR_METHODS = 12;
	final static int MIN_PERIOD = 5000; // microseconds
	final static int MAX_PERIOD = 10000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
        failure = false;
        test_count = new int[ NR_METHODS + 1 ];
        error = "no error";

		System.out.println("Beginning test...");
        Random rng = new Random();
        int i;
	
		SysDevice sys = IOFactory.getFactory().getSysDevice();

        // the test threads are created here
		for (i=1; i<=NR_METHODS; ++i) {
            int max = MAX_PERIOD - MIN_PERIOD;
            int period = rng.nextInt() % max;
           
            if ( period < 0 ) period = - period;
            period += MIN_PERIOD;

			RtHWMethTestCMP th = new RtHWMethTestCMP(
                            1 + (( i - 1 ) / sys.nrCpu ), 
                            period, i);
			th.setProcessor(( i - 1 ) % sys.nrCpu );
		}
		
        // the reporter thread (highest priority) is created here
        RtHWMethTestCMP reporter = new RtHWMethTestCMP(NR_METHODS, 500000, 0);
        reporter.setProcessor(0);

        // then the main() method just starts the mission
		System.out.println("Start mission");
		RtThread.startMission();
		System.out.println("Mission started");
    }



	public void run() {
        
        if (method_number == 0) {
            /* reporter */

            System.out.println("Reporter active");
            int i;
            for (;;) {
                for (i=1; i<=NR_METHODS; i++) {
                    System.out.print(" ");
                    System.out.print(test_count[ i ]);
                }
                System.out.println();
                if (failure) {
                    System.out.println("Mission failed :(");
                    System.out.println(error);
                    return;
                }
                waitForNextPeriod();
            }
        }
		
        test_cp     cp = new test_cp () ;
        Random      rng = new Random(method_number);
        int[]       memory = new int [ 4 ];
        int         din, address, dout, write, val, iteration, t;

        // Check hardware and software representations of registers.
        // There are four registers per hardware method, and all of them
        // are initially zero. As each test method runs, the registers
        // become filled with data. 
        iteration = 0;
        while (!failure) {
            val = rng.nextInt();
            write = val & 1;
            address = (val >> 1) & 3;
            din = rng.nextInt();
            if ( 0 != ( val & 0x8000 )) {
                t = rng.nextInt() & 0x7fffff;
            } else {
                t = 0;
            }

            switch (method_number) {
                case 1 :    dout = cp.test01(address, din, write, t); break;
                case 2 :    dout = cp.test02(address, din, write, t); break;
                case 3 :    dout = cp.test03(address, din, write, t); break;
                case 4 :    dout = cp.test04(address, din, write, t); break;
                case 5 :    dout = cp.test05(address, din, write, t); break;
                case 6 :    dout = cp.test06(address, din, write, t); break;
                case 7 :    dout = cp.test07(address, din, write, t); break;
                case 8 :    dout = cp.test08(address, din, write, t); break;
                case 9 :    dout = cp.test09(address, din, write, t); break;
                case 10 :   dout = cp.test10(address, din, write, t); break;
                case 11 :   dout = cp.test11(address, din, write, t); break;
                case 12 :   dout = cp.test12(address, din, write, t); break;
                default :   error = "Outside case?"; failure = true; return;
            }
            if (dout != memory[ address ]) { 
                error = ( "dout/memory mismatch " + 
                        Integer.toString(dout) + " versus " +
                        Integer.toString(memory[ address ]) + " for HW meth " +
                        Integer.toString(method_number) + " and address " +
                        Integer.toString(address) + " with write " +
                        Integer.toString(write) + " on iteration " +
                        Integer.toString(iteration) );
                failure = true;
                return;
            }
            if (write != 0) {
                memory[ address ] = din;
                test_count[ method_number ] ++;
            }
            iteration++;
            waitForNextPeriod();
		}
	}

}
