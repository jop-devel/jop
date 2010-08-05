/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Wolfgang Puffitsch <wpuffits@mail.tuwien.ac.at>

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

package cruiser.model;

import cruiser.common.*;

public class TrackGenerator extends Thread {

	private int generatorStep = 0;
	private CarModel car;

	public TrackGenerator(CarModel car) {
		this.car = car;
	}

	public void run() {
		for (;;) {
			
			long startTime = System.currentTimeMillis();

			double currentTime = car.timestep*CarModel.TIME_SCALE;
			GeneratorItem currentItem = generatorList[generatorStep];

			// check whether we should send a new message
			if (Double.compare(currentTime, currentItem.time) >= 0) {
				car.setIncline(currentItem.incline);
				car.setTargetSpeed(currentItem.targetSpeed);
				WireMessage msg = new WireTargetSpeedMessage(WireMessage.Type.TARGET_SPEED,
															 (short)(currentItem.targetSpeed/3.6*100),
															 (short)(currentItem.targetDistance*10));
				System.out.print(msg.toString());
				generatorStep++;
			}

			// run until all items are consumed
			if (generatorStep >= generatorList.length) {
				break;
			}

			long elapsedTime = System.currentTimeMillis() - startTime;

			try {
				if (elapsedTime < CarModel.TIME_SCALE*1000) {
					sleep((int)(CarModel.TIME_SCALE*1000-elapsedTime));
				}
			} catch (InterruptedException exc) {
				// ignore
			}
		}
	}

	private class GeneratorItem {
		final double time; // seconds
		final double targetSpeed; // km/h
		final double targetDistance; // meters
		final double incline;
		GeneratorItem(double time, double targetSpeed, double targetDistance, double incline) {
			this.time = time;
			this.targetSpeed = targetSpeed;
			this.targetDistance = targetDistance;
			this.incline = incline;
		}
	}

	private final GeneratorItem [] generatorList = {
		new GeneratorItem(0.0, 100.0, 100.0, 0.0),
		new GeneratorItem(10.0, 50.0, 150.0, 0.05),
		new GeneratorItem(20.0, 25.0, 100.0, -0.15),
		new GeneratorItem(30.0, 100.0, 0.0, 0.01),
		new GeneratorItem(40.0, 0.0, 100.0, -0.15),
		new GeneratorItem(60.0, 150.0, 500.0, 0.25),
		new GeneratorItem(70.0, 150.0, 500.0, 0.0),
		new GeneratorItem(90.0, 0.0, 85.0, 0.0),
		new GeneratorItem(100.0, 5.0, 5.0, 0.30),
		new GeneratorItem(130.0, 5.0, 5.0, -0.30),
		new GeneratorItem(160.0, 5.0, 5.0, 0.00)
	};

}