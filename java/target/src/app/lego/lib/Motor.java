/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

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

package lego.lib;

import com.jopdesign.sys.*;

/**
 * Motor steering (Motor 0-2) and back-EMF motor speed measurement 
 * (only Motor 0-1 due to pin constraints).
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 */
public class Motor {

	public static final int[] IO_OUTPUT_MOTOR = 
	{ Const.IO_LEGO+1, Const.IO_LEGO+2, Const.IO_LEGO+3 };

	public static final int[] IO_INPUT_MOTOR = 
	{ Const.IO_LEGO+2, Const.IO_LEGO+3 };
	
	public static final int[] IO_SYNCHRONIZED_INPUT_MOTOR =
	{ Const.IO_LEGO+8, Const.IO_LEGO+9 };

	/**
	 * Motor turned off.
	 */
	public static final int STATE_OFF = 0;
	/**
	 * Motor turns "forward".
	 */
	public static final int STATE_FORWARD = 1;
	/**
	 * Motor turns "backward".
	 */
	public static final int STATE_BACKWARD = 2;
	/**
	 * Brakes the motor.
	 */
	public static final int STATE_BRAKE = 3;

	protected static final int MASK_STATE = 0x3;
	protected static final int MASK_DUTYCYCLE = 0x3FFF;
	protected static final int MASK_MEASURE = 0x1;

	protected static final int OFFSET_DUTYCYCLE = 2;
	protected static final int OFFSET_MEASURE = 2 + 14;

	protected static final int OFFSET_BACKEMF1 = 9;
	protected static final int MASK_BACKEMF = 0x1ff;

	protected static int[] readValue = new int[2];
	protected static int[] writeValue = new int[3];
	
	/**
	 * Index of the Motor this instance steers.
	 */
	protected int index;	

	/**
	 * Maximum duty cycle value. 
	 */
	public static final int MAX_DUTYCYCLE = MASK_DUTYCYCLE;

	protected static final int DUTYCYCLE_PERCENTAGE_FACTOR = (MAX_DUTYCYCLE * 65536) / 100;
	protected static final int DUTYCYCLE_PERCENTAGE_SHIFT = 16;
	
	/**
	 * Back-EMF value expected to be measured when motor is idle.  
	 */
	public static final int BACKEMF_IDLE_VALUE = 0x100;
	
	/**
	 * Index of the Motor this instance steers.
	 * @return 0-2.
	 */
	public int getIndex()
	{
		return index;
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
		Native.wr((state & MASK_STATE) | ((dutyCycle & MASK_DUTYCYCLE) << OFFSET_DUTYCYCLE) | (((measure?1:0) & MASK_MEASURE) << OFFSET_MEASURE), IO_OUTPUT_MOTOR[index]);
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
		Native.wr((state & MASK_STATE) | ((dutyCyclePercentageToDutyCycle(percentage) & MASK_DUTYCYCLE) << OFFSET_DUTYCYCLE) | (((measure?1:0) & MASK_MEASURE) << OFFSET_MEASURE), IO_OUTPUT_MOTOR[index]);
	}

	/**
	 * 
	 * @param index Index of the Motor this instance steers (0-2).
	 */
	public Motor(int index)
	{
		this.index = index;
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
		writeValue[index] = (state & MASK_STATE) | ((dutyCycle & MASK_DUTYCYCLE) << OFFSET_DUTYCYCLE) | (((measure?1:0) & MASK_MEASURE) << OFFSET_MEASURE); 
		Native.wr(writeValue[index], IO_OUTPUT_MOTOR[index]);
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
		writeValue[index] = (state & MASK_STATE) | ((dutyCyclePercentageToDutyCycle(percentage) & MASK_DUTYCYCLE) << OFFSET_DUTYCYCLE) | (((measure?1:0) & MASK_MEASURE) << OFFSET_MEASURE); 
		Native.wr(writeValue[index], IO_OUTPUT_MOTOR[index]);
	}

	/**
	 * 
	 * @param state {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 */
	public void setState(int state)
	{
		writeValue[index] = (state & MASK_STATE) | (writeValue[index] & ~MASK_STATE);
		Native.wr(writeValue[index], IO_OUTPUT_MOTOR[index]);
	}

	/**
	 * 
	 * @param dutyCycle 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public void setDutyCycle(int dutyCycle)
	{
		writeValue[index] = ((dutyCycle & MASK_DUTYCYCLE) << OFFSET_DUTYCYCLE) | (writeValue[index] & ~(MASK_DUTYCYCLE << OFFSET_DUTYCYCLE));
		Native.wr(writeValue[index], IO_OUTPUT_MOTOR[index]);
	}

	/**
	 * 
	 * @param percentage Overshooting is handled.
	 * @return Duty cycle value between 0 and {@linkplain #MAX_DUTYCYCLE}.
	 */
	public static int dutyCyclePercentageToDutyCycle(int percentage)
	{
		return ((Math.max(0, Math.min(percentage, 100)) * DUTYCYCLE_PERCENTAGE_FACTOR) >> DUTYCYCLE_PERCENTAGE_SHIFT);
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
		writeValue[index] = (((measure?1:0) & MASK_MEASURE) << OFFSET_MEASURE) | (writeValue[index] & ~(MASK_MEASURE << OFFSET_MEASURE));
		Native.wr(writeValue[index], IO_OUTPUT_MOTOR[index]);
	}

	/**
	 * 
	 * @return 0-{@linkplain #MAX_DUTYCYCLE}.
	 */
	public int getDutyCycle()
	{
		return (writeValue[index] >> OFFSET_DUTYCYCLE) & MASK_DUTYCYCLE;
	}
	
	/**
	 * 
	 * @return When true, back-EMF measurement is made when running forward or backward.
	 * This necessitates to stop the motor for short times (which is also made for motors without 
	 * back-EMF measurement capability).
	 */
	public boolean getMeasure()
	{
		return ((writeValue[index] >> OFFSET_MEASURE) & MASK_MEASURE) != 0;
	}
	
	/**
	 * 
	 * @return {@linkplain #STATE_OFF}, {@linkplain #STATE_FORWARD}, {@linkplain #STATE_BACKWARD} or {@linkplain #STATE_BRAKE}.
	 */
	public int getState()
	{
		return ((writeValue[index]) & MASK_STATE);
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
		int value = Native.rd(IO_INPUT_MOTOR[index]);
		return new int[] { value & MASK_BACKEMF, (value >> OFFSET_BACKEMF1) & MASK_BACKEMF };
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
		int[] raw = readBackEMF();
		return new int[] { raw[0]-BACKEMF_IDLE_VALUE, raw[1]-BACKEMF_IDLE_VALUE };
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
		emfData[0] = readValue[index] & MASK_BACKEMF;
		emfData[1] = (readValue[index] >> OFFSET_BACKEMF1) & MASK_BACKEMF;
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
		return new int[] { readValue[index] & MASK_BACKEMF, (readValue[index] >> OFFSET_BACKEMF1) & MASK_BACKEMF };	
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
		for (int i = 0; i < 2; i++)
			readValue[i] = Native.rd(IO_SYNCHRONIZED_INPUT_MOTOR[i]);		
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
		int[] raw = getSynchronizedBackEMF();
		return new int[] { raw[0]-BACKEMF_IDLE_VALUE, raw[1]-BACKEMF_IDLE_VALUE };		
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
		updateSynchronizedBackEMF(emfData);
		emfData[0] -= BACKEMF_IDLE_VALUE;
		emfData[1] -= BACKEMF_IDLE_VALUE;
	}

}
