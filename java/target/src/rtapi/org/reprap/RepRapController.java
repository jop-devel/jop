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
	public static final int DECIMALS = 2; //X,Y,Z and E values are turned into millimeter*10
	private static final int X_STEPS_PER_MILLIMETER = 40; //= steps*microstepping^-1/(belt_pitch*pulley_teeth) = 200*(1/8)^-1/(5*8)
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
	
	private RepRapInterface reprap = new RepRapInterface();
	//private RepRapSimulator reprap = new RepRapSimulator();
	private LedSwitch LS = LedSwitchFactory.getLedSwitchFactory().getLedSwitch();
	
	private Parameter current = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Current position
	private Parameter target = new Parameter(0,0,0,0,E_MAX_FEED_RATE,200);//Target position
	private Parameter delta = new Parameter(0,0,0,0,0,0);//The move length
	private Parameter direction = new Parameter(1,1,1,1,0,0);//1 = positive direction, -1 = negative
	private Parameter error = new Parameter(0,0,0,0,0,0);//Bresenham errors
	private Parameter max = new Parameter(5600,6000,24000,Integer.MAX_VALUE,0,0);//Max X,Y,Z positions
	

	private int dT = 0; // Time (1 millisecond pulse count) needed to perform move 
	
	int output = 0x01040412;
	boolean Stepping = false;
	
	
	/*
	 * Square root function from
	 */
	
	final static int[] table = 
		{
	     0,    16,  22,  27,  32,  35,  39,  42,  45,  48,  50,  53,  55,  57,
	     59,   61,  64,  65,  67,  69,  71,  73,  75,  76,  78,  80,  81,  83,
	     84,   86,  87,  89,  90,  91,  93,  94,  96,  97,  98,  99, 101, 102,
	     103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118,
	     119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132,
	     133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145,
	     146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155, 156, 157,
	     158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
	     169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178,
	     179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188,
	     189, 189, 190, 191, 192, 192, 193, 193, 194, 195, 195, 196, 197, 197,
	     198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206,
	     207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214, 214, 215,
	     215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223,
	     224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230, 231,
	     231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238,
	     239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246,
	     246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253,
	     253, 254, 254, 255
	  };

	  /**
	   * A faster replacement for (int)(java.lang.Math.sqrt(x)).  Completely accurate for x < 2147483648 (i.e. 2^31)...
	   */
	  static int sqrt(int x) {
	    int xn;

	    if (x >= 0x10000) {
	      if (x >= 0x1000000) {
	        if (x >= 0x10000000) {
	          if (x >= 0x40000000) {
	            xn = table[x >> 24] << 8;
	          } else {
	            xn = table[x >> 22] << 7;
	          }
	        } else {
	          if (x >= 0x4000000) {
	            xn = table[x >> 20] << 6;
	          } else {
	            xn = table[x >> 18] << 5;
	          }
	        }

	        xn = (xn + 1 + (x / xn)) >> 1;
	        xn = (xn + 1 + (x / xn)) >> 1;
	        return ((xn * xn) > x) ? --xn : xn;
	      } else {
	        if (x >= 0x100000) {
	          if (x >= 0x400000) {
	            xn = table[x >> 16] << 4;
	          } else {
	            xn = table[x >> 14] << 3;
	          }
	        } else {
	          if (x >= 0x40000) {
	            xn = table[x >> 12] << 2;
	          } else {
	            xn = table[x >> 10] << 1;
	          }
	        }

	        xn = (xn + 1 + (x / xn)) >> 1;

	        return ((xn * xn) > x) ? --xn : xn;
	      }
	    } else {
	      if (x >= 0x100) {
	        if (x >= 0x1000) {
	          if (x >= 0x4000) {
	            xn = (table[x >> 8]) + 1;
	          } else {
	            xn = (table[x >> 6] >> 1) + 1;
	          }
	        } else {
	          if (x >= 0x400) {
	            xn = (table[x >> 4] >> 2) + 1;
	          } else {
	            xn = (table[x >> 2] >> 3) + 1;
	          }
	        }

	        return ((xn * xn) > x) ? --xn : xn;
	      } else {
	        if (x >= 0) {
	          return table[x] >> 4;
	        }
	      }
	    }
	    return -1;
	  }
	
	private boolean inPosition = false;
	
	synchronized public boolean isInPosition()
	{
		return inPosition & !Stepping;
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
			for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
			{
				temp = temp/10;
			}
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
			for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
			{
				temp = temp/10;
			}
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
			for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
			{
				temp = temp/10;
			}
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
			for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
			{
				temp = temp/10;
			}
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
			for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
			{
				temp = temp/10;
			}
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
		
		int length = sqrt(delta.X/X_STEPS_PER_MILLIMETER*delta.X/X_STEPS_PER_MILLIMETER+delta.Y/Y_STEPS_PER_MILLIMETER*delta.Y/Y_STEPS_PER_MILLIMETER+
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
	
	synchronized private int getTargetTemperature() 
	{
		return targetTemperature;
	}
	
	synchronized public void setTargetTemperature(int targetTemperature) 
	{
		for (int i = 0; i < DECIMALS; i++) //@WCA loop = 1
		{
			targetTemperature = targetTemperature/10;
		}
		if(targetTemperature > MAX_TEMPERATURE)
		{
			targetTemperature = MAX_TEMPERATURE;
		}
		this.targetTemperature = targetTemperature;
	}
	
	private int tmpcnt1 = 0;
	private int tmppnt = 0;
	private int[] tmpval = new int[5];
	private int tmpcnt2 = 0;
	
	
	@Override
	public void handleAsyncEvent()
	{
		tmpcnt1++;
		if(tmpcnt1 == 1000)
		{
			tmpcnt1 = 0;
			int tmpCur = reprap.readTemperature();
			tmpval[tmppnt++] = tmpCur;
			if(tmppnt == tmpval.length)
			{
				tmppnt = 0;
			}
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
		tmpcnt2++;
		if(tmpcnt2 == 5000)
		{
			tmpcnt2 = 0;
			int temp = 0;
			for (int i = 0; i < tmpval.length; i++) //@WCA loop = 5 
			{
				temp += tmpval[i];
			}
			temp = temp/tmpval.length;
			setCurrentTemperature(temp);
			LS.ledSwitch = temp;
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
				boolean tempInPosition = true;
				int sensorvalue = reprap.readEndstops();
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
				Stepping = true;
				setInPosition(tempInPosition);
			}
		}
		reprap.write(output);
	}
}