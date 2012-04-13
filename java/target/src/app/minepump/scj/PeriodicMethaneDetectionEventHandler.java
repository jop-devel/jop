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
import javax.realtime.PeriodicParameters;

import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import minepump.actuators.WaterpumpActuator;
import minepump.sensors.MethaneSensor;


public class PeriodicMethaneDetectionEventHandler extends PeriodicEventHandler
{
	protected MethaneSensor methaneSensor;
	protected WaterpumpActuator waterpumpActuator;
	
	public PeriodicMethaneDetectionEventHandler(PriorityParameters priority, 
	                                        PeriodicParameters parameters,
	                                        StorageParameters storage,
	                                        int memorySize,
	                                        MethaneSensor methaneSensor,
	                                        WaterpumpActuator waterpumpActuator)
	{
		super(priority, parameters, storage, memorySize);
		this.methaneSensor = methaneSensor;
		this.waterpumpActuator = waterpumpActuator;
	}

	public void handleAsyncEvent() 
	{		
		if (methaneSensor.isCriticalMethaneLevelReached()) {
	      	waterpumpActuator.emergencyStop(true);
		}
		else {
			waterpumpActuator.emergencyStop(false);
		}
	}
}
