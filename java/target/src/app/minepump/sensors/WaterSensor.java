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

import minepump.sensors.Sensor;

public abstract class WaterSensor extends Sensor {

	protected int consecutiveReadingsTrigger;
	protected int consecutiveReadingsObserved;

	public WaterSensor(int sensorId, int consecReadingsTrigger) {
		super(sensorId);
		this.consecutiveReadingsTrigger = consecReadingsTrigger;
		this.consecutiveReadingsObserved = 0;
	}

	abstract public boolean criticalWaterLevel();
}
 
