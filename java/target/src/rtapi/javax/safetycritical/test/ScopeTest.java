package javax.safetycritical.test;

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

public class ScopeTest implements Safelet
{

	private MissionSequencer seq = null;
	
	@Override
	public MissionSequencer getSequencer()
	{
		if(seq == null)
		{
			seq = new MissionSequencer(
					new PriorityParameters(0),
					new StorageParameters(100, null, 0,0)
					)
			{
				private Mission mission = null;
				
				@Override
				protected Mission getNextMission()
				{
					if(mission == null)
					{
						mission = new Mission()
						{
							PeriodicEventHandler peh = null;
							
							@Override
							public long missionMemorySize()
							{
								return 50;
							}
							
							@Override
							protected void initialize()
							{
								if(peh == null)
								{
									peh = new PeriodicEventHandler(
											new PriorityParameters(1),
											new PeriodicParameters(null, new RelativeTime(1000,0)),
											new StorageParameters(25, null, 0, 0)
										  )
									{
										public Object innerObjectReference = null;
										private Runnable innerLogic = null;
										
										@Override
										public void handleAsyncEvent()
										{
											if(innerLogic == null)
											{
												innerLogic = new Runnable()
												{
													private Object object = null;
													private boolean caughtException = false;
													
													@Override
													public void run()
													{
														if(object == null)
														{
															object = new Object();
														}
														caughtException = false;
														try
														{
															innerObjectReference = object;
														} catch (Exception e)
														{
															caughtException = true;
														}
														if(!caughtException)
														{
															throw new ThrowBoundaryError("The inner logic assigned a referenced to an inner object in an outer object!");
														}
													}
												};
											}
											ManagedMemory.getCurrentManagedMemory().enterPrivateMemory(20, innerLogic);
											
										}
									};
								}
								
							}
						};
					}
					return mission;
				}
			};
		}
		return seq;
	}

}
