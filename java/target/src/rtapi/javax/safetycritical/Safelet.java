/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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


/**
 * 
 */
package javax.safetycritical;

/*
Here follows an issue list:
	jx.rt.PeriodicParameter cannot return the start and period parameters.
	There is no way (except JNI) to transfer those parameters in a safe way
	from jx.scj.PeriodicParameters as we are crossing package boundaries.
	
	BAEH is useless on MissionSequencer on an implementation that does
	not run on top of the RTSJ. We also see the public method
	handleAsyncEvent, which is not so good as an application can
	invoke it. Would that trigger another start of a mission?
	
	In level 0 and 1 the priority of the sequencer has no meaning as
	the sequencer is passive. SCJAllow(2)?

Questions:
	Is it useful to have a start time that is longer than a period?
	
	Why is the handleEvent in AEH abstract, but PAEH does not define a
	new one (it is inherited from jx.rt.AEH in PAEH).
	
	We could provide a default implementation of getNextMission that
	returns null. Makes the Hello World shorter.

 */

/**
 * The interface that represents the SCJ application.
 * 
 * @author Martin Schoeberl
 *
 */
public interface Safelet {
	
	final static int LEVEL_0 = 0;
	final static int LEVEL_1 = 1;
	final static int LEVEL_2 = 2;

	/**
	 * The SCJ level. Here an integer instead of the enum.
	 * @return
	 */
	public int getLevel();
	
	public MissionSequencer getSequencer();
}
