/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
 * Created on 01.09.2005
 *
 */
package dsp;

import joprt.RtThread;

import com.jopdesign.sys.*;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */

public class AC97 {

	public static final int CSR = 0x0/4;	// Main Configuration/Status Register
	public static final int OCC0 = 0x4/4;	// RO Output Channel Configuration Register 0
	public static final int OCC1 = 0x8/4;	// Output Channel Configuration Register 1
	public static final int ICC = 0xc/4;	// Input Channel Configuration Register
	public static final int CRAC = 0x10/4;	// Codec Register Access Command
	public static final int INTM = 0x14/4;	// Interrupt Mask
	public static final int INTS = 0x18/4;	// Interrupt Status Register
	public static final int RES = 0x1c;
	public static final int OCH0 = 0x20/4;	// Output Channel 0
	public static final int OCH1 = 0x24/4;	// Output Channel 1
	public static final int OCH2 = 0x28/4;	// Output Channel 2
	public static final int OCH3 = 0x2c/4;	// Output Channel 3
	public static final int OCH4 = 0x30/4;	// Output Channel 4
	public static final int OCH5 = 0x34/4;	// Output Channel 5
	public static final int ICH0 = 0x38/4;	// Input Channel 0
	public static final int ICH1 = 0x3c/4;	// Input Channel 1
	public static final int ICH2 = 0x40/4;	// Input Channel 2
	
	public static final int MSK_RD_DONE = 0x01;
	public static final int MSK_WR_DONE = 0x02;
	
	public static int rd(int wb_reg) {
		
		return Native.rdMem(Const.WB_AC97+wb_reg);
	}

	public static void wr(int wb_reg, int val) {
		
		Native.wrMem(val, Const.WB_AC97+wb_reg);
	}

	/**
	 * Codec register read
	 * @param reg
	 * @return
	 */
	public static int codecRd(int reg) {
		
		wr(CRAC, 0x80000000+(reg<<16));
		while((rd(INTS) & MSK_RD_DONE)==0) {
			;	// busy wait till value read
		}
		return rd(CRAC)&0xffff;
	}

	/**
	 * Codec register read
	 * @param reg
	 * @param val
	 */
	public static void codecWr(int reg, int val) {
		
		wr(CRAC, (reg<<16)+val);
		while((rd(INTS) & MSK_WR_DONE)==0) {
			;	// busy wait till write done
		}
	}

	
	
	
	public static void init() {

		//
		//	check status register and force a reset
		//
		System.out.println("CSR="+rd(CSR));
		wr(CSR, 1);			// cold reset the AC97
		System.out.println("CSR="+rd(CSR));
		RtThread.sleepMs(10);
		System.out.println("CSR="+rd(CSR));
		wr(CSR, 2);			// resume the AC97
		RtThread.sleepMs(10);
		System.out.println("CSR="+rd(CSR));
		
		//
		//	enable stereo in and out in the AC97 interface
		//
		System.out.println("OCC0="+rd(OCC0));
		wr(OCC0, 0x0101);	// enable front left and right output
//		wr(OCC0, 0x0909);	// enable front left and right output
		System.out.println("OCC0="+rd(OCC0));	
		wr(ICC, 0x2121);	// enable left and right input
//		wr(ICC, 0x0909);	// enable left and right input
		System.out.println("ICC="+rd(ICC));

		//
		//	read some vendor information
		//
		System.out.println("VENDOR ID="+codecRd(0x7c));
		System.out.println("VENDOR version="+codecRd(0x7e));
		
		//
		//	do the initilization
		//
		System.out.println("MASTER VOLUME="+codecRd(0x02));
		codecWr(0x02, 0x0000); // unmute master volume
		System.out.println("MASTER VOLUME="+codecRd(0x02));
		
//		codecWr(0x10, 0x0808); // enable line input
		codecWr(0x18, 0x0808); // PCM-out volume
		codecWr(0x1a, 0x0404); // select line record
		codecWr(0x1c, 0x0000); // record gain
//		codecWr(0x20, 0x0000); // local loopback
//		codecWr(0x64, 0x0000); // mixer adc, input gain
	}
	
	public static void run() {
		
		int left, right, i, j, v1, v2;
		
		for (i=0; i<10; ++i) {
			// flush input FIFOs
			rd(ICH0);
			rd(ICH1);
			rd(INTS);
		}
		
		i = 0;
		
		for (;;) {
			// busy wait for input samples
			for (;;) {
				int status =Native.rdMem(Const.WB_AC97+INTS);
				if ((status&0x2d00000)!=0) break;
			}
			left = Native.rdMem(Const.WB_AC97+ICH0);
			right = Native.rdMem(Const.WB_AC97+ICH1);
//			if ((Native.rdMem(Const.WB_AC97+INTS)&0x1200000)!=0) {
//				continue;
//			}
			

			Native.wrMem(left, Const.WB_AC97+OCH0);
			Native.wrMem(right, Const.WB_AC97+OCH1);
		}
	}
	public static void main(String[] args) {
		
		init();
		run();
	}

}
