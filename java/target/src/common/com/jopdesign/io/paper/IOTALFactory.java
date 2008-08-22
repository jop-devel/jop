/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.io.paper;

import com.jopdesign.io.Ethernet;

public class IOTALFactory extends BaseBoard {
	
	private TALPins pins;
	private LEDPort leds;
	private Ethernet cs8900;
	
	IOTALFactory() {
		super();
		pins = (TALPins) jvmHWOCreate(0);
		leds = (LEDPort) jvmHWOCreate(0);
		cs8900 = (Ethernet) jvmHWOCreate(0);
	}
	
	static IOTALFactory single = new IOTALFactory();
	
	public static IOTALFactory getFactory() { return single; }
	
	public TALPins getPins() { return pins; }
	public LEDPort getLEDs() { return leds; }
	public Ethernet getEthernet() {return cs8900; }

}
