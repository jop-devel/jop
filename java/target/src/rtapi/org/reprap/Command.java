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
package org.reprap;


public abstract class Command 
{
	Command next;
	boolean enqueued = false;
	protected HostController hostController;
	private CommandController commandController;
	
	protected Command(HostController hostController, CommandController commandController)
	{
		this.commandController = commandController;
		this.hostController = hostController;
	}
	
	protected abstract boolean execute();
	
	protected void respond()
	{
		hostController.confirmCommand(null);
	}
	
	public boolean enqueue()
	{
		return commandController.enqueue(this);
	}
}
