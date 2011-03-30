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
package cmpce;

import java.util.Vector;

import javax.realtime.RelativeTime;

/**
 * @author martin
 * 
 */
public class Task implements Runnable {

	/**
	 * A message queue for the print out.
	 */
	static Vector msg = new Vector(10);

	StringBuilder myMsg;
	int id;
	int msCost;

	public Task(int id, RelativeTime cost) {
		myMsg = new StringBuilder(10);
		this.id = id;
		msCost = (int) cost.getMilliseconds();
	}

	public void run() {

		long time = System.currentTimeMillis();
		myMsg.setLength(0);
		myMsg.append("Hello from task t");
		myMsg.append((char) ('0' + id));
		myMsg.append(" on CPU ");
		// for this type of thing scopes would be nice
		// is generates garbage and the CMP system will crash
		// myMsg.append(CyclicSchedule.getCurrentProcessor());
		// shall we use the CPU local scopes we have?
		myMsg.append((char) ('0' + CyclicSchedule.getCurrentProcessor()));
		msg.addElement(myMsg);
		
		time += msCost;
		while (System.currentTimeMillis()-time < 0) {
			;
		}

	}

	static void printMsg() {
		for (int i = 0; i < 3; ++i) {
			int size = msg.size();
			if (size != 0) {
				StringBuilder sb = (StringBuilder) msg.remove(0);
				System.out.println(sb);
			}
		}
		if (CyclicSchedule.isFrameOverrun()) {
			System.out.println("Frame overrun");
			// stop the output
			for (;;) {
				;
			}
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Let's start with the CPM CE scheduler!");

		Runnable printer = new Runnable() {
			public void run() {
				printMsg();
			}
		};

		Runnable r1 = new Task(1, new RelativeTime(100, 0));
		Runnable r2 = new Task(2, new RelativeTime(300, 0));
		Runnable r3 = new Task(3, new RelativeTime(300, 0));

		CyclicSchedule.Frame frame0[] = {
				new CyclicSchedule.Frame(new RelativeTime(50, 0), printer),
		};

		CyclicSchedule.Frame frame1[] = {
				new CyclicSchedule.Frame(new RelativeTime(100, 0), r1),
				new CyclicSchedule.Frame(new RelativeTime(300, 0), r3),
		};
		
		CyclicSchedule.Frame frame2[] = {
				new CyclicSchedule.Frame(new RelativeTime(300, 0), r2),
				new CyclicSchedule.Frame(new RelativeTime(100, 0), r1),
		};
		
		CyclicSchedule.Frame cmpSchedule[][] = { frame0, frame1, frame2 };
		CyclicSchedule sch = new CyclicSchedule(cmpSchedule);

		sch.startMission();
	}

}
