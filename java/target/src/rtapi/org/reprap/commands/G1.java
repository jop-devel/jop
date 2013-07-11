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

class G1 extends Command
{
	G1 next;
	private RepRapController repRapController;
	private G1Pool pool;
	private Parameter parameter = new Parameter();
	private boolean executed = false;
	
	G1(HostController hostController, CommandController commandController, RepRapController repRapController, G1Pool pool) 
	{
		super(hostController, commandController);
		this.repRapController = repRapController;
		this.pool = pool;
	}
	
	public void setParameters(Parameter parameter) 
	{
		this.parameter.copy(parameter);
		this.executed = false;
	}
	
	@Override
	public boolean execute() 
	{
		if(!executed)
		{
			repRapController.setTarget(parameter);
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
