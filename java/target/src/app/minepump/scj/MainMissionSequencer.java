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
package minepump.scj;



import javax.realtime.PriorityParameters;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.StorageParameters;


public class MainMissionSequencer extends MissionSequencer {
    public MainMissionSequencer(PriorityParameters priority,
			StorageParameters storage) {
		super(priority, storage);
	}

	public Mission getNextMission() {
        return new MainMission();
    }
}
