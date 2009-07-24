package lego.explorer;

import lego.lib.Sensors;

public class IRSensor {
	public int value = -1;
	public boolean isHigh = false;
	public int treshold = 179;
	public boolean running;
	private int sensorID;
	public IRSensor(int sensorID) {
		this.sensorID = sensorID;
	}
	/** Self-calibrating update */
	public void updateSensor() {
		value = Sensors.readSensor(sensorID);
		isHigh = value >= treshold;
	}
	public void setTreshold(int treshold) {
		this.treshold = treshold;
	}
}
