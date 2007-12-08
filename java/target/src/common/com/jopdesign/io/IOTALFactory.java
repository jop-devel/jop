package com.jopdesign.io;
/**
 * Not used - just the paper example
 * @author martin
 *
 */

public class IOTALFactory extends IOMinFactory {
	
	private TALPins pins;
	private LEDPort leds;
	private Ethernet cs8900;
	
	IOTALFactory() {
		super();
		pins = (TALPins) JVMIOMagic(0);
		leds = (LEDPort) JVMIOMagic(0);
		cs8900 = (Ethernet) JVMIOMagic(0);
	}
	
	static IOTALFactory single = new IOTALFactory();
	
	static IOTALFactory getFectory() { return single; }
	
	public TALPins getPins() { return pins; }
	public LEDPort getLEDs() { return leds; }
	public Ethernet getEthernet() {return cs8900; }

}
