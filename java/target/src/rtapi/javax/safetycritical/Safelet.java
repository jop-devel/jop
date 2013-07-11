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

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * The interface that represents the SCJ application.
 * 
 * A safety-critical application consists of one or more missions, executed
 * concurrently or in sequence. Every safety-critical application is represented
 * by an implementation of Safelet which identifies the outer-most
 * MissionSequencer. This outer-most MissionSequencer takes responsibility for
 * running the sequence of missions that comprise this safety-critical
 * application. The mechanism used to identify the Safelet to a particular SCJ
 * environment is implementation defined. For the MissionSequencer returned from
 * getSequencer, the SCJ infrastructure arranges for an independent thread to
 * begin executing the code for that sequencer and then waits for that thread to
 * terminate its execution.
 * 
 * @author Martin Schoeberl
 * 
 * @param <MissionLevel>
 */
@SCJAllowed
public interface Safelet<MissionLevel extends Mission> {

	/**
	 * The infrastructure shall invoke <code>initializeApplication</code> in the
	 * allocation context of immortal memory. The application can use this
	 * method to allocate data structures that are in immortal memory.
	 * <code>initializeApplication</code> shall be invoked after
	 * <code>immortalMemorySize</code>, and before <code>getSequencer</code>.
	 */
	@SCJAllowed(SUPPORT)
	// MS: looks like there is a change in the annotation system in SCJ
//	@SCJRestricted(INITIALIZATION)
	@SCJRestricted(phase = INITIALIZATION)
	public void initializeApplication();

	/**
	 * The infrastructure invokes getSequencer to obtain the MissionSequencer
	 * object that oversees execution of missions for this application. The
	 * returned MissionSequencer resides in immortal memory.
	 * 
	 * @return the MissionSequencer that oversees execution of missions for this
	 *         application.
	 */

	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer<MissionLevel> getSequencer();

	/**
	 * 
	 * @return the amount of immortal memory currently available for execution
	 *         of this application.
	 */
	@SCJAllowed(SUPPORT)
	public long immortalMemorySize();
}
