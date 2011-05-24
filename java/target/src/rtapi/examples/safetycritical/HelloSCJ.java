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

package examples.safetycritical;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.*;

/**
 * A minimal SCJ application - The SCJ Hello World
 * 
 * @author Martin Schoeberl
 *
 */
public class HelloSCJ extends Mission implements Safelet {

	// From Mission
	@Override
	protected void initialize() {
		
		PeriodicEventHandler peh = new PeriodicEventHandler(
				new PriorityParameters(11),
				new PeriodicParameters(new RelativeTime(0,0), new RelativeTime(1000,0)),
				new StorageParameters(0, 0, 0)
			) {
			public void handleAsyncEvent() {
				Terminal.getTerminal().writeln("Ping ");
			}
		};
	}

	// Safelet methods
	@Override
	public MissionSequencer getSequencer() {
		// we assume this method is invoked only once
		StorageParameters sp = new StorageParameters(1000000, 0, 0);
		return new LinearMissionSequencer(new PriorityParameters(13), sp, this);
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		
	}


	
	// not used anymore
//	public long missionMemorySize() {
//		return 0;
//	}
//
//	public int getLevel() {
//		return 1;
//	}


	/**
	 * Within the JOP SCJ version we use a main method instead
	 * of a command line parameter or configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello");
		Terminal.getTerminal().writeln("Hello SCJ World!");
		JopSystem.startMission(new HelloSCJ());
	}


}
