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


import com.jopdesign.io.SerialPort;
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

    private SerialPort control_channel ;

	public void Start() {
        int mac_size = 10000 ;
        int [] array1 = new int [ mac_size ] ;
        int [] array2 = new int [ mac_size ] ;
        int i , expect , got , header ;

		System.out.println("Let us begin.");

        JeopardIOFactory factory = JeopardIOFactory.getJeopardIOFactory();
        control_channel = factory.getControlPort();

        // make the data to be mac'ed
        expect = 0 ;
        for ( i = 0 ; i < mac_size ; i ++ )
        {
            array1 [ i ] = 123 + ( i * 99 ) + ( i * i * 12 ) ;
            array2 [ i ] = 456 + ( i * 78 ) + ( i * i * 9 ) ;
            expect += array1 [ i ] * array2 [ i ] ;
        }
		System.out.print("Expecting result: ");
		System.out.println(expect);

		System.out.print("Hardware versions: " );
        for ( i = 0 ; i < 8 ; i ++ )
        {
            System.out.print("device ");
            System.out.print(i&3);
            System.out.print(" ");
            System.out.println(QueryCPVersion(i&3));
        }

		System.out.print("Result from CP1: ");
		System.out.println(MAC(1, array1, array2));
		System.out.print("Result from CP2: ");
		System.out.println(MAC(2, array1, array2));

        array1 [ 0 ] = 0;
		System.out.print("Result after change: ");
		System.out.println(MAC(1, array1, array2));
		System.out.println("That's all.");
	}
    /* SRC ID & DEST ID & TYPE & SIZE 
     * 1 version query
     * 2 version response
     * 3 method call
     * 4 method return
     * */

    public int QueryCPVersion ( int cp_number )
    {
        // header: query version (cmd 1) to co-processor cp_number
        // with zero words in the payload.
        SendCCMessage ( ( cp_number << 16 ) | 0x0100 ) ;

        int header = ReceiveCCMessage () ;
        int size = header & 0xff ;

        if ((( 0xff & ( header >> 24 )) != cp_number ) 
                /* src should be cp_number */
        || (( 0xff & ( header >> 16 )) != 0 ) /* dest should be 0 */
        || (( 0xff & ( header >> 8 )) != 2 ) /* type should be 2 */
        || ( size != 1 )) /* size should be 1 */
        {
            while ( size != 0 )
            {
                ReceiveCCMessage () ;
                size--;
            }
            return -1;
        }
        /* next word is the version */
        return ReceiveCCMessage () ;
    }

    public int MAC ( int cp_number , int [] array1 , int [] array2 )
    {
        int mac_size = array1 . length ;
        //assert array2 . length == mac_size ;

        // header: call method (cmd 3) to co-processor cp_number
        // with four words in the payload.
        SendCCMessage ( 0x0304 | ( cp_number << 16 ) ) ; 
        SendCCMessage ( 1 ) ;                       // method 1: MAC
        SendCCMessage ( Dereference ( array2 ) ) ;  // parameter 1
        SendCCMessage ( Dereference ( array1 ) ) ;  // parameter 2
        SendCCMessage ( mac_size ) ;                // parameter 3

        // MAC runs...

        int header = ReceiveCCMessage () ;
        // header: method return (cmd 4) to co-processor zero (the CPU)
        // with one word in the payload.
        //assert ( header & 0xffffff ) == 0x401 ;
        // source of this message is co-processor cp_number
        //assert ( header >> 24 ) == cp_number ;

        // return payload
        return ReceiveCCMessage () ;
    }

    public void SendCCMessage ( int word )
    {
        while ( ! control_channel . txEmpty () ) {}
        control_channel . write ( word ) ;
    }

    public int ReceiveCCMessage ()
    {
        while ( ! control_channel . rxFull () ) {}
        return control_channel . read () ;
    }
                    
    public int Dereference ( int [] a )
    {
        return Native . rdMem ( Native . toInt ( a ) ) ;
    }
}

