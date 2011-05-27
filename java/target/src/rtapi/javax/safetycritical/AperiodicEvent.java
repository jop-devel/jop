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

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import javax.realtime.AsyncEvent;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import javax.safetycritical.annotate.Allocate.Area;

/**
 * @author Martin Schoeberl
 * 
 */
@SCJAllowed(LEVEL_1)
public class AperiodicEvent extends AsyncEvent {
	/**
	 * Constructor for an aperiodic event.
	 * <p>
	 * Does not allocate memory. Does not allow this to escape the local
	 * variables.
	 */
	@Allocate( { Area.THIS })
	@MemoryAreaEncloses(inner = { "this" }, outer = { "handler" })
	@SCJAllowed(LEVEL_1)
	@SCJRestricted(phase = INITIALIZATION)
	public AperiodicEvent() {
	}

	// TODO: the following code is from an older implementation.
	// Is it still valid? The spec contains no methods on AE
	AperiodicEventHandler aeh[];

	/**
	 * A aperiodic event bound to a handler.
	 * 
	 * @param h
	 */
	public AperiodicEvent(AperiodicEventHandler h) {
		aeh = new AperiodicEventHandler[1];
		aeh[0] = h;
	}

	/**
	 * Do we really want to bind several handlers to one event?
	 * 
	 * @param h
	 */
	public AperiodicEvent(AperiodicEventHandler h[]) {
		// a defensive copy
		aeh = new AperiodicEventHandler[h.length];
		for (int i = 0; i < h.length; ++i) {
			aeh[i] = h[i];
		}
	}

	public void fire() {
		int len = aeh.length;
		for (int i = 0; i < len; ++i) {
			aeh[i].unblock();
		}
	}

}
