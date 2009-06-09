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
import com.jopdesign.sys.*;


/**
 * @author jack
 *
 */
public class HWMethTest {

	public static void main(String[] args) {
    HWMethTest h = new HWMethTest () ;
    
    h.Start () ;
  }

	public void Start() {
    final int max_mac_size = 10000 ;
    final int max_cycles = 100000 ;
    int [] array1 = new int [ max_mac_size ] ;
    int [] array2 = new int [ max_mac_size ] ;


    System.out.println("Let us begin.");

    mac_coprocessor m = new mac_coprocessor () ;
		int ts = Native.rdMem(Const.IO_CNT);
		int te = Native.rdMem(Const.IO_CNT);
		int to = te-ts;
    int icount = 0 ;

    for ( int mac_size = 1 ; mac_size < max_mac_size ; icount ++ )
    {
      int i, j, time, out, expect = 0;
      boolean error = false;
      int max_time = 0;
      int min_time = 1 << 30;
      int total_time = 0;

      for ( i = 0 ; i < mac_size ; i ++ )
      {
        array1 [ i ] = mac_size + 123 + ( i * 99 ) + ( i * i * 12 ) ;
        array2 [ i ] = mac_size + 456 + ( i * 78 ) + ( i * i * 9 ) ;
        expect += array1 [ i ] * array2 [ i ] ;
      }

      System.out.print(mac_size);
      System.out.print(" ");
      // Warmup method cache
      out = m.mac1(mac_size, array1, array2);

      for ( i = 0 ; i < max_cycles ; i ++ )
      {
        ts = Native.rdMem(Const.IO_CNT);
        out = m.mac1(mac_size, array1, array2);
        te = Native.rdMem(Const.IO_CNT);
        time = te - ts - to;
        if ( time > max_time )
        {
          max_time = time;
        }
        if ( time < min_time )
        {
          min_time = time;
        }
        total_time += time;
        if ( out != expect )
        {
          error = true;
          break ;
        }
      }

      if ( error )
      {
        System.out.println("Unexpected value!");
        break ;
      } 
      System.out.print(min_time);
      System.out.print(" ");
      System.out.print(total_time / max_cycles);
      System.out.print(" ");
      System.out.println(max_time);

      mac_size ++ ;
      if ( icount > 100 )
      {
        mac_size += mac_size / 4 ;
      }
    }
		System.out.println("That's all.");
	}
}

