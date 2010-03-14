/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
package csp;

import java.util.Vector;

import joprt.RtThread;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * @author martin
 *
 */
public class Main implements Runnable {
		
		int id;
		
		static Vector msg;

		public Main(int i) {
			id = i;
		}

		/**
		 * @param args
		 */
		public static void main(String[] args) {

			msg = new Vector();
			
			System.out.println("Hello World from CPU 0");
			
			SysDevice sys = IOFactory.getFactory().getSysDevice();
//			for (int i=0; i<sys.nrCpu-1; ++i) {
//				Runnable r = new Main(i+1);
//				Startup.setRunnable(r, i);
//			}
			Runnable r = new Main(1);
			Startup.setRunnable(r, 0);
			
			// start the other CPUs
			sys.signal = 1;
			
			int[] buffer = new int[10];
			// print their messages
			for (;;) {
				int size = msg.size();
				if (size!=0) {
					StringBuffer sb = (StringBuffer) msg.remove(0);
					System.out.println(sb);
				}
				
				if (NoC.isReceiving()) {
					buffer[0] = NoC.b_receive1();
					System.out.print("Received ");
					System.out.print((char) buffer[0]);
					System.out.println();
				}
				RtThread.sleepMs(100);
				System.out.print("Status: ");
				System.out.println(NoC.NOC_REG_STATUS);
				System.out.println(Native.rd(NoC.NOC_REG_STATUS));

			}
		}

		public void run() {
			
			StringBuffer sb = new StringBuffer();
			sb.append("Hello World from CPU ");
			sb.append(id);
			for (int i=0; i<10; ++i) {
				msg.addElement(sb);		
				RtThread.sleepMs(300*id);
				NoC.nb_send1(0, 'a'+i);
			}
		}

	}

