package jbe;

import josx.platform.rcx.*;

/**
*	LowLevel time and output for leJOS
*/
public class LowLevel {

	static boolean init;

	static int timeMillis() {
    	return (int) System.currentTimeMillis();
	}

	static void msg(String msg) {

		TextLCD.print(msg);
		try { Thread.sleep(1500); } catch (Exception e) {}
	}

	static void msg(int val) {

		LCD.showNumber(val/1000);
		try { Thread.sleep(1500); } catch (Exception e) {}
		LCD.showNumber(val%1000);
		try { Thread.sleep(1500); } catch (Exception e) {}
	}

	static void msg(String msg, int val) {

		msg(msg);
		msg(val);
	}

	static void lf() {

	}
}
