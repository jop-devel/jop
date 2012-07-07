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

package scopeuse.ex2;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

/**
 * 
 * @author jrri
 *
 */

public class ExecWithRetHandler extends PeriodicEventHandler{
	
	public ExecWithRetHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("***************** Handler *****************");
		System.out.println("");
		
		SimpleIn in  = new SimpleIn();
		SimpleOut out  = new SimpleOut();

		// Allocate input parameters
		in.param1 = System.currentTimeMillis();
		in.param2 = System.nanoTime();
		
		Runnable r = new Worker(in, out);
		
		ManagedMemory.enterPrivateMemory(256, r);
		
		// now we can use out.result
		System.out.println("Result: "+out.result);

	}
}
