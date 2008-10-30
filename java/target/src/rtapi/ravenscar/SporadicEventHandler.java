/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package ravenscar;

import joprt.*;

// import javax.realtime.*;
// public class SporadicEventHandler extends javax.realtime.BoundAsyncEventHandler {
public class SporadicEventHandler extends SwEvent {

	public SporadicEventHandler(PriorityParameters pp, SporadicParameters spor) {

		// super(pri, spor, null, ImmortalMemory.instance(),null, true, null); // no heap

		super(pp.getPriority(),
				((int) spor.getMinInterarrival().getMilliseconds())*1000 + spor.getMinInterarrival().getNanoseconds()/1000
			);

	}
	
	// override SwEvents handle method to call jagun's handle-version
	public void handle() {

		handleAsyncEvent();
	}

	/*
	public MemoryArea getMemoryArea()
	{
		return super.getMemoryArea();
	}
	*/
	
	// ovveride to handle event
	
	public void handleAsyncEvent() { }
};

