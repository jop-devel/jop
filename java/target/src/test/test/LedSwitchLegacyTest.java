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

package test;

import com.jopdesign.sys.*;
import util.*;

public class LedSwitchLegacyTest {

	public static void main(String[] args)
	{
		while(true)
		{
			// Wait 5 sek
			Timer.wd();
			int i = Timer.getTimeoutMs(5000);
			while (!Timer.timeout(i));
			
			// IO
			int val;
			val = Native.rdMem(Const.LS_BASE);
			System.out.println("Switches = '" + val + "'");
			Native.wrMem(val,Const.LS_BASE);
		}
	}
}
