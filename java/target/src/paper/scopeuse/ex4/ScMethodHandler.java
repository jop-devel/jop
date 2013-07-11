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

package scopeuse.ex4;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Memory;
import com.jopdesign.sys.Native;

/**
 * 
 * @author jrri
 *
 */

public class ScMethodHandler extends PeriodicEventHandler{
	
	public ScMethodHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
	}

	@Override
	public void handleAsyncEvent() {
		System.out.println("***************** Handler *****************");
		System.out.println("");

		// Created in the scope where handler executes
		ParamObject pObj = new ParamObject();
		
		pObj.mem =  Memory.getCurrentMemory();
		pObj.param_X = 120;
		pObj.retObject = null;
		
		// This object simulates the method with
		// parameters that returns an object
		Method myMethod = new Method(pObj);
		ManagedMemory.enterPrivateMemory(256, myMethod);
		
		// Now the returned object can be used
		System.out.println(pObj.retObject.keys[1]);
		
	}
}
