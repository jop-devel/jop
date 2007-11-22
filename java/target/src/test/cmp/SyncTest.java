/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cmp;

import joprt.*;
import util.Timer;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import cmp.IntegerClass;

public class SyncTest {		
	
	static Object lock1;
	static IntegerClass whoAmI;
	
	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		int test = 0;
		
		if (cpu_id == 0x00000000)
		{
			whoAmI = new IntegerClass();
			lock1 = new Object();
			whoAmI.numberArray = new int [10];
			
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
			System.out.println("Depp!!!");
		
			while(true)
			{
				/*synchronized(lock1){
					for( int i=0; i<10; i++){
						print_02d(whoAmI.numberArray[i]);
					}
				}
					
				/*	for( int i=0; i<10; i++){
						whoAmI.numberArray[i] = i;
					}
					
					for( int i=0; i<10; i++){
						print_02d(whoAmI.numberArray[i]);
					}
				}*/
				synchronized(lock1){
					for( int i=0; i<10; i++){
							print_02d(whoAmI.numberArray[i]);
						}
				
					
					/*for( int i=0; i<10; i++){
							whoAmI.numberArray[i] = i;
						}
					
					/*for( int i=0; i<10; i++){
							print_02d(whoAmI.numberArray[i]);
						}*/
				//}
				}
				
				for(int i=0; i<10000; i++);
				
				
			}						
		}
		else
		{
			if (cpu_id == 0x00000001)
			{
				test = 1;
				
				while(true)
				{	
					/*synchronized(lock1){
						for( int i=0; i<5; i++){
							whoAmI.numberArray[i] = i+10;		
						}
					}	*/
					for( int i=0; i<50; i++);	
				}
			}
		}
	}
	
	static void print_02d(int i) {
		int j;
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
		print_char(i+'0');
	}
	
	static void print_char(int i) {

		System.out.print((char) i);
		/*
		wait_serial();
		Native.wr(i, Const.IO_UART);
		*/
	}
	
}