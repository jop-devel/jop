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

/**
 * @author martin
 *
 */
import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.SCJAllowed;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

@SCJAllowed
public class CyclicSchedule {
	@SCJAllowed
	final public static class Frame {
		
		Runnable handler;
		int usDuration;

		@SCJAllowed
		public Frame(RelativeTime duration, Runnable handler) {
			this.handler = handler;
			usDuration = (int) (duration.getMilliseconds()*1000 + duration.getNanoseconds()/1000);
		}

		@SCJAllowed
		final RelativeTime getDuration() {
			return null;
		}

		final Runnable[] getHandlers() {
			return null;
		}
	}

	@SCJAllowed
	public CyclicSchedule(Frame[] frames) {
	}

	Frame[][] frames;
	static SysDevice sys = IOFactory.getFactory().getSysDevice();

	/**
	 * Set the cyclic schedule for a multiprocessor version.
	 * 
	 * @param frames
	 */
	public CyclicSchedule(Frame[][] frames) {
		int nrCpus = Runtime.getRuntime().availableProcessors();
		if (frames.length > nrCpus) {
			throw new Error("Not enough CPUs for this schedule");
		}
		this.frames = frames;
		// TODO: check that all cycles are of the same length
		// anything else to do?
		
	}

	
	/**
	 * We don't care about the Safelet at the moment. Just start the stuff.
	 */
	public void startMission() {
		for (int i=0; i<frames.length-1; ++i) {
			Runnable r = new Runnable() {
				public void run() {
					executeCycle();					
				}
				
			};
			Startup.setRunnable(r, i);
		}
		
		// start the cycle in 1 ms
		startTime = sys.uscntTimer + 1000;
		// start the other CPUs
		sys.signal = 1;
		
		// also do a schedule
		executeCycle();
	}

	int startTime;
	static boolean frameOverrun;
	
	private void executeCycle() {
		
		// wait for start time
		int nextTime = startTime;
		int next = 0;
		Frame myFrames[] = frames[sys.cpuId];
		// THE endless loop
		for (;;) {
			Frame f = myFrames[next];
			++next;
			if (next==myFrames.length) {
				next = 0;
			}
			// wait for the start
			while (nextTime-sys.uscntTimer >= 0) {
				;
			}
			// run the runnable
			f.handler.run();
			// calculate next start time 
			nextTime += f.usDuration;
			// check for overrun
			if (nextTime-sys.uscntTimer < 0) {
				frameOverrun = true;
			}
		}
	}
	
	public static int getCurrentProcessor() {
		return sys.cpuId;
	}
	
	public static boolean isFrameOverrun() {
		return frameOverrun;
	}

	final RelativeTime getCycleDuration() {
		return null;
	}

	protected final Frame[] getFrames() {
		return null;
	}
}