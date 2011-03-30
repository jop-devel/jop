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
 * A CMP Hello World version with faked Threads 
 * 
 * @author martin
 *
 */
public class HelloThreadCMP implements Runnable {
	
	int id;
	
	static Vector msg;

	public HelloThreadCMP(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		msg = new Vector();
		
		System.out.println("Hello World from CPU 0");
		int cpus = Runtime.getRuntime().availableProcessors();
		System.out.println(cpus+" procesors available");
		
		Thread th[] = new Thread[cpus-1];
		
		for (int i=0; i<cpus-1; ++i) {
			Runnable r = new HelloThreadCMP(i+1);
			th[i] = new Thread(r);
			th[i].start();
		}
				
		printMsg(1);
		System.out.println("Join threads");
		for (int i=0; i<cpus-1; ++i) {
			th[i].join();
		}
		System.out.println("All threads finished, starting new ones");
		for (int i=0; i<cpus-1; ++i) {
			Runnable r = new HelloThreadCMP(i+5);
			th[i] = new Thread(r);
			th[i].start();
		}

		printMsg(10);
		
	}

	/**
	 * Print messages for n seconds
	 */
	private static void printMsg(int s) {
		
		for (int i=0; i<s*100; ++i) {
			int size = msg.size();
			if (size!=0) {
				StringBuffer sb = (StringBuffer) msg.remove(0);
				System.out.println(sb);
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}
	}

	public void run() {
		
		StringBuffer sb = new StringBuffer();
		sb.append("Hello World from CPU ");
		sb.append(id);
		for (int i=0; i<5; ++i) {
			msg.addElement(sb);	
			try {
				Thread.sleep(300*id);
			} catch (Exception e) {
			}
		}
	}

}
