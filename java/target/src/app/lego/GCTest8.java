package lego;

import com.jopdesign.sys.*;
import joprt.RtThread;
import lego.lib.Buttons;
import lego.lib.DigitalInputs;
import lego.lib.FutureUse;
import lego.lib.Leds;
import lego.lib.Microphone;
import lego.lib.Motor;
import lego.lib.Sensors;

/**
 * does not crash
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class GCTest8
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{				

		new RtThread(10, 10*1000)
		{
			public void run()
			{
				StringBuffer output = new StringBuffer(500);
				do
				{
					output = new StringBuffer(500);
					
				} while (true);
			}
		};

		RtThread.startMission();
	}

}
