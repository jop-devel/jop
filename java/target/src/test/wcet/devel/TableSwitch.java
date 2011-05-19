/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Benedikt Huber <benedikt.huber@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wcet.devel;

/* test procedure:
 * 
 * (1) Compile TableSwitch, and check length of test1()
 * $test$>   make java_app  P1=test P2=wcet/devel P3=TableSwitch
 * $test$>   cat java/target/dist/bin/TableSwitch.jop.txt | grep test1 -A 2 | grep code_length
 * $stdout$> Code(max_stack = 2, max_locals = 0, code_length = 1544)
 *
 * (2) Check size of TableSwitch according to WCET tool
 * $test$>   make wcet  P1=test P2=wcet/devel P3=TableSwitch WCET_METHOD=measure | grep 'min-cache-size'
 * $stdout$> min-cache-size: wcet.devel.TableSwitch.test1()V 386
 * 
 * That's ok, because 386*4 = 1544
 * 
 * (3) Check execution time of TableSwitch:
 * $test$>   make jsim  P1=test P2=wcet/devel P3=TableSwitch | grep '\[TableSwitch\]'
 * $stdout$> measured-execution-time[TableSwitch]:1073
 * 
 * (4) Check WCET
 * 
 * $test$>   make java_app wcet  P1=test P2=wcet/devel P3=TableSwitch WCET_METHOD=measure |  grep 'wcet: (cost:'
 * $stdout$> wcet: (cost: 1132, execution: 347, cache: 785)
 */

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class TableSwitch {
	static volatile int x;
	/** tableswitch on 128 cases.
	 * Purpose: check that bytecode length is calculated correctly */
	private static void test1() {
		switch(x&0xff) {
		case 0: x=0; break;
		case 1: x=1; break;
		case 2: x=2; break;
		case 3: x=3; break;
		case 4: x=4; break;
		case 5: x=5; break;
		case 6: x=6; break;
		case 7: x=7; break;
		case 8: x=8; break;
		case 9: x=9; break;
		case 10: x=10; break;
		case 11: x=11; break;
		case 12: x=12; break;
		case 13: x=13; break;
		case 14: x=14; break;
		case 15: x=15; break;
		case 16: x=16; break;
		case 17: x=17; break;
		case 18: x=18; break;
		case 19: x=19; break;
		case 20: x=20; break;
		case 21: x=21; break;
		case 22: x=22; break;
		case 23: x=23; break;
		case 24: x=24; break;
		case 25: x=25; break;
		case 26: x=26; break;
		case 27: x=27; break;
		case 28: x=28; break;
		case 29: x=29; break;
		case 30: x=30; break;
		case 31: x=31; break;
		case 32: x=32; break;
		case 33: x=33; break;
		case 34: x=34; break;
		case 35: x=35; break;
		case 36: x=36; break;
		case 37: x=37; break;
		case 38: x=38; break;
		case 39: x=39; break;
		case 40: x=40; break;
		case 41: x=41; break;
		case 42: x=42; break;
		case 43: x=43; break;
		case 44: x=44; break;
		case 45: x=45; break;
		case 46: x=46; break;
		case 47: x=47; break;
		case 48: x=48; break;
		case 49: x=49; break;
		case 50: x=50; break;
		case 51: x=51; break;
		case 52: x=52; break;
		case 53: x=53; break;
		case 54: x=54; break;
		case 55: x=55; break;
		case 56: x=56; break;
		case 57: x=57; break;
		case 58: x=58; break;
		case 59: x=59; break;
		case 60: x=60; break;
		case 61: x=61; break;
		case 62: x=62; break;
		case 63: x=63; break;
		case 64: x=64; break;
		case 65: x=65; break;
		case 66: x=66; break;
		case 67: x=67; break;
		case 68: x=68; break;
		case 69: x=69; break;
		case 70: x=70; break;
		case 71: x=71; break;
		case 72: x=72; break;
		case 73: x=73; break;
		case 74: x=74; break;
		case 75: x=75; break;
		case 76: x=76; break;
		case 77: x=77; break;
		case 78: x=78; break;
		case 79: x=79; break;
		case 80: x=80; break;
		case 81: x=81; break;
		case 82: x=82; break;
		case 83: x=83; break;
		case 84: x=84; break;
		case 85: x=85; break;
		case 86: x=86; break;
		case 87: x=87; break;
		case 88: x=88; break;
		case 89: x=89; break;
		case 90: x=90; break;
		case 91: x=91; break;
		case 92: x=92; break;
		case 93: x=93; break;
		case 94: x=94; break;
		case 95: x=95; break;
		case 96: x=96; break;
		case 97: x=97; break;
		case 98: x=98; break;
		case 99: x=99; break;
		case 100: x=100; break;
		case 101: x=101; break;
		case 102: x=102; break;
		case 103: x=103; break;
		case 104: x=104; break;
		case 105: x=105; break;
		case 106: x=106; break;
		case 107: x=107; break;
		case 108: x=108; break;
		case 109: x=109; break;
		case 110: x=110; break;
		case 111: x=111; break;
		case 112: x=112; break;
		case 113: x=113; break;
		case 114: x=114; break;
		case 115: x=115; break;
		case 116: x=116; break;
		case 117: x=117; break;
		case 118: x=118; break;
		case 119: x=119; break;
		case 120: x=120; break;
		case 121: x=121; break;
		case 122: x=122; break;
		case 123: x=123; break;
		case 124: x=124; break;
		case 125: x=125; break;
		case 126: x=126; break;
		default:  x=127; break;
		}
	}

	static int ts, te, to;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		x=255;
		invoke();
		if (Config.MEASURE) {
			int dt = te-ts-to;
			System.out.print("measured-execution-time[TableSwitch]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test1();
	}

}
