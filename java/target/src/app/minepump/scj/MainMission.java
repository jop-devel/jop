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

import javax.realtime.RelativeTime;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.realtime.PeriodicParameters;

import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;


import minepump.actuators.GenericActuator;
import minepump.actuators.WaterpumpActuator;
import minepump.scj.PeriodicMethaneDetectionEventHandler;
import minepump.scj.PeriodicWaterLevelDetectionEventHandler;
import minepump.sensors.HighWaterSensor;
import minepump.sensors.LowWaterSensor;
import minepump.sensors.MethaneSensor;


public class MainMission extends Mission {
    private static final int PERIODIC_GAS_PERIOD = 56;
   	private static final int PERIODIC_WATER_PERIOD = 40;
   	private static final int SPORADIC_WATER_PERIOD = 40;

	private static final int GAS_PRIORITY = 10;
	private static final int WATER_PRIORITY = 10;
   	
   	private static final int ACTUATOR_ID_WATERPUMP = 0;
   	private static final int ACTUATOR_ID_ENVIRONMENT = 1;

	private static final int SENSOR_ID_METHANE = 0;
	private static final int SENSOR_ID_HIGH_WATER = 1;
	private static final int SENSOR_ID_LOW_WATER = 2;
    
	
    // TODO set mission memory size
    public long missionMemorySize() { return 10000; }
    
    public static void MyInitialize()
    {
    	new MainMission().initialize();
    }
    
	protected void initialize() {

		// Actuators 
		WaterpumpActuator waterpumpActuator = new WaterpumpActuator(ACTUATOR_ID_WATERPUMP);
		GenericActuator environmentActuators = new GenericActuator(ACTUATOR_ID_ENVIRONMENT);

		// Sensors
		int criticalMethaneLevel = 2;
		int brickHistorySize = 10;
		MethaneSensor methaneSensor = new MethaneSensor(SENSOR_ID_METHANE, criticalMethaneLevel, brickHistorySize);
		
		int consecutiveNoWaterReadings = 3;
		int consecutiveHighWaterReadings = 3;
		HighWaterSensor highWaterSensor = new HighWaterSensor(SENSOR_ID_HIGH_WATER, consecutiveHighWaterReadings);
		LowWaterSensor lowWaterSensor = new LowWaterSensor(SENSOR_ID_LOW_WATER, consecutiveNoWaterReadings);

		// Methane
		PeriodicMethaneDetectionEventHandler methane = new PeriodicMethaneDetectionEventHandler(
			new PriorityParameters(11),
			new PeriodicParameters(new RelativeTime(0,0), new RelativeTime(PERIODIC_GAS_PERIOD,0)),
			new StorageParameters(100000L, null), // TODO update memory size
			10000, // Set the size of the private scoped memory area for this event handler
			methaneSensor, waterpumpActuator);
		methane.register();		
		
		// Water
		PeriodicWaterLevelDetectionEventHandler water = new PeriodicWaterLevelDetectionEventHandler(
			new PriorityParameters(11),
			new PeriodicParameters(new RelativeTime(0,0), new RelativeTime(PERIODIC_WATER_PERIOD,0)),
			new StorageParameters(100000L, null), // TODO update memory size
			10000, // Set the size of the private scoped memory area for this event handler			
			highWaterSensor,
			lowWaterSensor,
			waterpumpActuator);			
		water.register();	

		// init system
		environmentActuators.start();
		
	}
}
