/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package test.level0;

import javax.realtime.*;
import javax.safetycritical.*;
import javax.safetycritical.CyclicSchedule;

import test.cyclic.EventHandler;

public class Level0Mission extends CyclicExecutive {

	int id;
	long missionMemory = 1024;
	int totalPeriodicHandlers = 3;
	
	public Level0Mission(int id) {
		this.id = id;
	}
		
	@Override
	protected void initialize() {
		
		MyLevel0App.term.writeln("Initializing mission " + id);
		
		// Create the handlers for the frames
		for (int i = 0; i < totalPeriodicHandlers; i++) {
			(new EventHandler(new PriorityParameters(i + 10),
					new PeriodicParameters(null, new RelativeTime(10, 0)),
					new StorageParameters(1024, null), 256, "PEH"+i)).register();
		}
	}

	public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {

		Frame[] frames = new Frame[3];
		frames[0] = new Frame(new RelativeTime(1500, 0),
				new PeriodicEventHandler[] { handlers[0], handlers[1] });
		frames[1] = new Frame(new RelativeTime(1500, 0),
				new PeriodicEventHandler[] { handlers[1], handlers[2] });
		frames[2] = new Frame(new RelativeTime(2000, 0),
				new PeriodicEventHandler[] { handlers[2], handlers[0] });

		return new CyclicSchedule(frames);

	}

	@Override
	public long missionMemorySize() {
		return missionMemory;
	}
	
	@Override
	public void cleanUp(){
		super.cleanUp();
	}

}

//public class Level0Mission extends Mission {
//
//	private CyclicSchedule customSchedule;
//	int id;
//
//	public Level0Mission(int id) {
//		this.id = id;
//	}
//
//	@Override
//	protected void initialize() {
//
//		MyLevel0App.term.writeln("Initializing mission " + id);
//
//		// Cyclic schedule
//		Task taskSet[] = new Task[3];
//
//		RelativeTime frameLength = new RelativeTime(500, 0);
//
//		taskSet[0] = new Task(new RelativeTime(100, 0), 512, 0);
//		taskSet[1] = new Task(new RelativeTime(100, 0), 512, 1);
//		taskSet[2] = new Task(new RelativeTime(100, 0), 512, 2);
//
//		CyclicSchedule.Frame frames[] = { new CyclicSchedule.Frame(frameLength,
//				taskSet) };
//
//		customSchedule = new CyclicSchedule(frames);
//
//	}
//
//	public CyclicSchedule getSchedule() {
//		return customSchedule;
//
//	}
//
//	@Override
//	public long missionMemorySize() {
//		// TODO Auto-generated method stub
//		return 2048;
//	}
//
//	/**
//	 * TODO: Timing for execution of tasks
//	 */
//
//	@Override
//	protected Runnable start() {
//
//		return new Runnable() {
//
//			@Override
//			public void run() {
//
//				initialize();
//
//				// Execute in mission memory
//				for (int i = 0; i < customSchedule.frames.length; i++) {
//					// Get individual frames
//					CyclicSchedule.Frame frame = customSchedule.frames[i];
//					Task tasks[] = frame.getTaskSet();
//					for (int j = 0; j < tasks.length; j++) {
//						Memory.getCurrentMemory().enterPrivateMemory(
//								tasks[j].scopeSize, tasks[j]);
//					}
//				}
//			};
//
//		};
//
//	}
//}
