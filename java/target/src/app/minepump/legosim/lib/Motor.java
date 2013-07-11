/*******************************************************************************
 *
 *   This file is part of JOP, the Java Optimized Processor
 *     see <http://www.jopdesign.com/>
 * 
 *   Copyright (C) 2007, Peter Hilber and Alexander Dejaco
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Casper Jensen <semadk@gmail.com> - Changes for use as simulation
 *     Christian Frost <thecfrost@gmail.com> - Changes for use as simulation
 *     Kasper Søe Luckow <luckow@cs.aau.dk> - Changes for use as simulation
 *
 ******************************************************************************/

package minepump.legosim.lib;

/**
 * Motor steering (Motor 0-2) and back-EMF motor speed measurement 
 * (only Motor 0-1 due to pin constraints).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 */
public class Motor {

	/**
	 * Index of the Motor this instance steers.
	 * @return 0-2.
	 */
	public int getIndex()
	{
		return 0;
	}
	
	/**
	 * 
	 * @param index 0-2.
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 * @param measure When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability). 
	 * @param dutyCycle 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public static void setMotor(int index, int state, boolean measure, int dutyCycle)
	{
	
	}
	
	/**
	 * 
	 * @param index 0-2.
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 * @param measure When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability).
	 * @param percentage Overshooting is handled.
	 */
	public static void setMotorPercentage(int index, int state, boolean measure, int percentage)
	{
	
	}

	/**
	 * 
	 * @param index Index of the Motor this instance steers (0-2).
	 */
	public Motor(int index)
	{
	
	}

	/**
	 * 
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 * @param measure When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability). 
	 * @param dutyCycle 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public void setMotor(int state, boolean measure, int dutyCycle)
	{
	
	}
	
	/**
	 * 
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 * @param measure When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability).
	 * @param percentage Overshooting is handled.
	 */
	public void setMotorPercentage(int state, boolean measure, int percentage)
	{
	
	}

	/**
	 * 
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 */
	public void setState(int state)
	{
	
	}

	/**
	 * 
	 * @param dutyCycle 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public void setDutyCycle(int dutyCycle)
	{
	
	}

	/**
	 * 
	 * @param percentage Overshooting is handled.
	 * @return Duty cycle value between 0 and {@linkplain #MAX_DUTYCYCLE}.
	 */
	public static int dutyCyclePercentageToDutyCycle(int percentage)
	{
		return 0;
	}
	
	/**
	 * 
	 * @param percentage Overshooting is handled.
	 */
	public void setDutyCyclePercentage(int percentage)
	{
		setDutyCycle(dutyCyclePercentageToDutyCycle(percentage));
	}

	/**
	 * @param measure When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability).
	 */
	public void setMeasure(boolean measure)
	{
	
	}

	/**
	 * 
	 * @return 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public int getDutyCycle()
	{
		return 0;
	}
	
	/**
	 * 
	 * @return When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability).
	 */
	public boolean getMeasure()
	{
		return false;
	}
	
	/**
	 * 
	 * @return {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 */
	public int getState()
	{
		return 0;
	}
	
	/**
	 * Reads and returns the raw back-EMF values last measured by the ADC.
	 * Reading back-EMF values is only supported for Motor 0 and Motor 1.
	 * 
	 * @warning Allocates Memory, only use when garbage collector is active
	 * @return 9 bit ADC value. XXX typical range
	 */
	public int[] readBackEMF()
	{		
		return new int[] {};
	}
	
	/**
	 * Reads and returns the back-EMF values last measured by the ADC.
	 * Reading back-EMF values is only supported for Motor 0 and Motor 1.
	 * 
	 * @warning Allocates Memory, only use when garbage collector is active
	 * @return The values returned are the differences of the 9 bit ADC value and {@linkplain #BACKEMF_IDLE_VALUE}. 
	 */
	public int[] readNormalizedBackEMF()
	{	
		return new int[] {};
	}

	/**
	 * Returns the back-EMF values for the motor last read by an 
	 * invocation of the class method {@linkplain #synchronizedReadBackEMF()}.
	 * Reading back-EMF values is only supported for Motor 0 and Motor 1.
	 * @param emfData array of size 2 to hold the results
	 * @return 9 bit ADC value.
	 * @warning Allocates Memory, only use when garbage collector is active
	 */
	public void updateSynchronizedBackEMF(int[] emfData)
	{
	
	}

	/**
	 * Returns the back-EMF values for the motor last read by an 
	 * invocation of the class method {@linkplain #synchronizedReadBackEMF()}.
	 * 
	 * @warning Allocates Memory, only use when garbage collector is active
	 * @see updateSynchronizedBackEMF
	 * @return 9 bit ADC value.
	 */
	public int[] getSynchronizedBackEMF()
	{
		return new int[] {};	
	}

	/**
	 * Gets the back-EMF values last measured by the ADC.
	 * The Motor 0 and Motor 1 back-EMF values are guaranteed to be synchronized.
	 * <p>
	 * 9*4 bits for the 4 back-EMF values cannot be transferred in a single cycle.
	 * Therefore, the hardware won't update both values after the values for the 
	 * first motor have been read until the values for the second motor have been 
	 * read, too.
	 */
	public static /*synchronized*/ void synchronizedReadBackEMF()	// TODO make synchronized?
	{		
			
	}

	/**
	 * Gets the back-EMF values last measured by the ADC.
	 * The Motor 0 and Motor 1 back-EMF values are guaranteed to be synchronized.
	 * <p>
	 * 9*4 bits for the 4 back-EMF values cannot be transferred in a single cycle.
	 * Therefore, the hardware won't update both values after the values for the 
	 * first motor have been read until the values for the second motor have been 
	 * read, too.
	 * 
	 * @warning Allocates Memory, only use when garbage collector is active
	 * @return The values returned are the differences of the 9 bit ADC value and {@linkplain #BACKEMF_IDLE_VALUE}.
	 */
	public int[] getSynchronizedNormalizedBackEMF()
	{	
		return new int[] {};		
	}

	/** Gets the back-EMF values last measured by the ADC.
	 * The Motor 0 and Motor 1 back-EMF values are guaranteed to be synchronized.
	 * <p>
	 * 9*4 bits for the 4 back-EMF values cannot be transferred in a single cycle.
	 * Therefore, the hardware won't update both values after the values for the 
	 * first motor have been read until the values for the second motor have been 
	 * read, too.
	 */
	public void updateSynchronizedNormalizedBackEMF(int[] emfData) {		
	
	}

}
