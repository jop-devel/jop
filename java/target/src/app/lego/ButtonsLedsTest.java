package lego;

import lego.lib.*;

public class ButtonsLedsTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		while (true)
		{
			Leds.setLeds(Buttons.getButtons());
		}
	}

}
