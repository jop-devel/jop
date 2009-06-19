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

package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class DspioFactory extends IOFactory {

	private SerialPort usb;
	private OutPort oport;

	// Handles should be the first static fields!
	private static int USB_PTR;
	private static int USB_MTAB;
	private static int OUT_PRT;
	private static int OUT_MTAB;

	DspioFactory() {
		usb = (SerialPort) makeHWObject(new SerialPort(),
				Const.IO_USB, 0);
		oport = (OutPort) makeHWObject(new OutPort(),
				Const.IO_DSPIO_OUT, 1);

	};
	// that has to be overridden by each sub class to get
	// the correct cp
	private static Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	
	static DspioFactory single = new DspioFactory();
	
	public static DspioFactory getDspioFactory() {		
		return single;
	}

	public SerialPort getUsbPort() { return usb; }
	
	public OutPort getOutPort() { return oport; }

}
