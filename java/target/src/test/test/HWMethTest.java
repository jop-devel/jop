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
    
    h.HWM_Start () ;
    h.MAC_Start () ;
  }

  public final static int max_mac_size = 10000 ;
  public final static int max_cycles = 100 ;
  public int [] array1 ;
  public int [] array2 ;
  public mac_coprocessor m ;
  public bitcount_maxsearch bcms ;

	public void MAC_Start() 
  {
    array1 = new int [ max_mac_size + 1 ] ;
    array2 = new int [ max_mac_size + 1 ] ;

    m = mac_coprocessor.getInstance () ;
    int icount = 0 ;
    boolean error = false ;

    for ( int mac_size = 1 ; 
          ( mac_size < max_mac_size ) && ! error ; icount ++ )
    {
      error = MAC_Measure ( mac_size ) ;
      mac_size ++ ;
      if ( icount > 100 )
      {
        mac_size += mac_size / 4 ;
      }
    }
    if ( ! error )
    {
      MAC_Measure ( max_mac_size ) ;
    }
	}

  public boolean MAC_Measure( int mac_size )
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

  public static int [] lut ;
  public final int max_test_size = max_mac_size ;
  public int [] test_vector ;

  public void HWM_Start ()
  {
    // Build LUT
    int i ;
    int [] data = new int [ 1 ] ;
    lut = new int [ 256 ] ;

    for ( i = 0 ; i < 256 ; i ++ )
    {
      data [ 0 ] = i ;
      lut [ i ] = bit_count1 ( 1 , data ) ;
    }

    bcms = new bitcount_maxsearch () ;
    test_vector = new int [ max_test_size + 1 ] ;
    boolean error = false;

    for ( i = 1 ; i <= max_cycles ; i ++ )
    {
      Prepare_Noisy_Vector ( i ) ;
      error = HWM_SW_Measure ( i ) ;
      if ( error ) return ;
    }
    System.out.print("v=0 ");
    Prepare_Fill_Vector ( max_test_size , 0 ) ;
    error = HWM_SW_Measure ( max_test_size ) ;
    if ( error ) return ;

    System.out.print("v=0..9 ");
    for ( i = 0 ; i < 10 ; i ++ )
    {
      test_vector[ i ] = i ;
    }
    error = HWM_SW_Measure ( max_test_size ) ;
    if ( error ) return ;

    Prepare_Fill_Vector ( max_test_size , 0x7fffffff ) ;
    System.out.print("v=-1 ");
    error = HWM_SW_Measure ( max_test_size ) ;
    if ( error ) return ;

    System.out.print("v=Noise ");
    Prepare_Noisy_Vector ( max_test_size ) ;
    error = HWM_SW_Measure ( max_test_size ) ;
    if ( error ) return ;

    System.out.print("v=1..N ");
    for ( i = 0 ; i < max_test_size ; i ++ )
    {
      test_vector [ i ] = i + 1 ;
    }
    error = HWM_SW_Measure ( max_test_size ) ;
    if ( error ) return ;
  }

  public void Prepare_Fill_Vector ( int test_size , int fill )
  {
    for ( int i = 0 ; i < test_size ; i ++ )
    {
      test_vector [ i ] = fill ;
    }
  }

  public void Prepare_Noisy_Vector ( int test_size )
  {
    for ( int i = 0 ; i < test_size ; i ++ )
    {
      test_vector [ i ] = test_size + 
        ((((((( 1 + i ) * i ) + 3 ) * i ) + 7 ) * i ) + 11 ) ;
    }
  }

  public boolean HWM_SW_Measure( int test_size )
  {
    int ts = Native.rdMem(Const.IO_CNT);
    int te = Native.rdMem(Const.IO_CNT);
    int to = te-ts;
    boolean error = false;

    System.out.print(test_size);
    test_vector [ test_size ] = 0x7fffffff ;

    mac (test_size, test_vector, test_vector);
    ts = Native.rdMem(Const.IO_CNT);
    mac (test_size, test_vector, test_vector);
    te = Native.rdMem(Const.IO_CNT);
    System.out.print(" ");
    System.out.print(te - ts - to);

    int bc1 = bit_count1 ( test_size , test_vector ) ;
    ts = Native.rdMem(Const.IO_CNT);
    bit_count1 ( test_size , test_vector ) ;
    te = Native.rdMem(Const.IO_CNT);
    System.out.print(" ");
    System.out.print(te - ts - to);

    int bc2 = bit_count2 ( test_size , test_vector ) ;
    ts = Native.rdMem(Const.IO_CNT);
    bit_count2 ( test_size , test_vector ) ;
    te = Native.rdMem(Const.IO_CNT);
    System.out.print(" ");
    System.out.print(te - ts - to);

    int sm = search_max ( test_size , test_vector ) ;
    ts = Native.rdMem(Const.IO_CNT);
    search_max ( test_size , test_vector ) ;
    te = Native.rdMem(Const.IO_CNT);
    System.out.print(" ");
    System.out.print(te - ts - to);

    int bchw1 = bcms.bitcount ( test_size , test_vector ) ;
    ts = Native.rdMem(Const.IO_CNT);
    int bchw2 = bcms.bitcount ( test_size , test_vector ) ;
    te = Native.rdMem(Const.IO_CNT);
    int bchw_time = te - ts - to;

    int smhw1 = bcms.maxsearch ( test_size , test_vector ) ;
    ts = Native.rdMem(Const.IO_CNT);
    int smhw2 = bcms.maxsearch ( test_size , test_vector ) ;
    te = Native.rdMem(Const.IO_CNT);
    int smhw_time = te - ts - to;

    System.out.print(" ");
    System.out.print(bchw_time);
    System.out.println(""); 

    if ( smhw_time != bchw_time )
    {
      System.out.print("Discrepancy on smhw/bchw time: ");
      System.out.print(bchw_time);
      System.out.print(" ");
      System.out.print(smhw_time);
      System.out.println(""); 
      error = true ;
    }

    if (( bc1 != bc2 )
    || ( bc1 != bchw1 )
    || ( bc1 != bchw2 ))
    {
      System.out.print("Discrepancy on bitcount results: ");
      System.out.print(bc1);
      System.out.print(" ");
      System.out.print(bc2);
      System.out.print(" ");
      System.out.print(bchw1);
      System.out.print(" ");
      System.out.print(bchw2);
      System.out.println("");
      error = true ;
    }
    if (( sm != smhw1 )
    || ( sm != smhw2 ))
    {
      System.out.print("Discrepancy on searchmax results: ");
      System.out.print(sm);
      System.out.print(" ");
      System.out.print(smhw1);
      System.out.print(" ");
      System.out.print(smhw2);
      System.out.println("");
      error = true ;
    }
    return error ;
  }

  public int bit_count1(int size, int[]data) {
    int count = 0;
    for ( int i = 0 ; i < size ; i ++ ) // @WCA loop<=10000
    { 
      int d = data [ i ];
      for ( int j = 0 ; j < 32 ; j ++ ) // @WCA loop=32
      {
        if (( d & 1 ) == 1 ) count ++ ;
        d = d >> 1 ;
      }
    }
    return count;
  }

  public int bit_count2(int size, int[]data) {
    int count = 0;
    for ( int i = 0 ; i < size ; i ++ ) // @WCA loop<=10000
    { 
      int d = data [ i ];
      for ( int j = 0 ; j < 4 ; j ++ ) // @WCA loop=4
      {
        count += lut [ d & 255 ] ;
        d = d >> 8 ;
      }
    }
    return count;
  }

  public int search_max(int size, int[]data) {
    int max = 0;
    for ( int i = 0 ; i < size ; i ++ ) // @WCA loop<=10000
    { 
      int d = data [ i ];
      if ( d > max ) max = d ;
    }
    return max ;
  }

	public int mac(int size, int[] array1, int[] array2) {
    int val = 0;
    for ( int i = 0 ; i < size ; i ++ ) // @WCA loop<=10000
    {
      val += array1 [ i ] * array2 [ i ] ;
    }
		return val ;
	}
}

