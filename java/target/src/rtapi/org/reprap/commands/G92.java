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
import org.reprap.CommandController;
import org.reprap.HostController;
import org.reprap.Parameter;
import org.reprap.RepRapController;

//Set Position
public class G92 extends Command
{
	private RepRapController repRapController;
	private Parameter parameters = new Parameter();
	
	public G92(HostController hostController, CommandController commandController, RepRapController repRapController) 
	{
		super(hostController, commandController);
		this.repRapController = repRapController;
	}
	
	public boolean enqueue(Parameter parameters) 
	{
		this.parameters.X = parameters.X;
		this.parameters.Y = parameters.Y;
		this.parameters.Z = parameters.Z;
		this.parameters.E = parameters.E;
		return super.enqueue();
	}
	
	@Override
	public boolean execute() 
	{
		repRapController.setPosition(parameters);
		return true;
	}
}
