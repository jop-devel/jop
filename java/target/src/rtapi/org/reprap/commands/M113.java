/*
  Copyright (C) 2012, T�rur Biskopst� Str�m (torur.strom@gmail.com)

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
package org.reprap.commands;

import org.reprap.Command;

public class M113 extends Command
{
	private static M113 instance = new M113();//Unbuffered command so only single instance
	
	public static boolean enqueue()
	{
		Command.enqueue(instance);
		return true;
	}
	
	@Override
	public boolean execute() 
	{
		//This command is supposed to set the extruder PWM
		return true;
	}
}
