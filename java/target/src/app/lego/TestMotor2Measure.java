package lego;

import lego.lib.*;

public class TestMotor2Measure
{
	public static final int INTERVAL = 200;
	
	public static void main(String[] args)
	{
		Motor m2 = new Motor(2);
		
		while (true)
		{
			boolean measure = Buttons.getButtons() != 0;
			if (m2.getMeasure() != measure)				
				System.out.println("Setting: " + (measure ? "True" : "False"));
				m2.setMotor(Motor.STATE_FORWARD, measure, Motor.MAX_DUTYCYCLE);
		}
	}
}
