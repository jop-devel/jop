package cmp;

import java.util.Hashtable;
import jbe.LowLevel;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class HashtableBenchCMP2 {

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
		
		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			// Initialization for benchmarking 
			int start = 0;
			int stop = 0;
			int time = 0;
			
			// Initialize the data Array 
			String string = new String("Test");
			data = new DataClass[max];
			
			for (int i=0; i<max; i++) 
			{
				data[i] = new DataClass(new Integer(i), string + i);
			}
			
			htable = new Hashtable(max);
			
			System.out.println("Application benchmarks:");
			
			start = LowLevel.timeMillis();
					
			// Start CPUs
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
			// Fill Hashtable
			for (int j=0; j<(max/2); j++)
			{
				if (htable.containsKey(data[j].key)) {
				} else {
					htable.put(data[j].key, data[j].value);
				}
			}
			
			// Read Table
			for (int i=0; i<max/2; i++)
			{
				if (htable.contains(string + i))
				{
					data[i].value = (String)htable.get(data[i].key);
				}
			}
			
			// Delete Hashtable
			for (int i=0; i<(max/2); i++)
			{
				if (htable.containsKey(data[i].key))
				{
					htable.remove(data[i].key);
				}
			}

			while(true){
				util.Timer.usleep(500);
				synchronized(lock)
				{
					if (wait == true)
						break;
				}
			}

			stop = LowLevel.timeMillis();
			
			System.out.println("StartTime: " + start);
			System.out.println("StopTime: " + stop);
			time = stop-start;
			System.out.println("TimeSpent: " + time);
		} 
		else                                       
		{		                                   
			if (cpu_id == 0x00000001)            
			{
				String string = new String("Test");
				// Fill Hashtable
				for (int j=max/2; j<max; j++)
				{
					if (htable.containsKey(data[j].key)) {
					} else {
						htable.put(data[j].key, data[j].value);
					}
				}
				
				// Read Table
				for (int i=max/2; i<max; i++)
				{
					if (htable.contains(string + i))
					{
						data[i].value = (String)htable.get(data[i].key);
					}
				}
				
				// Delete Hashtable
				for (int i=(max/2); i<max; i++)
				{
					if (htable.containsKey(data[i].key))
					{
						htable.remove(data[i].key);
					}
				}
				
				synchronized(lock){
					wait = true;}
				
				while(true);
			}  
		}                                      
	}	
}