/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2012, Martin Schoeberl (martin@jopdesign.com)

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

package examples.scjhello;

import javax.safetycritical.*;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;


/**
 * A minimal SCJ application - The SCJ Hello World
 * Now in a version of an application defined class
 * for each needed class.
 * 
 * @author Martin Schoeberl
 * 
 */
public class HelloSafelet implements Safelet {

	@Override
	public MissionSequencer getSequencer() {
		return new HelloSequencer(new HelloMission());
	}


	@Override
	public long immortalMemorySize() {
		return 1000;
	}

	/**
	 * Within the JOP SCJ version we use a main method instead of a command line
	 * parameter or configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// in SCJ we don't have System.out,
		// but for now it's nice for debugging
		System.out.println("Hello");
		JopSystem.startMission(new HelloSafelet());
	}


	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		// TODO Auto-generated method stub
		
	}

}
