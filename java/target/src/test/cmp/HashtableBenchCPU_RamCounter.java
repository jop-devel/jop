package cmp;

import java.util.Hashtable;
import jbe.LowLevel;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


public class HashtableBenchCPU_RamCounter {

	// Shared Variables
	public static boolean wait = false;
	public static Hashtable htable;
	public static DataClass[] data;
	public static int max = 256;
	public static int test0 = 0;
	public static int test1 = 0;
	public static int test2 = 0;
	public static int test3 = 0;
	static Object lock;
	
	
	public static void main(String[] args) {		
		
			// Initialization for benchmarking 
			int count0 = 0;
			int count1 = 0;
			int us0 = 0;
			int us1 = 0;
			int count_result = 0;
			int us_result = 0;
			
			
			
			// Initialize the data Array 
			String string = new String("Test");
			data = new DataClass[max];
			
			for (int i=0; i<max; i++) 
			{
				data[i] = new DataClass(new Integer(i), string + i);
			}
			
			// Test Hashtable
			System.out.println("Hashtable test");
			htable = new Hashtable(max);

			
			// Startpoint of measuring
			count0 = Native.rdMem(Const.IO_RAMCNT);
			us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
					
			// Fill Hashtable
			for (int j=0; j<max; j++)
			{
				if (htable.containsKey(data[j].key)) {
					//System.out.println("Contains Key: " + data[j].key);
					test0++;
					//System.out.println("Hashtable Size: " + htable.size() + " "+ j);
				} else {
					htable.put(data[j].key, data[j].value);
				}
			}
			
			// Read Table
			for (int i=0; i<max; i++)
			{
				if (htable.contains(string + i))
				{
					data[i].value = (String)htable.get(data[i].key);
				}
			}
			
			// Delete Hashtable
			for (int i=0; i<max; i++)
			{
				if (htable.containsKey(data[i].key))
				{
					htable.remove(data[i].key);
				}
			}
			
			// Endpoint of measuring
			us1 = Native.rdMem(Const.IO_CNT); // Clockcycles
			count1 = Native.rdMem(Const.IO_RAMCNT);
			
			count_result = count1 - count0;
			us_result = us1 - us0;
				
			LowLevel.msg("RAM Accesses:", count_result);
			LowLevel.lf();
			LowLevel.msg("Time us:", us_result);
			LowLevel.lf();
			LowLevel.msg("in %:", count_result/(us_result/100));
			LowLevel.lf();
		                                  
	}	
}