/**
*	test program for sigma delta ADC with NTC.
*/

public class TestTemp {

	private static int[] tab;		// starts with -55o C for real value

	public static void main(String[] args) {

		Timer.init();			// wd

		Display.init();
// Display.line1();
		Temp.init();

		forever();
	}

/**
*	main loop.
*/
	private static void forever() {

		int val, old_val;

		old_val = 0;

		for (;;) {

			val = JopSys.rd(BBSys.IO_ADC);

			if (val!=old_val) {
				old_val = val;

				Display.cls();
				val = 46000-val;		// thinking in the other direction
				Display.intVal(val);
				int t = Temp.calc(val);
				Display.line2();
				if (t<0) {
					Display.data('-');
					Display.intVal(-t);
				} else {
					Display.data(' ');
					Display.intVal(t);
				}
				Timer.sleepWd(100);
			}

			Timer.wd();
		}
	}
}
