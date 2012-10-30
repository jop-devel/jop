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


/**
 * 
 */
package javax.safetycritical;

import com.jopdesign.sys.Memory;

import joprt.RtThread;

/**
 * @author Martin Schoeberl
 *
 */
public class JopSystem {
	
	public static void startMission(Safelet scj) {
		
		MissionSequencer ms = scj.getSequencer();
//		System.out.println("Good");
		
//		MissionDescriptor md = ms.getInitialMission();
		// TODO: there is some chaos on mission and the classes
		// for it -- needs a reread on current spec
		// and a fix
//		MissionDescriptor md = ms.getNextMission();
//		MissionDescriptor md = null;
//		md.initialize();
		
		// this should be a loop
		Mission m = ms.getNextMission();
		// that should be done in the sequencer
		
		m.initialize();
		
		debug(m);
		
		Terminal.getTerminal().writeln("SCJ Start mission on JOP");
		//RtThread.startMission();
	}
	
	public static void startCycle(Safelet scj){
		
		CyclicExecutor missionEx = new CyclicExecutor(); 

		Memory missionMem = new Memory(100,100);
		
		MissionSequencer ms = scj.getSequencer();
		
		CyclicExecutive ce = (CyclicExecutive) ms.getNextMission();
		missionMem.resize(ce.missionMemorySize());
		missionEx.ce = ce;
		
		// Change allocation context to Mission memory
		missionMem.enter(missionEx);
		
	}
	
	static class CyclicExecutor implements Runnable {
		
		CyclicExecutive ce;
		
		@Override
		public void run() {
			
			// Private memory for all periodic handlers
			// should be reused.
			Memory privateMemory = new Memory(100,100);

			ce.initialize();
			/**
			 * Upon return from initialize(), the infrastructure invokes the 
			 * mission’s getSchedule method in a Level 0 run-time environment.
			 * The infrastructure creates an array representing all of the
			 * ManagedSchedulable objects that were registered by the initialize
			 * method and passes this array as an argument to the mission’s
			 * getSchedule method
			 */
			CyclicSchedule schedule = ce.getSchedule(ce.peHandlers);
			Frame[] frames = schedule.getFrames();
			FrameExecutor frameEx = new FrameExecutor(frames);
			
			for (int i = 0; i < frames.length; i++) {
				for (int j = 0; j < frames[i].handlers_.length; j++) {
					// Resize private memory according to handlers
					// storage parameters
					privateMemory.resize(0);

					frameEx.i = i;
					frameEx.j = j;

					privateMemory.enter(frameEx);
				}
			}
		}
		
	}
	
	static class FrameExecutor implements Runnable {
		
		int i,j = 0;
		Frame[] frames;
		
		public FrameExecutor(Frame[] frames) {
			this.frames = frames;
		}

		@Override
		public void run() {
			frames[i].handlers_[j].handleAsyncEvent();
		}
		
	}
	
	public static void debug(Mission m){
		System.out.println("Periodic handlers in mission: " +m.peHandlerCount);
		System.out.println("Current periodic handler index:  " +m.peHandlerIndex);
		System.out.println("Periodic handler array size:  " +m.peHandlers.length);

		System.out.println("Aperiodic handlers in mission: " +m.aeHandlerCount);
		System.out.println("Current aperiodic handler index:  " +m.aeHandlerIndex);
		System.out.println("Aperiodic handler array size:  " +m.aeHandlers.length);

		System.out.println("Aperiodic long handlers in mission: " +m.aleHandlerCount);
		System.out.println("Current aperiodic long handler index:  " +m.aleHandlerIndex);
		System.out.println("Aperiodic long handler array size:  " +m.aleHandlers.length);

	}
	
//	public static void runMission(Safelet scj){
//		
//		MissionSequencer ms = scj.getSequencer();
//		
//		Memory missionMem;
//		Mission m;
//		
//		//initial mission
//		m = ms.getNextMission();
//		
//		while(m != null){
//
//			int x = (int) m.missionMemorySize();
//			
//			// In mission memory
//			Memory.getCurrentMemory().enterPrivateMemory(x, m.start());
//			
//			// When we return from mission memory
//			m = ms.getNextMission();
//		}
//	}
}
