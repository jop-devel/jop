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

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import com.jopdesign.io.*;


public class RepRapController extends PeriodicEventHandler
{
	public static final int NR_DECIMALS = 2; //X,Y,Z and E values are turned into millimeter*10
	private static final int X_STEPS_PER_MILLIMETER = 40; //= (steps*microstepping^-1)/(belt_pitch*pulley_teeth) = (200*(1/8)^-1)/(5*8)
	private static final int Y_STEPS_PER_MILLIMETER = X_STEPS_PER_MILLIMETER;
	private static final int Z_STEPS_PER_MILLIMETER = 160; //= steps/distance_between_threads = 200/1.25
	private static final int E_STEPS_PER_MILLIMETER = 37; //= steps*gear_ration/(Pi*diameter) = 200*(39/11)/(Pi*6)
	private static final int E_MAX_FEED_RATE = 800;
	private static final int STEPS_PER_MINUTE = 30000;
	private static final int MAX_TEMPERATURE = 220;

	RepRapController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(100, new long[]{100}, 0, 0), 0);
	}
	
	private ExpansionHeader reprap = ExpansionHeaderFactory.getExpansionHeaderFactory().getExpansionHeader();
	//private RepRapSimulator reprap = new RepRapSimulator();
	//private LedSwitch LS = LedSwitchFactory.getLedSwitchFactory().getLedSwitch();
	
	private Parameter current = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Current position
	private Parameter target = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Target position
	private Parameter delta = new Parameter(0,0,0,0,0,0);//The move length
	private Parameter direction = new Parameter(1,1,1,1,0,0);//1 = positive direction, -1 = negative
	private Parameter error = new Parameter(0,0,0,0,0,0);//Bresenham errors
	private Parameter max = new Parameter(5600,6000,24000,Integer.MAX_VALUE,0,0);//Max X,Y,Z positions
	

	private int dT = 0; // Time (1 millisecond pulse count) needed to perform move 
	
	int output = 0x01040412;
	boolean Stepping = false;
	
	private boolean inPosition = false;
	
	synchronized public boolean isInPosition()
	{
		return inPosition;
	}
	
	synchronized private void setInPosition(boolean inPosition)
	{
		this.inPosition = inPosition;
	}
	
	private boolean absolute = true;
	
	synchronized private boolean isAbsolute()
	{
		return absolute;
	}
	
	synchronized public void setAbsolute(boolean absolute)
	{
		this.absolute = absolute;
	}
	
	//Not threadsafe
	public void setParameter(Parameter source, Parameter target)
	{
		boolean absolute = isAbsolute();
		if(source.X > Integer.MIN_VALUE)
		{
			int temp = source.X*X_STEPS_PER_MILLIMETER;
			//temp = temp/DECIMALS;
			temp = Math.divs100(temp);
			if(absolute)
			{
				target.X = temp;
			}
			else
			{
				target.X = current.X+temp;
			}
		}
		if(source.Y > Integer.MIN_VALUE)
		{
			int temp = source.Y*Y_STEPS_PER_MILLIMETER;
			temp = Math.divs100(temp);
			if(absolute)
			{
				target.Y = temp;
			}
			else
			{
				target.Y = current.Y+temp;
			}
		}
		if(source.Z > Integer.MIN_VALUE)
		{
			int temp = source.Z*Z_STEPS_PER_MILLIMETER;
			temp = Math.divs100(temp);
			if(absolute)
			{
				target.Z = temp;
			}
			else
			{
				target.Z = current.Z+temp;
			}
		}
		if(source.E > Integer.MIN_VALUE)
		{
			int temp = source.E*E_STEPS_PER_MILLIMETER;
			temp = Math.divs100(temp);
			if(absolute)
			{
				target.E = temp;
			}
			else
			{
				target.E = current.E+temp;
			}
		}
		if(source.F > 0)
		{
			int temp = source.F;
			temp = Math.divs100(temp);
			if(absolute)
			{
				target.F = temp;
			}
			else
			{
				target.F = current.F+temp;
			}
		}
	}
	
	//Not threadsafe
	public void setPosition(Parameter position)
	{
		boolean absolute = isAbsolute();
		setAbsolute(true);
		setParameter(position,current);
		target.copy(current);
		setAbsolute(absolute);
	}
	
	//Not threadsafe
	public void setTarget(Parameter newTarget)
	{
		setParameter(newTarget,target);
		if(target.X >= current.X)
		{
			delta.X = target.X-current.X;
			output = output | (1 << 8);
			direction.X = 1;
		}
		else
		{
			delta.X = current.X-target.X;
			output = output & ~(1 << 8);
			direction.X = -1;
		}
		if(target.Y >= current.Y)
		{
			delta.Y = target.Y-current.Y;
			output = output | (1 << 16);
			direction.Y = 1;
		}
		else
		{
			delta.Y = current.Y-target.Y;
			output = output & ~(1 << 16);
			direction.Y = -1;
		}
		if(target.Z >= current.Z)
		{
			delta.Z = target.Z-current.Z;
			output = output & ~(1 << 22);
			output = output & ~(1 << 28);
			direction.Z = 1;
		}
		else
		{
			delta.Z = current.Z-target.Z;
			output = output | (1 << 22);
			output = output | (1 << 28);
			direction.Z = -1;
		}
		if(target.E >= current.E)
		{
			delta.E = target.E-current.E;
			output = output | (1 << 2);
			direction.E = 1;
		}
		else
		{
			delta.E = current.E-target.E;
			output = output & ~(1 << 2);
			direction.E = -1;
		}
		
		int length = Math.sqrt(delta.X/X_STEPS_PER_MILLIMETER*delta.X/X_STEPS_PER_MILLIMETER+delta.Y/Y_STEPS_PER_MILLIMETER*delta.Y/Y_STEPS_PER_MILLIMETER+
				delta.Z/Z_STEPS_PER_MILLIMETER*delta.Z/Z_STEPS_PER_MILLIMETER+delta.E/E_STEPS_PER_MILLIMETER*delta.E/E_STEPS_PER_MILLIMETER);
		//Already checked for negativity and division by zero. Divide by 2 to account for 1 pulse every other millisecond
		dT = (length*STEPS_PER_MINUTE)/target.F;
		
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
		setInPosition(false);
	}
	
	private int currentTemperature = 0;
	private int targetTemperature = Integer.MIN_VALUE;
	
	synchronized public int getCurrentTemperature() 
	{
		return currentTemperature;
	}
	
	synchronized private void setCurrentTemperature(int currentTemperature) 
	{
		this.currentTemperature = currentTemperature;
	}
	
	synchronized public int getTargetTemperature() 
	{
		return targetTemperature;
	}
	
	synchronized public void setTargetTemperature(int targetTemperature) 
	{
		targetTemperature = Math.divs100(targetTemperature);
		if(targetTemperature > MAX_TEMPERATURE)
		{
			targetTemperature = MAX_TEMPERATURE;
		}
		this.targetTemperature = targetTemperature;
	}
	
	private int tmpcnt1 = 0;
	
	@Override
	public void handleAsyncEvent()
	{
		tmpcnt1++;
		if(tmpcnt1 == 500)
		{
			tmpcnt1 = 0;
			int tmpCur = reprap.readTemperature();
			setCurrentTemperature(tmpCur);
			//LS.ledSwitch = tmpCur;
			//Heater
			if(tmpCur < getTargetTemperature())
			{
				//output = output | (1 << 23);
				output = output | (1 << 25);
			}
			else
			{
				//output = output & ~(1 << 23);
				output = output & ~(1 << 25);
			}
			
		}
		if(!isInPosition())
		{
			if(Stepping)
			{
				output = output & ~(1 << 0);
				output = output & ~(1 << 6);
				output = output & ~(1 << 12);
				output = output & ~(1 << 20);
				output = output & ~(1 << 26);
				Stepping = false;
			}
			else
			{
				Stepping = true;
				boolean tempInPosition = true;
				int sensorvalue = reprap.readSensors();
				if(current.X != target.X)
				{
					if((sensorvalue & (1 << 7)) == 0 && direction.X == -1)//Check endstop
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
						tempInPosition = false;
						if(error.X > 0)
						{
							output = output | (1 << 6);
							current.X += direction.X;
							error.X -= 2*dT;
						}
					}
					error.X += 2*delta.X;
				}
				if(current.Y != target.Y)
				{
					if((sensorvalue & (1 << 5)) == 0 && direction.Y == -1)//Check endstop
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
						tempInPosition = false;
						if(error.Y > 0)
						{
							output = output | (1 << 12);
							current.Y += direction.Y;
							error.Y -= 2*dT;
						}
					}
					error.Y += 2*delta.Y;
				}
				if(current.Z != target.Z)
				{
					if((sensorvalue & (1 << 3)) == 0 && direction.Z == -1)//Check endstop
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
						tempInPosition = false;
						if(error.Z > 0)
						{
							output = output | (1 << 20);
							output = output | (1 << 26);
							current.Z += direction.Z;
							error.Z -= 2*dT;
						}
					}
					error.Z += 2*delta.Z;
				}
				if(current.E != target.E)
				{
					tempInPosition = false;
					if(error.E > 0)
					{
						output = output | (1 << 0);
						current.E += direction.E;
						error.E -= 2*dT;
					}
					error.E += 2*delta.E;
				}
				setInPosition(tempInPosition);
			}
		}
		reprap.write(output);
	}
}