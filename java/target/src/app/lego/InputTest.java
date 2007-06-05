package lego;

import java.io.IOException;

import lego.lib.Leds;

public class InputTest
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Waiting for input to echo...");
		while (true)
		{
			System.out.print((char)System.in.read());
			Leds.setLeds(~Leds.getLeds());
		}
	}
}
