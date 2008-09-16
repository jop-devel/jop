/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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


package rtlib;

import java.util.Vector;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

/**
 * Test of the rtlib Buffer
 * 
 * @author Martin Schoeberl
 *
 */
public class CMPBuffer implements Runnable {
	
	final static int MAX = 10000000;
	
	boolean producer;
	int cnt;
	
	public CMPBuffer(boolean prod) {
		producer = prod;
	}
	
	static Vector msg;
	static rtlib.Buffer buf = new Buffer(5);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		msg = new Vector();
		
		System.out.println("Buffer test start");
		
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		if (sys.nrCpu<3) {
			System.out.println("Not enough CPUs for this test");
			System.exit(-1);
		}
		
		Startup.setRunnable(new CMPBuffer(true), 1);
		Startup.setRunnable(new CMPBuffer(false), 0);
				
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

		if (producer) {
			for (;;) {
				if (!buf.full()) {
					buf.write(cnt++);
				}
				if (cnt==MAX) break;
			}
			msg("Producer finished");
		} else {
			for (;;) {
				if (!buf.empty()) {
					int val = buf.read();
					if (val!=cnt) {
						msg("Problem!");
					}
					++cnt;
				}
				if (cnt==MAX) break;
			}
			msg("Consumer finished");
		}
	}
	
	void msg(String s) {
		msg.addElement(new StringBuffer(s));				
	}

}
