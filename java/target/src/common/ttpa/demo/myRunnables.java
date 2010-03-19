/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
    
  Copyright (C) 2010, Thomas Hassler, Lukas Marx

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


/**
 * @author Thomas Hassler	e0425918@student.tuwien.ac.at
 * @author Lukas Marx	lukas.marx@gmail.com
 * @version 1.0
 */

package ttpa.demo;

import com.jopdesign.io.DspioFactory;
import com.jopdesign.io.SysDevice;

/**
 * lets the LED blink with the desired frequency
 */
public class myRunnables implements Runnable
{
	DspioFactory fact = DspioFactory.getDspioFactory();
	SysDevice sys = fact.getSysDevice();
	
	private boolean blink;
	private int rate;			// trim value from MC
	private int run = 1;		// switch value from MC
	private int count = 0;
	
	public void run()
	{
		count++;
		rate = UserRodl.node.getIoArrayPos(0);		// read the rate
		run = UserRodl.node.getIoArrayPos(1);		// read the blinking status
		System.out.println(rate);
		System.out.println(run);

		// blinking is activated
		if (run == 2)
		{
			if ((count % rate) == 0)		// calculate blinking frequency
			{
				if (blink)
				{
					sys.wd = 1;
					blink = false;
				}
				else
				{
					sys.wd = 0;
					blink = true;
				}
			}
		}
		// blinking is deactivated
		else 
		{
			sys.wd = 0;
		}
	}
}

/**
 * calculated value to send to the MC
 */
class exec2 implements Runnable
{
	private int slow = 0;		// to slow down blinking
	public static byte myExec = 1;

	public void run()
	{
		slow++;
		
		if ((slow % 3) == 0)	// slow down blinking
		{
			// send values 0, 1, 2, 3
			Appl.countExec2 = (byte) ((Appl.countExec2 + 1) % 4);
			UserRodl.setVariableValues();		// set the variable in the IO array
		}
	}
}
