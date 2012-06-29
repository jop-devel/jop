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
package org.reprap.commands;

import org.reprap.Command;
import org.reprap.Parameter;
import org.reprap.RepRapController;

//Set Position
public class G92 extends Command
{
	private static G92 instance = new G92();//Unbuffered command so only single instance
	
	private Parameter parameters = new Parameter();
	
	public static boolean enqueue(Parameter parameters)
	{
		instance.parameters.X = parameters.X;
		instance.parameters.Y = parameters.Y;
		instance.parameters.Z = parameters.Z;
		instance.parameters.E = parameters.E;
		return instance.addToQueue();
	}
	
	@Override
	public boolean execute() 
	{
		RepRapController.instance.setPosition(parameters);
		return true;
	}
}
