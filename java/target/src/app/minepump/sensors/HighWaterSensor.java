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

public class HighWaterSensor extends WaterSensor {

	//calibrate me plz
	private static final int CURRENT_WATER_COLOR_THRESHOLD = 50;

	private int currentWaterColor;
	
	public HighWaterSensor(int sensorId, int consecWaterReadingsTrigger) {
		super(sensorId, consecWaterReadingsTrigger);
		this.currentWaterColor = 0;
	}
	
	public boolean criticalWaterLevel() {
		int sensorReading = conductMeasurement();

		if(sensorReading > super.NO_BRICK_PRESENT){
			if(super.consecutiveReadingsObserved == 0) {
				this.currentWaterColor = sensorReading;
				super.consecutiveReadingsObserved++;
			}
			else if(sensorReading <= this.currentWaterColor + CURRENT_WATER_COLOR_THRESHOLD && 
			   sensorReading >= this.currentWaterColor - CURRENT_WATER_COLOR_THRESHOLD) {
				super.consecutiveReadingsObserved++;
			}
			else
				super.consecutiveReadingsObserved = 0;			
		}
		else
			super.consecutiveReadingsObserved = 0;
		if(super.consecutiveReadingsObserved >= super.consecutiveReadingsTrigger) {
			super.consecutiveReadingsObserved = 0;
			return true;
		}
		else return false;
	}
	
}
