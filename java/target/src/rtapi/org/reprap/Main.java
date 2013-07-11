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

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.JopSystem;


public class Main implements Safelet
{

	private MissionSequencer seq = null;
	
	public long immortalMemorySize(){return 0;}
	
	
	public static void main(String[] args) {
		JopSystem.startMission(new Main());
	}
	
	@Override
	public MissionSequencer getSequencer()
	{
		if(seq == null)
		{
			seq = new RepRapMissionSequencer();
		}
		return seq;
	}
	
	public class RepRapMissionSequencer extends MissionSequencer
	{
		private Mission mission = null;
		
		RepRapMissionSequencer()
		{
			super(new PriorityParameters(0),new StorageParameters(1, null, 0,0));
		}
				
		@Override
		protected Mission getNextMission()
		{
			if(mission == null)
			{
				mission = new RepRapMission();
			}
//			current_mission = mission;
			return mission;
		}
		
		public class RepRapMission extends Mission
		{			
			@Override
			public long missionMemorySize()
			{
				return 0;
			}
			
			HostController hostController;
			RepRapController repRapController;
			CommandController commandController;
			CommandParser commandParser;
			
			@Override
			protected void initialize()
			{
				hostController = new HostController();
				repRapController = new RepRapController();
				commandController = new CommandController();
				commandParser = new CommandParser(hostController,commandController,repRapController);
			}
		}
	}

	@Override
	public void initializeApplication() {
	}
}
			
			
			
			