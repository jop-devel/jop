/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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

import joprt.RtThread;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

/**
 * A real-time threaded version of Hello World for CMP
 * 
 * @author martin
 *
 */
public class RtHelloCMP extends RtThread {
	
	public RtHelloCMP(int prio, int us) {
		super(prio, us);
	}

	int id;
	
	static Vector msg;
	
	final static int NR_THREADS = 3;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		msg = new Vector();
		
		System.out.println("Hello World from CPU 0");
		
		SysDevice sys = IOFactory.getFactory().getSysDevice();
//		for (int i=0; i<sys.nrCpu; ++i) {
		for (int i=0; i<NR_THREADS; ++i) {
			RtHelloCMP th = new RtHelloCMP(1, 1000*1000);
			th.id = i;
			th.setProcessor(i%sys.nrCpu);
		}
		
		System.out.println("Start mission");
		// start mission and other CPUs
		RtThread.startMission();
		System.out.println("Mission started");
		
		// print their messages
		for (;;) {
//			System.out.print("*");
//			RtThread.sleepMs(500);
			int size = msg.size();
			if (size!=0) {
				StringBuffer sb = (StringBuffer) msg.remove(0);
				System.out.println(sb);
			}
		}
	}

	public void run() {
		
		StringBuffer sb = new StringBuffer();
		sb.append("Thread start on CPU ");
		sb.append(IOFactory.getFactory().getSysDevice().cpuId);
		msg.addElement(sb);
		StringBuffer ping = new StringBuffer();
		for (;;) {
			ping.setLength(0);
			ping.append((char) ('A'+id));
			msg.addElement(ping);
//			System.out.println("ping");
			waitForNextPeriod();
		}
	}

}
