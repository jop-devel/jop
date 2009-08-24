package lego.explorer;


import util.Timer;
import lego.lib.Buttons;
import lego.lib.DigitalInputs;
import lego.lib.Leds;

/** common I/O for explorer bots */
public class BotInterface {
	public static final int LED_STATUS_OFF = 0, LED_STATUS_BLINK = 1, LED_STATUS_ON = 2;

	/** time **/
	public static class TimeStamp {
		public int secs;
		public int usecs;
		public void sync(TimeStamp ts) {
			ts.secs = secs;
			ts.usecs = usecs;
		}
		// FIXME: inefficient
		public int msDiff(TimeStamp ts) {
			int msdiff = (secs - ts.secs) * 1000;
			msdiff += (usecs - ts.usecs) / 1000;
			return msdiff;
		}		
	}
	public TimeStamp time = new TimeStamp();
	private boolean timeInit = false;
	private int usecBase = 0;

	/* Board elements */
	
	/** IR Sensor */
	public IRSensor irSensor;

	/** Drive */
	public Drive drive;

	/** Digitial sensors */
	public boolean[] digitals = new boolean[3];

	/** LEDs */
	public int[] ledStatus = new int[Leds.LED_COUNT];

	/** Buttons */
	private boolean[] buttons = new boolean[4];
	public boolean[] buttonEdge = new boolean[4];

	
	public BotInterface(int irSensorId, int motorLeft, int motorRight) {
		this.irSensor = new IRSensor(irSensorId);
		this.drive = new Drive(motorLeft, motorRight);
	}
	
	/** For simplicity, this is for now a single write/read loop.
	 *  This is a little unrealistic, though. 
	 *  Needs to be update at least every 20ms.
	 *  
	 *  FIXME: I didn't use JOPs Timer implementation, because it is unclear how to get a synchronized
	 *  usec / sec pair. The subtraction trick is from there.
	 */
	public void update() {
		int currentUS = Timer.us(); // relative us
		if(! timeInit) {
			time.secs = 0;
			time.usecs = 0;
			usecBase = currentUS;
			timeInit = true;
		} else {
			int diff = currentUS - usecBase;
			time.usecs += diff;
			if(time.usecs >= 1000000) {
				time.usecs -= 1000000;
				time.secs++;
			}
			usecBase = currentUS;
		}
		// write
		drive.write();
		for(int i = 0; i < Leds.LED_COUNT; i++) {
			if(ledStatus[i] == LED_STATUS_OFF) Leds.setLed(i, false);
			else if(ledStatus[i] == LED_STATUS_ON) Leds.setLed(i, true);
			else Leds.blinkUpdate(i);
		}
		// read
		drive.read();
		irSensor.updateSensor();
		int buttonVal = Buttons.getButtons();
		for(int ix = 0; ix < 4; ix++) {
			boolean btnDown = ((buttonVal >> ix) & 1) != 0;
			if(btnDown && ! buttons[ix]) buttonEdge[ix] = true;
			else                         buttonEdge[ix] = false;
			buttons[ix] = btnDown;
		}
		int  digitalVal = DigitalInputs.getDigitalInputs();
		for(int ix = 0; ix < 3; ix++) {
			boolean digitalOn = ((digitalVal >> ix) & 1) != 1;
			digitals[ix] = digitalOn;
		}		
	}
	
	public void dump() {
		System.out.print("Interface State at time ");
		System.out.print(time.secs);
		System.out.print(".");
		System.out.println(time.usecs);
		irSensor.dump();
		
		System.out.print("Buttons ");
		for(int i = 0; i < 4; i ++) {
			System.out.print(i);
			System.out.print(" = ");
			System.out.print(buttons[i]);
			System.out.print(" | ");
		}
		
		System.out.print("\nDigitals ");
		for(int i = 0; i < 3; i ++) {
			System.out.print(i);
			System.out.print(" = ");
			System.out.print(digitals[i]);
			System.out.print(" | ");
		}
		System.out.println("");
		drive.dump();
	}
}
