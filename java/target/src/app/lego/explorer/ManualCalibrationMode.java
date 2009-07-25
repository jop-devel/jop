package lego.explorer;

import lego.lib.Sensors;

/**
 * Manual Calibration:
 * LEDS: 1 = IR, 2 = ?, 3 = ?, 4 = ?
 * B1: Start Calibrartion
 * B2: Next Calibration
 * B3: Finished Calibration
 * B4: Emergency (in every mode)
 * -------------------------------------
 * IR Calibration
 * LEDS: 1 is blinking
 * B1: Record Low
 * B2: Record High
 * B3: Finish Calibration
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ManualCalibrationMode implements BotMode {
	public static class IrCalibrationMode implements BotMode {
		public int treshold;

		private static final int IR_RECORD_LOW=0, IR_RECORD_HIGH=1,IR_RECORD_DONE=2;
		private int maxLow,minHigh;
		private BotInterface iface;
		private static final IrCalibrationMode inst = new IrCalibrationMode();
		private IrCalibrationMode() {}
		public static IrCalibrationMode start(BotInterface iface) {
			 inst.iface = iface;
			 inst.maxLow = Sensors.MIN_VALUE; 
			 inst.minHigh = Sensors.MAX_VALUE;
			 return inst;
		}
		public boolean driver(boolean stop) {
			if(stop) return false;
			boolean[] btnPressed = iface.buttonEdge;
			int sens = iface.irSensor.value;
			if(btnPressed[IR_RECORD_DONE]) {
				treshold = (maxLow + minHigh) >>> 1;
				iface.irSensor.setTreshold(treshold);
				return false;
			} else if(btnPressed[IR_RECORD_LOW]) {
				maxLow = (sens > maxLow) ? sens : maxLow;  
			} else if(btnPressed[IR_RECORD_HIGH]) {
				minHigh = (sens > maxLow && sens < minHigh) ? sens : minHigh;
			}
			return true;
		}		
	}

	private BotInterface iface;
	private static ManualCalibrationMode inst = new ManualCalibrationMode();
	private ManualCalibrationMode() {}

	private BotMode calibDriver;

	public static ManualCalibrationMode start(BotInterface iface) {
		inst.iface = iface;
		inst.calibDriver = IrCalibrationMode.start(iface);
		return inst;
	}

	public boolean driver(boolean stop) {
		iface.ledStatus[2] = BotInterface.LED_STATUS_BLINK;
		if(calibDriver != null) {
			boolean active = calibDriver.driver(stop);
			if(! active) calibDriver = null;
			else         return true;
		}
		// Insert logic for more calibration modes here
		if(stop || calibDriver == null) {			
			iface.ledStatus[2] = BotInterface.LED_STATUS_OFF;
			return false;
		} else {
			return true;
		}
	}


}
