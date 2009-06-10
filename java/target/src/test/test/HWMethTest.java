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

  public final static int max_mac_size = 10000 ;
  public final static int max_cycles = 100 ;
  private int [] array1 ;
  private int [] array2 ;
  private mac_coprocessor m ;

	public void Start() {
    array1 = new int [ max_mac_size + 1 ] ;
    array2 = new int [ max_mac_size + 1 ] ;


    System.out.println("Let us begin.");

    m = new mac_coprocessor () ;
    int icount = 0 ;
    boolean error = false ;

    for ( int mac_size = 1 ; 
          ( mac_size < max_mac_size ) && ! error ; icount ++ )
    {
      error = Measure ( mac_size ) ;
      mac_size ++ ;
      if ( icount > 100 )
      {
        mac_size += mac_size / 4 ;
      }
    }
    if ( ! error )
    {
      Measure ( max_mac_size ) ;
    }
		System.out.println("That's all.");
	}

  private boolean Measure( int mac_size )
  {
    int i, expect = 0;
    boolean error = false;

    for ( i = 0 ; i < mac_size ; i ++ )
    {
      array1 [ i ] = mac_size + 123 + ( i * 99 ) + ( i * i * 12 ) ;
      array2 [ i ] = mac_size + 456 + ( i * 78 ) + ( i * i * 9 ) ;
      expect += array1 [ i ] * array2 [ i ] ;
    }
    // sentinel: don't mac this!
    array1 [ mac_size ] = 20576 ;
    array2 [ mac_size ] = 6 ;

    int ts = Native.rdMem(Const.IO_CNT);
    int te = Native.rdMem(Const.IO_CNT);
    int to = te-ts;

    System.out.print(mac_size);
    System.out.print(" ");

    int max_time = 0;
    int min_time = 1 << 30;
    int total_time = 0;

    // Warmup method cache
    int out = m.mac1(mac_size, array1, array2);

    for ( i = 0 ; i < max_cycles ; i ++ )
    {
      ts = Native.rdMem(Const.IO_CNT);
      out = m.mac1(mac_size, array1, array2);
      te = Native.rdMem(Const.IO_CNT);
      int time = te - ts - to;
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
      System.out.print(" - Unexpected value! hw ");
      System.out.print(out);
      System.out.print(" expect ");
      System.out.println(expect);
      return true;
    } 
    System.out.print(min_time);
    System.out.print(" ");
    System.out.print(total_time / max_cycles);
    System.out.print(" ");
    System.out.println(max_time);
    return false;
  }
}

