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

import minepump.legosim.lib.Sensors;

public class Sensor {
	
	// Calibrate constants!
	protected static final int NO_BRICK_PRESENT = 120;
	protected static final int WATER_SENSOR_RANGE_BEGIN = 132;
	protected static final int WATER_SENSOR_RANGE_END = 146;
	protected static final int METHANE_SENSOR_RANGE_BEGIN = 147;
	protected static final int METHANE_SENSOR_RANGE_END = 160;
	
	protected int sensorId;
	
	public Sensor(int sensorId) {
		this.sensorId = sensorId;
	}
	
	public int getSensorId() {
		return sensorId;
	}
	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}
	
	protected int conductMeasurement() {
		Sensors.synchronizedReadSensors();
		
		//this probably has to be debugged :) especially the mysterious bit shift....
		int sensorReading = Sensors.getBufferedSensor(this.sensorId)>> 1;
		return sensorReading;
	}

	protected boolean isBrickWater(int color) {
		return color >= WATER_SENSOR_RANGE_BEGIN && color <= WATER_SENSOR_RANGE_END;
	}
	
	protected boolean isBrickMethane(int color) {
		return color >= METHANE_SENSOR_RANGE_BEGIN && color <= METHANE_SENSOR_RANGE_END;
	}

	protected boolean isSensorReadingEnvironment(int color) {
		return !isBrickMethane(color) && !isBrickWater(color);
	}
}
