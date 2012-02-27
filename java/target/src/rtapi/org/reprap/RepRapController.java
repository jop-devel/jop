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
import javax.realtime.ThrowBoundaryError;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.JopSystem;
import com.jopdesign.io.*;


public class RepRapController extends PeriodicEventHandler
{
	ExpansionHeaderFactory EHF = ExpansionHeaderFactory.getExpansionHeaderFactory();
	ExpansionHeader EH = EHF.getExpansionHeader();
	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	LedSwitch LS = LSF.getLedSwitch();
	
	int oldvalue = 0x01040412;
	boolean Stepping = false;
	
	RepRapController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(300, null, 0, 0));
	}
	
	@Override
	public void handleAsyncEvent()
	{
		int value = oldvalue;
		int switchvalue = LS.ledSwitch;
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
			value = setBit(value,0,getBitValue(switchvalue,0));
			value = setBit(value,6,getBitValue(switchvalue,2));
			value = setBit(value,12,getBitValue(switchvalue,4));
			value = setBit(value,20,getBitValue(switchvalue,6));
			value = setBit(value,26,getBitValue(switchvalue,6));
			Stepping = true;
		}
		value = setBit(value,2,getBitValue(switchvalue,1));
		value = setBit(value,8,getBitValue(switchvalue,3));
		value = setBit(value,16,getBitValue(switchvalue,5));
		value = setBit(value,22,getBitValue(switchvalue,7));
		value = setBit(value,28,getBitValue(switchvalue,7));
		value = setBit(value,23,getBitValue(switchvalue,17));
		value = setBit(value,25,getBitValue(switchvalue,17));
		
		EH.expansionHeader = value;
		oldvalue = value;
	}
	
	private boolean getBitValue(int Value, int BitNumber)
	{
		return (Value & (1 << BitNumber)) != 0;
	}
	
	private int setBit(int Number, int BitNumber, boolean Value)
	{
		if(Value)
		{
			return Number | (1 << BitNumber);
		}
		return Number & ~(1 << BitNumber);
	}
}