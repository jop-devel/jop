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
	private static final int E_STEPS_PER_MILLIMETER = 37; //= steps*gear_ration/(Pi*diameter) = 200*(39/11)/(Pi*6)
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int E_MAX_FEED_RATE = 800;
	
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
			  new StorageParameters(50, null, 0, 0), 40);
		EH.expansionHeader = value;
	}
	
	ExpansionHeaderFactory EHF = ExpansionHeaderFactory.getExpansionHeaderFactory();
	ExpansionHeader EH = EHF.getExpansionHeader();
	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	public LedSwitch LS = LSF.getLedSwitch();
	
	private Parameter current = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Current position
	private Parameter target = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Target position
	private Parameter delta = new Parameter(0,0,0,0,0,0);//The move length
	private Parameter direction = new Parameter(1,1,1,1,0,0);//1 = positive direction, -1 = negative
	private Parameter error = new Parameter(0,0,0,0,0,0);//Bresenham errors
	private Parameter max = new Parameter(400000,800000,400000,Integer.MAX_VALUE,0,0);//Max X,Y,Z positions
	

	private int dT = 0; // Time (1 millisecond pulse count) needed to perform move 
	
	int value = 0x01040412;
	boolean Stepping = false;
	
	private boolean inPosition = false;
	
	public boolean inPosition()
	{
		synchronized (current) 
		{
			return inPosition;
		}
	}
	
	//Not threadsafe
	private static void setParameters(Parameter source, Parameter target)
	{
		if(source.X > Integer.MIN_VALUE)
		{
			target.X = (source.X*X_STEPS_PER_MILLIMETER)/(DECIMALS*10);
		}
		if(source.Y > Integer.MIN_VALUE)
		{
			target.Y = (source.Y*Y_STEPS_PER_MILLIMETER)/(DECIMALS*10);
		}
		if(source.Z > Integer.MIN_VALUE)
		{
			target.Z = (source.Z*Z_STEPS_PER_MILLIMETER)/(DECIMALS*10);
		}
		if(source.E > Integer.MIN_VALUE)
		{
			target.E = (source.E*E_STEPS_PER_MILLIMETER)/(DECIMALS*10);
		}
		if(source.F > 0)
		{
			target.F = source.F;
		}
	}
	
	public void setPosition(Parameter parameters)
	{
		synchronized (current) 
		{
			setParameters(parameters,current);
			target.X = current.X;
			target.Y = current.Y;
			target.Z = current.Z;
			target.E = current.E;
			target.F = current.F;
			target.S = current.S;
		}
	}
	
	public void setTarget(Parameter parameters)
	{
		synchronized (current) 
		{
			//int highestMove = 0;
			inPosition = false;
			setParameters(parameters,target);
			current.F = target.F; //No acceleration yet
			if(target.X >= current.X)
			{
				delta.X = target.X-current.X;
				setBit(8,true);
				direction.X = 1;
			}
			else
			{
				delta.X = current.X-target.X;
				setBit(8,false);
				direction.X = -1;
			}
			if(target.Y >= current.Y)
			{
				delta.Y = target.Y-current.Y;
				setBit(16,true);
				direction.Y = 1;
			}
			else
			{
				delta.Y = current.Y-target.Y;
				setBit(16,false);
				direction.Y = -1;
			}
			if(target.Z >= current.Z)
			{
				delta.Z = target.Z-current.Z;
				setBit(22,false);
				setBit(28,false);
				direction.Z = 1;
			}
			else
			{
				delta.Z = current.Z-target.Z;
				setBit(22,true);
				setBit(28,true);
				direction.Z = -1;
			}
			if(target.E >= current.E)
			{
				delta.E = target.E-current.E;
				setBit(2,true);
				direction.E = 1;
			}
			else
			{
				delta.E = current.E-target.E;
				setBit(2,false);
				direction.E = -1;
			}
			
			//Already checked for negativity and division by zero. Divide by 2 to account for 1 pulse every other millisecond
			dT = (delta.E*MILLISECONDS_PER_SECOND*SECONDS_PER_MINUTE)/(current.F*2*E_STEPS_PER_MILLIMETER);
			int tempdT = (delta.X*MILLISECONDS_PER_SECOND*SECONDS_PER_MINUTE)/(current.F*2*X_STEPS_PER_MILLIMETER);
			dT = (tempdT > dT) ? tempdT : dT;
			tempdT = (delta.Y*MILLISECONDS_PER_SECOND*SECONDS_PER_MINUTE)/(current.F*2*Y_STEPS_PER_MILLIMETER);
			dT = (tempdT > dT) ? tempdT : dT;
			tempdT = (delta.Z*MILLISECONDS_PER_SECOND*SECONDS_PER_MINUTE)/(current.F*2*Z_STEPS_PER_MILLIMETER);
			dT = (tempdT > dT) ? tempdT : dT;
			
			//If the target time to extrude is less than the speed of the axis, set the speed to the axis speed
			if(delta.X > dT)
			{
				dT = delta.X;
			}
			if(delta.Y > dT)
			{
				dT = delta.Y;
			}
			if(delta.Z > dT)
			{
				dT = delta.Z;
			}
			if(delta.E > dT)
			{
				dT = delta.E;
			}
			error.X = 2*delta.X - dT;
			error.Y = 2*delta.Y - dT;
			error.Z = 2*delta.Z - dT;
			error.E = 2*delta.E - dT;
		}
	}
	
	@Override
	public void handleAsyncEvent()
	{
		int switchvalue = LS.ledSwitch;
		int sensorvalue = switchvalue;//EH.expansionHeader;
		synchronized (current) 
		{
			if(Stepping)
			{
				setBit(0,false);
				setBit(6,false);
				setBit(12,false);
				setBit(20,false);
				setBit(26,false);
				Stepping = false;
			}
			else
			{
				//Heater
				//setBit(23,getBitValue(switchvalue,17));
				//setBit(25,getBitValue(switchvalue,17));
				
				inPosition = true;
				if(current.X != target.X)
				{
					if(!getBitValue(sensorvalue,7) && direction.X == -1)//Check endstop
					{
						current.X = 0;
						target.X = current.X;
					}
					else if(current.X == max.X && direction.X == 1)
					{
						target.X = current.X;
					}
					else
					{
						inPosition = false;
						if(error.X > 0)
						{
							setBit(6,true);
							current.X += direction.X;
							error.X -= 2*dT;
						}
					}
				}
				if(current.Y != target.Y)
				{
					if(!getBitValue(sensorvalue,5) && direction.Y == -1)//Check endstop
					{
						current.Y = 0;
						target.Y = current.Y;
					}
					else if(current.Y == max.Y && direction.Y == 1)
					{
						target.Y = current.Y;
					}
					else
					{
						inPosition = false;
						if(error.Y > 0)
						{
							setBit(12,true);
							current.Y += direction.Y;
							error.Y -= 2*dT;
						}
					}
				}
				if(current.Z != target.Z)
				{
					if(!getBitValue(sensorvalue,3) && direction.Z == -1)//Check endstop
					{
						current.Z = 0;
						target.Z = current.Z;
					}
					else if(current.Z == max.Z && direction.Z == 1)
					{
						target.Z = current.Z;
					}
					else
					{
						inPosition = false;
						if(error.Z > 0)
						{
							setBit(20,true);
							setBit(26,true);
							current.Z += direction.Z;
							error.Z -= 2*dT;
						}
					}
				}
				if(current.E != target.E)
				{
					inPosition = false;
					if(error.E > 0)
					{
						setBit(0,true);
						current.E += direction.E;
						error.E -= 2*dT;
					}
				}
				error.X += 2*delta.X;
				error.Y += 2*delta.Y;
				error.Z += 2*delta.Z;
				error.E += 2*delta.E;
				Stepping = true;
			}
		}
		EH.expansionHeader = value;
	}
	
	private static boolean getBitValue(int Value, int BitNumber)
	{
		return (Value & (1 << BitNumber)) != 0;
	}
	
	private void setBit(int BitNumber, boolean Value)
	{
		if(Value)
		{
			value = value | (1 << BitNumber);
			return;
		}
		value = value & ~(1 << BitNumber);
	}
}