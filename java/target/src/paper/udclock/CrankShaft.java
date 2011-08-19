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

package udclock;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.*;
import javax.safetycritical.io.SimplePrintStream;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.JVMHelp;

/**
 * A minimal SCJ application to show the crankshaft clock
 * with the extended model of user defined clocks.
 * 
 * @author Martin Schoeberl
 * 
 */
public class CrankShaft extends Mission implements Safelet {

	// work around...
	static CrankShaft single;
	
	static CrankshaftClock clock;

	static SimplePrintStream out;
	


	// From Mission
	@Override
	protected void initialize() {

		OutputStream os = null;
		try {
			os = Connector.openOutputStream("console:");
		} catch (IOException e) {
			throw new Error("No console available");
		}
		out = new SimplePrintStream(os);
		
		clock = new CrankshaftClock();
		// the clock is also an interrupt handler
		JVMHelp.addInterruptHandler(1, clock);
		// a helper handler to trigger the SW interrupt
		// and simulate an external interrupt source
		PeriodicEventHandler helper = new PeriodicEventHandler(
				new PriorityParameters(12), new PeriodicParameters(
						new RelativeTime(0, 0), new RelativeTime(100, 0)),
				new StorageParameters(10000, 1000, 1000)) {

			SysDevice sys = IOFactory.getFactory().getSysDevice();

			public void handleAsyncEvent() {
				// generate a SW interrupt
				sys.intNr = 1;
			}
		};
		helper.register();


		// A PEH that reads out the standard clock and the crankshaft clock
		PeriodicEventHandler peh = new PeriodicEventHandler(
				new PriorityParameters(11), new PeriodicParameters(
						new RelativeTime(0, 0), new RelativeTime(1000, 0)),
				new StorageParameters(10000, 1000, 1000)) {
			int cnt;
			AbsoluteRotationalTime dest = new AbsoluteRotationalTime(clock);

			public void handleAsyncEvent() {
				// The following type conversion is needed as we have
				// changed the classes to the new model.
				// Not needed in the RTSJ 1.1 version
				AbsoluteTime time = (AbsoluteTime) Clock.getRealtimeClock().getTime();
				out.print("It is " + time.getMilliseconds());
				clock.getTime(dest);
				out.println(" rotations " + dest.getRotations() + " degrees " + dest.getDegrees());
				++cnt;
				if (cnt > 10) {
					// getCurrentMission is not yet working
					single.requestTermination();
				}
			}
		};
		peh.register();
		
		// This is our periodic event handler that shall be
		// scheduled by the crankshaft clock
		PeriodicEventHandler rotation = new PeriodicEventHandler(
				new PriorityParameters(10), new PeriodicParameters(
						new RelativeTime(0, 0), new RelativeTime(1000, 0)),
				new StorageParameters(10000, 1000, 1000)) {
			int cnt;
			AbsoluteRotationalTime dest = new AbsoluteRotationalTime(clock);

			public void handleAsyncEvent() {
				out.println("Rotation tick");
			}
		};
		rotation.register();
	}

	// Safelet methods
	@Override
	public MissionSequencer getSequencer() {
		// we assume this method is invoked only once
		StorageParameters sp = new StorageParameters(1000000, 0, 0);
		return new LinearMissionSequencer(new PriorityParameters(13), sp, this);
	}

	@Override
	public long missionMemorySize() {
		return 100000;
	}

	/**
	 * Within the JOP SCJ version we use a main method instead of a command line
	 * parameter or configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Terminal.getTerminal().writeln("The crankshaft example with an active clock.");
		single = new CrankShaft();
		JopSystem.startMission(single);
	}

}
