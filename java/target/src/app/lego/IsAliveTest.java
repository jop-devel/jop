package lego;

import lego.lib.*;

public class IsAliveTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		while (true)
		{
			for (int i = 0; i < 4; i++)
				Leds.blinkUpdate(i);
		}
	}

}
