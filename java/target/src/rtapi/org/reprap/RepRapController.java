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

/*
	Author: Tórur Biskopstø Strøm (torur.strom@gmail.com)
*/
package org.reprap;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import com.jopdesign.io.*;


public class RepRapController extends PeriodicEventHandler
{
	private static RepRapController instance;
	
	public static RepRapController getInstance()
	{
		if(instance == null)
		{
			instance = new RepRapController();
		}
		return instance;
	}
	
	private RepRapController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(50, null, 0, 0));
	}
	
	ExpansionHeaderFactory EHF = ExpansionHeaderFactory.getExpansionHeaderFactory();
	ExpansionHeader EH = EHF.getExpansionHeader();
	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	public LedSwitch LS = LSF.getLedSwitch();
	
	private int X = 0;
	private int Y = 0;
	private int Z = 0;
	private int E = 0;
	private int F = 0;
	private int targetX = 0;
	private int targetY = 0;
	private int targetZ = 0;
	private int targetE = 0;
	private int targetF = 0;
	private boolean inPosition = true;
	
	private Object lockTarget = new Object();
	private Object lockPosition = new Object();
	
	public boolean inPosition()
	{
		synchronized (lockPosition) 
		{
			return inPosition;
		}
	}
	
	public void setTarget(boolean XSet, boolean YSet, boolean ZSet, boolean ESet, boolean FSet, int X, int Y, int Z, int E, int F)
	{
		synchronized (lockTarget) 
		{
			if(XSet)
			{
				targetX = X;
			}
			if(YSet)
			{
				targetY = Y;
			}
			if(ZSet)
			{
				targetZ = Z;
			}
			if(ESet)
			{
				targetE = E;
			}
			if(FSet)
			{
				targetF = F;
			}
		}
	}
	
	int oldvalue = 0x01040412;
	boolean Stepping = false;
	
	@Override
	public void handleAsyncEvent()
	{
		boolean inPosition = true;
		int value = oldvalue;
		int switchvalue = LS.ledSwitch;
		int tempX;
		int tempY;
		int tempZ;
		int tempE;
		int tempF;
		synchronized (lockTarget) 
		{
			tempX = targetX;
			tempY = targetY;
			tempZ = targetZ;
			tempE = targetE;
			tempF = targetF;
		}
		if(Stepping)
		{
			value = setBit(value,0,false);
			value = setBit(value,6,false);
			value = setBit(value,12,false);
			value = setBit(value,20,false);
			value = setBit(value,26,false);
			Stepping = false;
		}
		else
		{
			//Feeder
			value = setBit(value,0,getBitValue(switchvalue,0));
			value = setBit(value,2,getBitValue(switchvalue,1));
			//Heater
			value = setBit(value,23,getBitValue(switchvalue,17));
			value = setBit(value,25,getBitValue(switchvalue,17));
			if(X != tempX)
			{
				inPosition = false;
				value = setBit(value,6,true);
				boolean direction = false;
				if(X < tempX)
				{
					X++;
					direction = true;
				}
				else
				{
					X--;
				}
				value = setBit(value,8,direction);
			}
			
			if(Y != tempY)
			{
				inPosition = false;
				value = setBit(value,12,true);
				boolean direction = false;
				if(Y < tempY)
				{
					Y++;
					direction = true;
				}
				else
				{
					Y--;
				}
				value = setBit(value,16,direction);
			}
			
			if(Z != tempZ)
			{
				inPosition = false;
				value = setBit(value,20,true);
				value = setBit(value,26,true);
				boolean direction = false;
				if(Z < tempZ)
				{
					Z++;
					direction = true;
				}
				else
				{
					Z--;
				}
				value = setBit(value,22,direction);
				value = setBit(value,28,direction);
			}
			Stepping = true;
		}
		EH.expansionHeader = value;
		oldvalue = value;
		synchronized (lockPosition) 
		{
			this.inPosition = inPosition;
		}
	}
	
	private static boolean getBitValue(int Value, int BitNumber)
	{
		return (Value & (1 << BitNumber)) != 0;
	}
	
	private static int setBit(int Number, int BitNumber, boolean Value)
	{
		if(Value)
		{
			return Number | (1 << BitNumber);
		}
		return Number & ~(1 << BitNumber);
	}
}