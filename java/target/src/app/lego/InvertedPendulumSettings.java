package lego;

import lego.lib.Sensors;

public class InvertedPendulumSettings
{
	static final int TILT_SENSOR0 = 0;
	static final int TILT_SENSOR1 = 1;

	static final int MOTOR0 = 0;
	static final int MOTOR1 = 1;
	
	static final int TILT_SENSOR_BASE_VALUE = 290;
	
	static final int TILT_SENSOR_BALANCED_VALUE = 48;
	//static final int TILT_SENSOR_BALANCED_VALUE = 45; // too much forward	
	//static final int TILT_SENSOR_BALANCED_VALUE = 55; // too much backward 
	
	static final int getTiltSensorTousandth()
	{
		throw new RuntimeException();
	}
	
	protected static int getAngle()
	{
		return 
		-(
		Sensors.readSensor(InvertedPendulumSettings.TILT_SENSOR0)-InvertedPendulumSettings.TILT_SENSOR_BASE_VALUE - InvertedPendulumSettings.TILT_SENSOR_BALANCED_VALUE
		); 
	}
}
