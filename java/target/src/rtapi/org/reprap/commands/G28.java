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

public class G28 extends Command
{
	private final Parameter home = new Parameter();
	
	G28 next;
	private RepRapController repRapController;
	private G28Pool pool;
	boolean executed = false;
	
	G28(HostController hostController, CommandController commandController, RepRapController repRapController, G28Pool pool) 
	{
		super(hostController, commandController);
		this.repRapController = repRapController;
		this.pool = pool;
	}
	
	public void setParameters(Parameter parameter) 
	{
		home.X = (parameter.X > Integer.MIN_VALUE) ? 0 : Integer.MIN_VALUE;
		home.Y = (parameter.Y > Integer.MIN_VALUE) ? 0 : Integer.MIN_VALUE;
		home.Z = (parameter.Z > Integer.MIN_VALUE) ? 0 : Integer.MIN_VALUE;
		home.E = (parameter.E > Integer.MIN_VALUE) ? 0 : Integer.MIN_VALUE;
		this.executed = false;
	}
	
	@Override
	public boolean execute() 
	{
		if(!executed)
		{
			//Try to reach endstops by moving towards them
			repRapController.setTarget(home);
			executed=true;
		}
		if(repRapController.isInPosition())
		{
			pool.returnToPool(this);
			return true;
		}
		return false;
	}
	
	@Override
	protected void respond() 
	{
		//Do nothing, already confirmed
	}
}
