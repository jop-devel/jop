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
 * @author Martin Schoeberl
 * 
 * @param <MissionLevel>
 */
@SCJAllowed
public interface Safelet<MissionLevel extends Mission> {

	@SCJAllowed(SUPPORT)
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer<MissionLevel> getSequencer();

	@SCJAllowed(SUPPORT)
	public long immortalMemorySize();
}
