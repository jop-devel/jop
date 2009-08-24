package lego.explorer;

import lego.lib.Sensors;

public class IRSensor {
	public int value = -1;
	public boolean isHigh = false;
	public int treshold = 179;

	private int sensorID;
	
	public IRSensor(int sensorID) {
		this.sensorID = sensorID;
	}

	public void updateSensor() {
		value = Sensors.readSensor(sensorID);
		isHigh = value >= treshold;
	}
	
	public void setTreshold(int treshold) {
		this.treshold = treshold;
	}
	
	public void dump() {
		System.out.print("IR Sensor: ");
		System.out.print(value);
		System.out.print(" = ");
		System.out.print(isHigh);
		System.out.print(" | treshold ");
		System.out.println(treshold);		
	}
}
