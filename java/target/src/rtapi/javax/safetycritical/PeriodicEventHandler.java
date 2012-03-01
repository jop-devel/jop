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

package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.LEVEL_0;

import javax.realtime.HighResolutionTime;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import javax.safetycritical.*;

import com.jopdesign.sys.Memory;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import joprt.RtThread;

/**
 * The class that represents periodic activity. Should be used as the main
 * vehicle for real-time applications.
 * 
 * Now finally we have a class that the implementation can use.
 * 
 * @author Martin Schoeberl
 * 
 */

@SCJAllowed
public abstract class PeriodicEventHandler extends ManagedEventHandler {

	PriorityParameters priority;
	RelativeTime start, period;
	StorageParameters sp;
	// ThreadConfiguration tconf;
	String name;
	
	Memory privMem; 

	RtThread thread;

	@MemoryAreaEncloses(inner = { "this", "this", "this" }, outer = {
			"priority", "parameters", "scp" })
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp) {
		this(priority, parameters, scp, "");
	}

	@MemoryAreaEncloses(inner = { "this", "this", "this", "this" }, outer = {
			"priority", "parameters", "scp", "name" })
	@SCJAllowed(LEVEL_1)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters release, StorageParameters scp, String name) {
		// TODO: what are we doing with this Managed thing?
		super(priority, release, scp, name);
		this.priority = priority;
		

		start = (RelativeTime)release.getStart();
		period = release.getPeriod();
		// TODO scp
		// this.tconf = tconf;
		this.name = name;
		int p = ((int) period.getMilliseconds()) * 1000
				+ period.getNanoseconds() / 1000;
		if(p < 0) { // Overflow
			p = Integer.MAX_VALUE;
		}
		int off = ((int) start.getMilliseconds()) * 1000
				+ start.getNanoseconds() / 1000;
		if(off < 0) { // Overflow
			off = Integer.MAX_VALUE;
		}
		
		// TODO: this is a very quick hack to get privat memory
		// working. The StorageParameters is incomplete. Was this
		// updated in the spec?
		privMem = new Memory((int) scp.getTotalBackingStoreSize(), (int) scp.getTotalBackingStoreSize());

		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				handleAsyncEvent();
			}	
		};

		thread = new RtThread(priority.getPriority(), p, off) {

			public void run() {
				while (!MissionSequencer.terminationRequest) {
					privMem.enter(runner);
					waitForNextPeriod();
				}
			}
		};
	}

	@SCJAllowed
	@Override
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {

	}
}
