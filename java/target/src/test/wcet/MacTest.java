/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Jack Whitham

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

import com.jopdesign.sys.*;

public class MacTest {

	public static void main(String[] args) {
		measure();
	}

  public static final int mac_size = 10000 ;
  public static int [] array1 ;
  public static int [] array2 ;
  public static int expect ;

  // Initialise arrays and compute expected result
  static {
    array1 = new int [ mac_size ] ;
    array2 = new int [ mac_size ] ;
    expect = 0 ;
    for ( int i = 0 ; i < mac_size ; i ++ )
    {
      array1 [ i ] = 123 + ( i * 99 ) + ( i * i * 12 ) ;
      array2 [ i ] = 456 + ( i * 78 ) + ( i * i * 9 ) ;
      expect += array1 [ i ] * array2 [ i ] ;
    }
  }

  // The task, for WCET measurement purposes
	public static int measure() {
    int val = - expect ;
    for ( int i = 0 ; i < mac_size ; i ++ )
    {
      val += array1 [ i ] * array2 [ i ] ;
    }
		return val ;
	}
}

