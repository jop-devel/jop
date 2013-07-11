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

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.ReleaseParameters;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Memory;
import com.jopdesign.sys.RtThreadImpl;

import joprt.RtThread;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;
import static javax.safetycritical.annotate.Phase.CLEANUP;
import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;

/**
 * An almost empty class, just to add two methods.
 * 
 * @author Martin Schoeberl
 * 
 */
@SCJAllowed
public abstract class ManagedEventHandler extends BoundAsyncEventHandler
		implements ManagedSchedulable {

	private String name;

	@SCJAllowed(INFRASTRUCTURE)
	@SCJRestricted(phase = INITIALIZATION)
	ManagedEventHandler(PriorityParameters priority, ReleaseParameters release,
			StorageParameters scp, String name) {
		this.name = name;
	}

	@Override
	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = CLEANUP)
	public void cleanUp() {
		
		System.out.println("MEH cleanup");
	}

	// TODO: do we need to repeat it here?
	// ok, why not...
	@Override
	@SCJAllowed(SUPPORT)
	public abstract void handleAsyncEvent();

	@SCJAllowed
	public String getName() {
		return name;
	}

	//jrri: Not in v 0.9 of spec
//	@Override
//	@SCJAllowed
//	@SCJRestricted(phase = INITIALIZATION)
//	public void register() {
//		
//	}
	
	//jrri: Not in v 0.9 of spec
//	@SCJAllowed
//	public static ManagedEventHandler getCurrentHandler(){
//		return null;
//	}
}
