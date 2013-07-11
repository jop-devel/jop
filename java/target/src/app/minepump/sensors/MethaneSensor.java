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

import minepump.sensors.Bricks;
import minepump.sensors.Sensor;

public class MethaneSensor extends Sensor {
	
	private float criticalMethaneLevel;
	private MeasurementHistory mHistory;
	private boolean detectBrick = true;
	
	public MethaneSensor(int sensorId, int criticalMethaneLevel, int historySize) {
		super(sensorId);
		
		this.criticalMethaneLevel = criticalMethaneLevel;
		this.mHistory = new MeasurementHistory(historySize);
	}
	
	// THE FOLLOWING CODE IS USED IN minepump.tex !!!!
	public boolean isCriticalMethaneLevelReached() {
		int sensorReading = conductMeasurement();
		//System.out.println("Methaneensor: " + sensorReading);
		if(super.isBrickMethane(sensorReading) && detectBrick == true)	
		{
			mHistory.addMeasurement(Bricks.GAS);
			detectBrick = false;
			//System.out.println("GAS from methanesensor: " + sensorReading);
		}
			
		else if(super.isBrickWater(sensorReading) && detectBrick == true)
		{
			mHistory.addMeasurement(Bricks.WATER);
			detectBrick = false;
			//System.out.println("WATER from methanesensor: " + sensorReading);
		}
		else
		{
			detectBrick = true;
		}
		return mHistory.getMethaneLevel() >= this.criticalMethaneLevel;
	}
	// THE PREVIOUS CODE IS USED IN minepump.tex !!!!

	
	private class MeasurementHistory {
		private int INSERT_POINT = 0;
		private Bricks[] history;
		private int maxSize;
		
		public MeasurementHistory(int maxSize) {
			this.history = new Bricks[maxSize];
			this.maxSize = maxSize;
			
			// Initialize to WATER as we are only tracking Methane
			for (int iter = 0; iter < this.maxSize; iter++) 
				this.history[iter] = Bricks.WATER;
			
		}
		
		public void addMeasurement(Bricks brick) {			   
			this.history[this.INSERT_POINT] = brick;
			this.INSERT_POINT++;
			if (this.INSERT_POINT==history.length) {
				this.INSERT_POINT = 0;
			}
		}
		
		public float getMethaneLevel() {
			int methaneCount = 0;
			
			for (int iter = 0; iter < this.maxSize; iter++) { // @WCA loop<=10			
				if (this.history[iter] == Bricks.GAS) methaneCount++;			
			}
			
			return methaneCount;
		}
	}

}
