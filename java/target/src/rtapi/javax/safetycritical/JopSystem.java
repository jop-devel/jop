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

import java.util.Vector;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;

import com.jopdesign.sys.Memory;

import joprt.RtThread;

/**
 * @author Martin Schoeberl
 * 
 * I'm not sure that so much code shall be in this JOP specific class.
 * 
 */
public class JopSystem {

	public static void startMission(Safelet scj) {

		scj.initializeApplication();
		
		MissionSequencer ms = scj.getSequencer();
		Mission.currentSequencer = ms;

		// MissionDescriptor md = ms.getInitialMission();
		// TODO: there is some chaos on mission and the classes
		// for it -- needs a reread on current spec
		// and a fix
		// MissionDescriptor md = ms.getNextMission();
		// MissionDescriptor md = null;
		// md.initialize();

		// this should be a loop
		Mission m = ms.getNextMission();
		ms.current_mission = m;
		// that should be done in the sequencer

		// this should also run in mission memory
		m.initialize();

		Terminal.getTerminal().writeln("SCJ Start mission on JOP");
		RtThread.startMission();
	}

	public void startCycle(Safelet<CyclicExecutive> scj) {

		// Maximum memory size required for all
		// missions in the application, should
		// be known in advance
		int MAX_MISSION_MEM = 1 * 106 * 1000;
		
		Helper helper = new Helper();

		CyclicExecutor cycExec = new CyclicExecutor(helper);

		scj.initializeApplication();

		MissionSequencer<CyclicExecutive> ms = scj.getSequencer();
		Mission.currentSequencer = ms;
		cycExec.ms = ms;

		while (helper.nextMission) {

			// Change allocation context to Mission memory
			Memory.getCurrentMemory().enterPrivateMemory(MAX_MISSION_MEM,
					cycExec);

		}
	}

	class CyclicExecutor implements Runnable {

		MissionSequencer<CyclicExecutive> ms;
		Helper helper;

		public CyclicExecutor(Helper helper) {
			this.helper = helper;
		}

		@Override
		public void run() {

			CyclicExecutive ce;

			// Mission object is created in Mission Memory
			ce = (CyclicExecutive) ms.getNextMission();
			ms.current_mission = ce;

			if (ce != null) {

				Terminal.getTerminal().writeln("Got new mission...");

				// Current memory is mission memory
				ce.phase = Mission.INITIIALIZATION;
				ce.initialize();

				/**
				 * Upon return from initialize(), the infrastructure invokes the
				 * mission's getSchedule method in a Level 0 run-time
				 * environment. The infrastructure creates an array representing
				 * all of the ManagedSchedulable objects that were registered by
				 * the initialize method and passes this array as an argument to
				 * the mission's getSchedule method
				 */

				Vector v = ce.getHandlers();
				PeriodicEventHandler[] peHandlers = new PeriodicEventHandler[v
						.size()];
				v.copyInto(peHandlers);

				ce.phase = Mission.EXECUTION;
				CyclicSchedule schedule = ce.getSchedule(peHandlers);
				Frame[] frames = schedule.getFrames();

				/**
				 * The total size required can be the maximum of the backing
				 * store sizes needed for each handler's private memories.
				 */
				long maxScopeSize = 0;
				long maxBsSize = 0;
				for (int i = 0; i < frames.length; i++) {
					for (int j = 0; j < frames[i].handlers_.length; j++) {
						long k = frames[i].handlers_[j].getScopeSize();
						long l = frames[i].handlers_[j].storage	.getTotalBackingStoreSize();

						maxScopeSize = (k > maxScopeSize) ? k : maxScopeSize;
						maxBsSize = (l > maxBsSize) ? l : maxBsSize;

					}
				}

				Memory handlerPrivMemory = new Memory((int) maxScopeSize,
						(int) maxBsSize);

				HandlerExecutor handlerExecutor = new HandlerExecutor();

				AbsoluteTime now = new AbsoluteTime();

				AbsoluteTime next = new AbsoluteTime();
				next = Clock.getRealtimeClock().getTime(next).add(1000, 0);

				MissionSequencer.terminationRequest = false;

				while (!ce.terminationPending()) {

					for (int i = 0; i < frames.length; i++) {

						while (Clock.getRealtimeClock().getTime(now)
								.compareTo(next) < 0) {
							;
						}

						for (int j = 0; j < frames[i].handlers_.length; j++) {

							handlerExecutor.handler = frames[i].handlers_[j];

							/**
							 * Since no two PeriodicEventHandlers in a Level 0
							 * application are permitted to execute
							 * simultaneously, the backing store for the private
							 * memories may be reused. In order for this to be
							 * achieved, the implementation may revoke the
							 * backing store reservation for the private memory
							 * of a periodic event handler at the end of its
							 * release.
							 */

							handlerPrivMemory.enter(handlerExecutor);
						}

						next = next.add(frames[i].duration_, next);

						if (Clock.getRealtimeClock().getTime(now)
								.compareTo(next) > 0) {
							// Frame overrun
							Terminal.getTerminal().writeln("Frame overrun");
						}
					}
				}

				ce.phase = Mission.CLEANUP;
				ce.cleanUp();
				ce.phase = Mission.INACTIVE;
			} else {
				Terminal.getTerminal().writeln("No more missions...");
				helper.nextMission = false;
			}
		}
	}

	class HandlerExecutor implements Runnable {

		PeriodicEventHandler handler;

		@Override
		public void run() {
			handler.handleAsyncEvent();
		}

	}

	class Helper {
		boolean nextMission = true;
	}

	// public static void runMission(Safelet scj){
	//
	// MissionSequencer ms = scj.getSequencer();
	//
	// Memory missionMem;
	// Mission m;
	//
	// //initial mission
	// m = ms.getNextMission();
	//
	// while(m != null){
	//
	// int x = (int) m.missionMemorySize();
	//
	// // In mission memory
	// Memory.getCurrentMemory().enterPrivateMemory(x, m.start());
	//
	// // When we return from mission memory
	// m = ms.getNextMission();
	// }
	// }
}
