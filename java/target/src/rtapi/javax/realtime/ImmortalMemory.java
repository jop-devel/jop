/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

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


package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * Mmh, there is no ManagedImmortal class. How shall we now implement
 * all the getter methods via pacakge boundaries...
 * We will transer the information via a final class in safetycritical,
 * which has no public contructor, and call a final method in IM expecting
 * this type. That is ugly, but should do the job.
 * @author martin
 *
 */
@SCJAllowed
public final class ImmortalMemory extends MemoryArea
{

	private static ImmortalMemory instance;
	private static long IMMORTAL_MEMORY_SIZE = 200;

	private ImmortalMemory(long size) 
	{
		super();
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public static ImmortalMemory instance()
	{
		if(instance == null)
		{
			instance = new ImmortalMemory(IMMORTAL_MEMORY_SIZE);
		}
		return instance;
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public void enter(Runnable logic)
	{

	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryConsumed()
	{
		return 0L; // dummy return
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryRemaining()
	{
		return 0L; // dummy return
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long size()
	{
		return 0L; // dummy return
	}
}
