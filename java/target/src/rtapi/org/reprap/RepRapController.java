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
	public static final int DECIMALS = 1; //X,Y,Z and E values are turned into millimeter*10
	private static final int X_STEPS_PER_MILLIMETER = 40; //= steps*microstepping^-1/(belt_pitch*pulley_teeth) = 200*(1/8)^-1/(5*8)
	private static final int Y_STEPS_PER_MILLIMETER = X_STEPS_PER_MILLIMETER;
	private static final int Z_STEPS_PER_MILLIMETER = 160; //= steps/distance_between_threads = 200/1.25
	private static final int E_STEPS_PER_MILLIMETER = 376; //= steps*gear_ration/(Pi*diameter) = 200*(39/11)/(Pi*0.6)
	
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
		EH.expansionHeader = value;
	}
	
	ExpansionHeaderFactory EHF = ExpansionHeaderFactory.getExpansionHeaderFactory();
	ExpansionHeader EH = EHF.getExpansionHeader();
	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	public LedSwitch LS = LSF.getLedSwitch();
	
	private Parameter current = new Parameter();
	private Parameter target = new Parameter();
	
	private int dX = 0;
	private int dY = 0;
	private int sX = 1;
	private int sY = 1;
	private int sZ = 1;
	private int BresenhamError = 0;
	
	int value = 0x01040412;
	boolean Stepping = false;
	
	private boolean inPosition = true;
	
	public boolean inPosition()
	{
		synchronized (current) 
		{
			return inPosition;
		}
	}
	
	public void setTarget(Parameter parameters)
	{
		synchronized (current) 
		{
		
			//int highestMove = 0;
			if(parameters.X > Integer.MIN_VALUE)
			{
				target.X = (parameters.X*X_STEPS_PER_MILLIMETER)/10;
			}
			if(parameters.Y > Integer.MIN_VALUE)
			{
				target.Y = (parameters.Y*Y_STEPS_PER_MILLIMETER)/10;
			}
			if(parameters.Z > Integer.MIN_VALUE)
			{
				target.Z = (parameters.Z*Z_STEPS_PER_MILLIMETER)/10;
			}
			if(parameters.E > Integer.MIN_VALUE)
			{
				target.E = (parameters.E*E_STEPS_PER_MILLIMETER)/10;;
			}
			if(parameters.F > Integer.MIN_VALUE)
			{
				target.F = parameters.F;
			}
			
			if(target.X > current.X)
			{
				dX = target.X-current.X;
				value = setBit(value,8,true);
				sX = 1;
			}
			else
			{
				dX = current.X-target.X;
				value = setBit(value,8,false);
				sX = -1;
			}
			if(target.Y > current.Y)
			{
				dY = target.Y-current.Y;
				value = setBit(value,16,true);
				sY = 1;
			}
			else
			{
				dY = current.Y-target.Y;
				value = setBit(value,16,false);
				sY = -1;
			}
			if(target.Z > current.Z)
			{
				value = setBit(value,22,false);
				value = setBit(value,28,false);
				sZ = 1;
			}
			else
			{
				value = setBit(value,22,true);
				value = setBit(value,28,true);
				sZ = -1;
			}
			BresenhamError = dX-dY;
		}
	}
	
	@Override
	public void handleAsyncEvent()
	{
		boolean inPosition = true;
		int switchvalue = LS.ledSwitch;
		synchronized (current) 
		{
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
				
				int tempError = BresenhamError*2;
				if(current.X != target.X)
				{
					inPosition = false;
					if(tempError > -dY)
					{
						BresenhamError -= dY;
						value = setBit(value,6,true);
						current.X += sX;
					}
				}
				if(current.Y != target.Y)
				{
					inPosition = false;
					if(tempError < dX)
					{
						BresenhamError += dX;
						value = setBit(value,12,true);
						current.Y += sY;
					}
				}
				if(current.Z != target.Z)
				{
					inPosition = false;
					value = setBit(value,20,true);
					value = setBit(value,26,true);
					current.Z += sZ;
				}
				Stepping = true;
			}
			EH.expansionHeader = value;
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