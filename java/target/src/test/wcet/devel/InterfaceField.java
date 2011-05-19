/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp <stefan@stefant.org>
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

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class InterfaceField {

    static int init() {
        return 2;
    }

    private interface TestIF {
        static final int CNT = init();
    }

    private static void test1() {
        int k = 0;
        for (int i = 0; i < TestIF.CNT; i++) {
            k++;
        }
    }

    static int ts, te, to;

    public static void main(String[] args) {
        ts = Native.rdMem(Const.IO_CNT);
        te = Native.rdMem(Const.IO_CNT);
        to = te - ts;
        invoke();
        if (Config.MEASURE) {
            int dt = te - ts - to;
            System.out.print("measured-execution-time:");
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

