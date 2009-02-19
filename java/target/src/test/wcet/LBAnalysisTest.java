/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package wcet;

public class LBAnalysisTest {

	private static boolean flag;
	private static int index;

	public static void main(String [] args) {

		flag = (args == null);

 		measure0();
		measure1();
		measure2();
		measure3();
		measure4();
		measure5();
 		measure6();
 		measure7();
 		measure8();
 		measure9();
 		measure10();
 		measure11();
 		measure12();
 		measure13();
	}

	/* 100 */
	public static void measure0() {
        int x = 0;
        while(x < 100) {
			x++;
        } 
	}

	/* \infty */
	public static void measure1() {
        int x = 0;
        while(x < 100) {
            x=-1;
            if(x == 0) {
                x+=120;
            } else {
                x+=10;
            }
        } 
	}

	/* \infty */
	public static void measure2() {
        int x = -1;
		int y = 120;
		int z = 10;
        while(x < 100) {
			x = -1;
            if(x == 0) {
                x = y+x;
            } else {
                x = z+x;
            }
        } 
	}

	/* \infty */
	public static void measure3() {
        int x = -1;
		int y = 120;
		int z = 10;
        while(x < 100) {
			x = -1;
            if(x == 0) {
                x += y;
            } else {
                x += z;
            }
        } 
	}

	/* 11 */
	public static void measure4() {
        int x = -1;
        while(x < 100) {
            if(x == 0) {
                x += 120;
            } else {
                x += 10;
            }
        } 
	}

	/* \infty */
	public static void measure5() {
        int x = -1;
        while(x < 100) {
            if(x == 0) {
                x += 110;
            }
        } 
	}

	/* \infty */
	public static void measure6() {
        int x = 0;
		if (flag) {
			x = 2;
		}
        while(x < 100) {
            if(x > 0 && x < 2) {
                x += 2;
            } else if (x >= 3) {
				x += 1;
			}
        } 
	}

	/* \infty */
	public static void measure7() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			x=-1;
			x=x+10;
		}
	}

	/* \infty */
	public static void measure8() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			x-=1000;
			if (x != 0) {
				x+=1;
			} else {
				x+=2;
			}
		}
	}

	/* \infty */
	public static void measure9() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			if (x != 0) {
				x-=1000;
			} else {
				x+=1;
			}
		}
	}

	/* \infty */
	public static void measure10() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			if (x != 0) {
				x-=1;
			} else {
				x-=2;
			}
			if (x != 0) {
				x+=1;
			} else {
				x+=2;
			}
		}
	}

	/* \infty */
	public static void measure11() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			if (x != 0) {
				x+=-1;
			} else {
				x+=1;
			}
			if (x != 0) {
				x+=1;
			} else {
				x+=2;
			}
		}
	}

	/* \infty */
	public static void measure12() {
		int x = 0;
		while(x < 100) { // Terminiert nicht
			if (flag) {
				x++;
			}
		}
	}

	/* 10 */
	public static void measure13() {
		int i = 0;
		index = 100;
		while(i < index) {
			i++;
		}
	}

}