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

package test;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Lego {

	final static int MAX = 1000;

	final static int IO_MOTOR = Const.IO_LEGO;

/*
	en2 <= m_out(5);
	in2a <= m_out(4);
	in2b <= m_out(3);
	en1 <= m_out(2);
	in1a <= m_out(1);
	in1b <= m_out(0);
*/
	final static int M_EN1 = 4;
	final static int M_1A = 2;
	final static int M_1B = 1;
	final static int M_EN2 = 32;
	final static int M_2A = 16;
	final static int M_2B = 8;

	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");

		for (;;) {
			Native.wr(M_EN1|M_1A, Const.IO_LEGO);
			RtThread.sleepMs(500);
			Native.wr(M_EN1|M_1A, Const.IO_LEGO);
			RtThread.sleepMs(500);
		}
		/*-
		new RtThread(10, 100) {
			public void run() {

				int cnt = 0;

				int t = Native.rd(Const.IO_US_CNT);


				for (;;) {
					t += 100;
					while (t-Native.rd(Const.IO_US_CNT) > 0)
						;
					// waitForNextPeriod();
					if ((cnt & 0x01)==0 && cnt<24) {
						Native.wr(6, IO_MOTOR);
					} else {
						Native.wr(0, IO_MOTOR);
					}
					cnt = (cnt+1)&0x1f;
				}
			}
		};

		RtThread.startMission();

		//forever();
		for (;;) ;
		*/
	}

	static void forever() {

		int[] val = new int[MAX];

		for (;;) {
			// 0V ... 130 (short)
			// 5V ... 447 (open)
			int v = Native.rd(Const.IO_ADC1)-130;

			v *= 81;	// v = v/3.17
			v >>>= 8;
			if (v<0) v=0;
			if (v>100) v=100;
/*
			Dbg.intVal(v);
*/

			v = Native.rd(Const.IO_ADC2);
			int va = v & 0xffff;
			int vb = v >>> 16;
			// 9 bit ADC, 255 = 1.65V
			// 511 = 18.15V
			// 0 = - 14.85V
			// U = v*0.0644531 - 14.85
			va *= 6600;
			va >>>= 10;
			va -= 1485;			// [va] is 10mV
			vb *= 6600;
			vb >>>= 10;
			vb -= 1485;
/*
			Dbg.intVal(va);
			Dbg.intVal(vb);
			Dbg.lf();
*/

			RtThread.sleepMs(500);
			Timer.wd();
		}


/*
		for (;;) {
			int t = Native.rd(Const.IO_US_CNT);

			for (int i=0; i<MAX; ++i) {

				t += 10;
				while (t-Native.rd(Const.IO_US_CNT) > 0)
					;
				val[i] = Native.rd(Const.IO_ADC1);
			}

			RtThread.sleepMs(1000);
			Dbg.lf();
			for (int i=0; i<MAX; ++i) {
				Dbg.intVal(val[i]);
			}
		}
*/

		// mark the end of the program
		// in emb. systems there is no exit()
//		for (;;);
//		we don't need this anymore!
	}

	static int mcnt;
	static int state;
	static int motor;
	static int sum;

	static void motor() {

		++mcnt;

		int s = mcnt & 0x3;
		if (s>1) {
			int v = Native.rd(Const.IO_ADC2);
			int va = v & 0xffff;
			int vb = v >>> 16;
			v = va-vb;
			if (v>5 || v<-5) sum += v;
		}
		if (s==3) {
				motor |= M_EN1;
			if (sum>100) {
				motor |= M_1A;
			} else if (sum<-100) {
				motor |= M_1B;
			}
/*
		} else if (s==1) {
			motor = M_EN1;
*/
		} else {
			motor = 0;
		}
		Native.wr(motor, IO_MOTOR);

		if ((mcnt&127)==0) {
			System.out.println(sum);
		}
/*
		if (mcnt==100) {
			mcnt = 0;

			if (state==0) {
Dbg.wr("High Z ");
				;
			} else if (state==1) {
Dbg.wr("Forward ");
				motor |= M_EN1;
				motor |= M_1A;
			} else if (state==2) {
Dbg.wr("Backward ");
				motor |= M_EN1;
				motor |= M_1B;
			} else if (state==3) {
Dbg.wr("LL ");
				motor |= M_EN1;
			} else if (state==4) {
Dbg.wr("HH ");
				motor |= M_EN1;
				motor |= M_1A;
				motor |= M_1B;
			}
			++state;
			if (state>5) state = 0;
				
		}
*/
	}

}
