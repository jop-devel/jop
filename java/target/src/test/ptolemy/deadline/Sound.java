/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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
package ptolemy.deadline;

import util.Timer;
import joprt.RtThread;

import com.jopdesign.io.DspioFactory;
import com.jopdesign.io.OutPort;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Use deadline timer for sound generation with PWM.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 */
public class Sound {

	final static int PERIOD = 60000000/44100;

	public static void main(String[] args) {

		int time, off, val, idx;

		SysDevice sys = DspioFactory.getDspioFactory().getSysDevice();
		OutPort pwm = DspioFactory.getDspioFactory().getOutPort();
		
		int samples[] = new int[16*4*1024];
		val = 0;
		for (int i=0; i<16*4*1024; ++i) {
//			val += 149;
			val += 1490;
			if (val>65535) val = 0;
			samples[i] = val>>>6;
		}
		
		System.out.println("Period: "+PERIOD);

		time = sys.cntInt;
		val = 300;
		time += 1000;
		idx = 0;

//		for (;;) {
//			time += 30;
//			Native.wr(time, Const.IO_DEADLINE);
//			Native.wr(3, Const.IO_DSPIO_OUT);
//			time += 30;
//			Native.wr(time, Const.IO_DEADLINE);
//			Native.wr(0, Const.IO_DSPIO_OUT);
//		}

		for (;;) {
			time += PERIOD;
			++idx;
			idx &= 0xffff;
			val = 100+samples[idx];
			off = time + val;
			sys.deadLine = time;
			pwm.port = 3;
			sys.deadLine = off;	// some offset is needed - we have now a 1 offset
			pwm.port = 0;				// and a 1 phase offset. Is this ok?
		}

	}

}
