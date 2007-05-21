package lego;

import com.jopdesign.sys.Native;

import lego.lib.*;

public class BouncingTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int lastButtons = Native.rd(Buttons.IO_NOT_DEBOUNCED_BUTTONS);
		int buttons;

		do
		{
			buttons = Native.rd(Buttons.IO_NOT_DEBOUNCED_BUTTONS);
		}
		while (buttons == lastButtons);

		lastButtons = buttons;

		while (true)
			if (lastButtons != Native.rd(Buttons.IO_NOT_DEBOUNCED_BUTTONS))
			{
				System.out.println("Bounced!");
				return;
			}
	}
}
