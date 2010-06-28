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
 * A CMP version of Hello World
 * 
 * @author martin
 *
 */
public class HelloCMP implements Runnable {
	
	int id;
	
	static Vector msg;

	public HelloCMP(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		msg = new Vector();
		
		System.out.println("Hello World from CPU 0");
		
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		for (int i=0; i<sys.nrCpu-1; ++i) {
			Runnable r = new HelloCMP(i+1);
			Startup.setRunnable(r, i);
		}
		
		// start the other CPUs
		sys.signal = 1;
		
		// print their messages
		for (;;) {
			int size = msg.size();
			if (size!=0) {
				StringBuffer sb = (StringBuffer) msg.remove(0);
				System.out.println(sb);
			}
		}
	}

	public void run() {
		
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World from CPU ");
		sb.append(id);
		for (int i=0; i<10; ++i) {
			msg.addElement(sb);		
			RtThread.sleepMs(300*id);
		}
	}

}
