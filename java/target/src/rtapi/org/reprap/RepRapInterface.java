package org.reprap;

import com.jopdesign.io.ExpansionHeader;
import com.jopdesign.io.ExpansionHeaderFactory;

public class RepRapInterface 
{
	
	private ExpansionHeader reprap = ExpansionHeaderFactory.getExpansionHeaderFactory().getExpansionHeader();
	
	public void write(int IOValue)
	{
		reprap.IO = IOValue;
	}
	
	public int readEndstops()
	{
		return reprap.IO;
	}
	
	public int readTemperature()
	{
		return timingToTemperature(reprap.ADC);
	}
	

	
	private static final int[] TIMING = {1693,2213,3016,4078,5877,8158,12822,20636,32302,55876,88901,175887,331095};
	private static final int[] TEMPERATURE = {209,197,187,163,150,138,117,99,85,68,56,38,22};
	
	private static int timingToTemperature(int timing)
	{
		if(timing <= 0)
		{
			return 0;
		}
		int i;
		for(i = 0; i < TIMING.length-2; i++) //@WCA loop = 11
		{
			if(timing <= TIMING[i])
			{
				break;
			}
		}
		return (int)(TEMPERATURE[i]+(timing-TIMING[i])*((1000000*(TEMPERATURE[i+1]-TEMPERATURE[i]))/(TIMING[i+1]-TIMING[i]))/1000000);
	}
	
}
