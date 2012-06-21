/*******************************************************************************
 * Copyright (c) 2010
 *     Andreas Engelbredt Dalsgaard
 *     Casper Jensen 
 *     Christian Frost
 *     Kasper Søe Luckow.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Andreas Engelbredt Dalsgaard <andreas.dalsgaard@gmail.com> - Changes to run on  jop SCJ implementation
 *     Casper Jensen <semadk@gmail.com> - Initial implementation
 *     Christian Frost <thecfrost@gmail.com> - Initial implementation
 *     Kasper Søe Luckow <luckow@cs.aau.dk> - Initial implementation
 ******************************************************************************/
package minepump;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;

import javax.safetycritical.JopSystem;
import javax.safetycritical.Safelet;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.StorageParameters;

import minepump.scj.PeriodicMethaneDetectionEventHandler;
import minepump.scj.MainMissionSequencer;
import minepump.scj.MainMission;
/** 
 * Open questions
 *
 * Use of SCJAllowed, should all usable classes be annotated with it?
 * - Do we import classes from both scj and java.realtime namepsaces?
 *
 * Should we refactor our configuration?
 *
 * Determine the mission memory size and set it in MainMission.java
 */

public class Minepump implements Safelet {
	
	public void setUp() {}
	
    public void tearDown() {}

    public MissionSequencer getSequencer() {
    	StorageParameters sp =
            new StorageParameters(100000L, null); // TODO: Set memory size
        return new MainMissionSequencer(new PriorityParameters(10), sp);
    }

	public static void main(String[] args) {
		// Implementation specific registration of safelet
		// The setUp function is then invoked on the safelet, continued by a call to getSequencer.
		// The Sequencer is then responsible for running the missions, when finished, the tearDown
		// method is invoked and the app is terminated.
		JopSystem.startMission(new Minepump());
	}

	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 1000;
	}

}
