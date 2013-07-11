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
import org.reprap.RepRapController;


public class M105 extends Command
{
	private static final char[] T = {'T',':'};
	private static final char[] B = {' ','B',':','5','0'};
	
	private RepRapController repRapController;
	
	public M105(HostController hostController, CommandController commandController, RepRapController repRapController) 
	{
		super(hostController, commandController);
		this.repRapController = repRapController;
	}
	
	@Override
	public boolean execute() 
	{
		//This command is supposed to return the extruder and bed temperatures
		return true;
	}
	
	@Override
	public void respond() 
	{
		hostController.confirmCommand(T,HostController.intToChar(repRapController.getCurrentTemperature()),B);
	}
}
