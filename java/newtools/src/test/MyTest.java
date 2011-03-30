/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MyTest {

    public static void main(String[] args) {
        
        Map map = new HashMap();
        Object[] arr = new Object[10];

        long l1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++ ) {
            arr[0] = "Test1";
            arr[1] = "Test1";
            arr[4] = "Test1";
            arr[2] = "Test1";
            arr[5] = "Test1";
            arr[3] = "Test1";
            arr[6] = "Test1";
            arr[4] = "Test1";
            arr[7] = "Test1";
        }
        long l2 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++ ) {
            map.put("i0", "Test1");
            map.put("i1", "Test1");
            map.put("i4", "Test1");
            map.put("i2", "Test1");
            map.put("i5", "Test1");
            map.put("i3", "Test1");
            map.put("i6", "Test1");
            map.put("i4", "Test1");
            map.put("i7", "Test1");
        }
        long l3 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++ ) {
            map.get("i0");
            map.get("i1");
            map.get("i4");
            map.get("i2");
            map.get("i5");
            map.get("i3");
            map.get("i6");
            map.get("i4");
            map.get("i7");
        }
        long l4 = System.currentTimeMillis();
        System.out.println(String.format("%d %d %d", l2-l1, l3-l2, l4-l3));

    }

}
