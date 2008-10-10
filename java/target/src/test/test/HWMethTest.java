/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Jack Whitham

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

package test;


import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.sys.Native;


/**
 * @author jack
 *
 * JOP can also say 'Hello World'
 */
public class HWMethTest {

	public static void main(String[] args) {
        HWMethTest h = new HWMethTest () ;
        
        h.Start () ;
    }

	public void Start() {
        int mac_size = 10000 ;
        int [] array1 = new int [ mac_size ] ;
        int [] array2 = new int [ mac_size ] ;
        int i, expect, j;


		System.out.println("Let us begin.");

        mac_coprocessor m = new mac_coprocessor () ;

        for ( j = 0 ; j < 10 ; j ++ )
        {
            // make the data to be mac'ed
            
            expect = 0 ;
            for ( i = 0 ; i < mac_size ; i ++ )
            {
                array1 [ i ] = 123 + ( i * ( 99 + j )) + ( i * i * 12 ) ;
                array2 [ i ] = 456 + ( i * 78 ) + ( i * i * ( 9 + j )) ;
                expect += array1 [ i ] * array2 [ i ] ;
            }
            System.out.print("Test: ");
            System.out.println(m.mac1(mac_size, array1, array2) - expect);
        }
		System.out.println("That's all.");
	}
}

