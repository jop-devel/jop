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
package minepump.sensors;

import minepump.sensors.WaterSensor;

public class LowWaterSensor extends WaterSensor {
	
	public LowWaterSensor(int sensorId, int consecNoWaterReadingsTrigger) {
		super(sensorId, consecNoWaterReadingsTrigger);
	}
	
	public boolean criticalWaterLevel() {
		int sensorReading = conductMeasurement();
		if(sensorReading > super.NO_BRICK_PRESENT ) {
			super.consecutiveReadingsObserved = 0;
			return false;
		}
		else super.consecutiveReadingsObserved++;

		if(super.consecutiveReadingsObserved >= super.consecutiveReadingsTrigger) return true;
		
		else return false;
	}
	
}
