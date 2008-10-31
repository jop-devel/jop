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
package examples.safetycritical;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.JopSystem;
import javax.safetycritical.MissionDescriptor;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.PeriodicParameters;
import javax.safetycritical.Safelet;
import javax.safetycritical.SingleMissionSequencer;
import javax.safetycritical.Terminal;
import javax.safetycritical.ThreadConfiguration;

import jbe.kfl.JopSys;

/**
 * @author Martin Schoeberl
 *
 */
public class HelloSCJ extends MissionDescriptor implements Safelet {

	protected void initialize() {
		
		PeriodicEventHandler peh = new PeriodicEventHandler(
				new PriorityParameters(11),
				new PeriodicParameters(new RelativeTime(0,0), new RelativeTime(1000,0)),
				new ThreadConfiguration()
			) {
			public void handleAsyncEvent() {
				Terminal.getTerminal().writeln("Ping ");
			}
		};
	}

	public long missionMemorySize() {
		return 0;
	}

	public int getLevel() {
		return 1;
	}

	public MissionSequencer getSequencer() {
		// we assume this method is invoked only once
		return new SingleMissionSequencer(new PriorityParameters(13), this);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Terminal.getTerminal().writeln("Hello SCJ World!");
		JopSystem.startMission(new HelloSCJ());
	}

}
