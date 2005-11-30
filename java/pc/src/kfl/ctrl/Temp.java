package kfl.ctrl;

/**
*	test program for sigma delta ADC with NTC.
*/

public class Temp {

	private static int[] tab;		// starts with -55o C for real value

	public static void init() {

		int[] x = { -1,
			17287, 
			17296, 
			17307, 
			17322, 
			17342, 
			17369, 
			17405, 
			17450, 
			17510, 
			17586, 
			17684, 
			17806, 
			17958, 
			18145, 
			18373, 
			18650, 
			18994, 
			19397, 
			19887, 
			20470, 
			21162, 
			21976, 
			22927, 
			24036, 
			25315, 
			26792, 
			28510, 
			30485, 
			32747, 
			35329, 
			38237, 
			41527, 
			45218, 
			49364, 
		};
		tab = x;
	}

	public static int calc(int val) {

		int i, t;
		int x, y;

		t = -60;
		for (i=0; val>tab[i]; ++i) {
			t += 5;
		}
		x = val-tab[i];
		y = tab[i+1] - tab[i];
		t += 5*x/y;

		return t;
	}
}
