package kfl.test;

import kfl.*;
/**
*	test program for sigma delta ADC.
*/

public class TestADC {

	public static void main(String[] args) {

		Timer.init();			// wd

		Display.init();
// Display.line1();

		forever();
	}


/**
*	main loop.
*/
	private static void forever() {

		int val, old_val;
		int i;

		old_val = 0;

		for (;;) {

			val = JopSys.rd(BBSys.IO_ADC);

			if (val!=old_val) {
				old_val = val;

				Display.cls();
				Display.intVal(val);
				Display.line2();

//
//	46.000 = 3.26V
//	41880 = 3V
//	300 = 0V
//
				i = val-300;
				i /= 139;					// 10mV (41.880-300)/300
//
//	without comerator:
//	11820 = 0V
//	39870 = 2V
//
				i = val-11820;
				i /= 140;

				if (i<0) i = 0;
				Display.intVal(i/100);
				i %= 100;
				Display.data('.');
				Display.data('0'+i/10);
				Display.data('0'+i%10);
			}

			Timer.wd();
		}
	}
}
