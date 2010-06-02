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

public class CarModel extends Thread {

	volatile long timestep = 0;

	public final static double TIME_SCALE = 0.005; // seconds

	final static double GRAVITY = 9.81; // meters per queare seconds
	final static double AIR_DENSITY = 1.2; // kilograms per cubic meter

	final static double REFERENCE_AREA = 2.25; // square meters
	final static double CD_FACTOR = 0.33; // air drag coefficient
	final static double CR_FACTOR = 0.025; // surface drag coefficient

	final static double MASS = 1550.0; // kilograms

	final static double MAX_ENGINE_FORCE = 8000.0; // cap maximum force (just a guess, accelerating with 0.5G)
	final static double MAX_ENGINE_POWER = 100*1000.0; // kilowatts
	final static double CLUTCH_LIMIT = 4.0; // upper limit for clutch in meters per second
	private double throttle = 0.0; // 0..1

	final static double MAX_BRAKE_FORCE = 15000.0; // kilonewton (just a guess, braking with 1.0G)
	private double brake = 0.0; // 0..1

	private double incline = 0.0; // road steepness, -0.5..0.5, more than that would probably flip the car...
	private double targetSpeed = 0.0; // just for debugging

	private double speed = 0.0; // meters per second
	private double distance = 0.0; // meters
	private double acceleration = 0.0; // meters per square second

	public void setThrottle(double t) {
		// limit throttle to valid range
		throttle = Math.max(0.0, Math.min(t, 1.0));
		// System.err.println("new throttle: "+throttle);
	}

	public void setBrake(double b) {
		// limit brake to valid range
		brake = Math.max(0.0, Math.min(b, 1.0));
		// System.err.println("new brake: "+brake);
	}

	public void setIncline(double d) {
		// limit incline to valid range
		incline = Math.max(-0.5, Math.min(d, 0.5));		
	}

	public void setTargetSpeed(double t) {
		// limit incline to valid range
		targetSpeed = t;
	}

	public double getSpeed() {
		return speed;
	}

	double windForce() {
		// drag rises with the square of the speed
		return 0.5*(speed*speed)*AIR_DENSITY*CD_FACTOR*REFERENCE_AREA;
	}

	double rollForce() {
		int direction = Double.compare(speed, 0.0);
		// mass times gravitational force
		if (direction > 0) {
			return CR_FACTOR*MASS*GRAVITY;
		} else if (direction < 0) {
			return -CR_FACTOR*MASS*GRAVITY;
		}
		// no roll force if we don't roll
		return 0.0;
	}

	double gravityForce() {
		return MASS*GRAVITY*incline;
	}

	double clutchFactor() {
		if (Double.compare(Math.abs(speed), CLUTCH_LIMIT) < 0) {
			return 1.0/(1.0+CLUTCH_LIMIT-Math.abs(speed));
		}
		return 1.0;
	}

	double engineForce() {
		// limit maximum force from the engine
		if (Double.compare(speed, 0.0) == 0) {
			if (Double.compare(throttle, 0.0) > 0) {
				return MAX_ENGINE_FORCE;
			} else {
				return 0.0;
			}			
		}
		return Math.min(MAX_ENGINE_FORCE, MAX_ENGINE_POWER*clutchFactor()*throttle/Math.abs(speed));
	}

	double brakeForce() {
		// the force of the brake depends on the current direction
		int direction = Double.compare(speed, 0.0);
		if (direction > 0) {
			return brake*MAX_BRAKE_FORCE;
		} else if (direction < 0) {
			return -brake*MAX_BRAKE_FORCE;
		} else {
			// if we don't move, we work only against gravity
			if (Double.compare(brake, 0.0) > 0) {
				// limit the force against gravity with actually applied brake value
				return Math.min(Math.max(-brake*MAX_BRAKE_FORCE, -gravityForce()), brake*MAX_BRAKE_FORCE);
			} else {
				return 0.0;
			}
		}
	}

	public void run() {
		for (timestep = 0;; timestep++) {

			long startTime = System.currentTimeMillis();

			// acceleration = \Delta speed = force / mass
			double acc = (engineForce()-brakeForce()-windForce()-rollForce()-gravityForce());
			acceleration = acc / MASS;

			// System.err.println(speed*3.6+" "+acceleration+" "+engineForce()+" "+brakeForce()+" "+windForce()+" "+rollForce()+" "+gravityForce());
			
			if (Double.compare(speed, 0.0) > 0) {
				speed = Math.max(speed+acceleration*TIME_SCALE, 0.0);
			} else if (Double.compare(speed, 0.0) < 0) {
				speed = Math.min(speed+acceleration*TIME_SCALE, 0.0);
			} else {
				speed += acceleration*TIME_SCALE;
			}

			// just for checking how far we've gone yet
			distance += speed * TIME_SCALE;

			// debugging output
			System.err.println(timestep*TIME_SCALE+" "+speed*3.6+" "+distance+" "+acceleration+" "+incline+" "+targetSpeed);

			long elapsedTime = System.currentTimeMillis() - startTime;

			try {
				if (elapsedTime < TIME_SCALE*1000) {
					sleep((int)(TIME_SCALE*1000-elapsedTime));
				}
			} catch (InterruptedException exc) {
				// ignore
			}
		}
	}

}