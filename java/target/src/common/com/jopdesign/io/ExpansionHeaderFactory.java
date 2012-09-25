/*
  Copyright (C) 2012, Tórur Biskopstø Strøm (torur.strom@gmail.com)

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

public class ExpansionHeaderFactory extends IOFactory
{
	private ExpansionHeader EH;

	// Handles should be the first static fields!
	private static int EH_PTR;
	private static int EH_MTAB;

	ExpansionHeaderFactory() {
		EH = (ExpansionHeader) makeHWObject(new ExpansionHeader(),
				Const.EH_BASE, 0);

	};
	// that has to be overridden by each sub class to get
	// the correct cp
	private static Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	
	static ExpansionHeaderFactory single = new ExpansionHeaderFactory();
	
	public static ExpansionHeaderFactory getExpansionHeaderFactory() {		
		return single;
	}

	public ExpansionHeader getExpansionHeader() { return EH; }

}
