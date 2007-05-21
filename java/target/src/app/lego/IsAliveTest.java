package lego;

import lego.lib.*;

public class IsAliveTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int i = 0;
		while (true)
		{
			Leds.setLeds((++i & 0x10000) == 0 ? Buttons.getButtons() : ~Buttons.getButtons());
		}
	}

}
