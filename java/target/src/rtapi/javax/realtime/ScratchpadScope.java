/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

package javax.realtime;

import com.jopdesign.io.IOFactory;

public class ScratchpadScope extends ScopedMemory {

	/**
	 * All instances of ScratchpadScope on the same processor
	 * point to the same on-chip memory.
	 * TODO: we have to find a solution to avoid this sharing.
	 * 
	 * TODO: find a nicer solution for SCJ version of SPM.
	 */
	public ScratchpadScope() {
//		super(IOFactory.getFactory().getScratchpadMemory()[0]);
	}

//	@Override
//	public void enter(Runnable logic)
//	{
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public long memoryConsumed()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long memoryRemaining()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
