package cmp;

import java.lang.Math;
import java.util.Hashtable;
import java.util.Random;
import jbe.LowLevel;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


public class HashtableBenchmark {

	// Shared Variables
	public static boolean wait = false;
	public static Hashtable htable;
	public static DataClass[] data;
	public static int max = 100;
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
			
			// Print DataClass
			/*for(int i=0; i<max; i++)
			{
				System.out.println("Key: " + data[i].key + " Value: " + data[i].value);
			}*/
			
			// Test Hashtable
			System.out.println("Hashtable test");
			htable = new Hashtable(max);
			
			start = LowLevel.timeMillis();
			System.out.println("StartTime: " + start);
			
			
			// Start CPUs
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
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

			// wait = true;
			// Wait for CPU1
			while(true){
				synchronized(lock)
				{
					if (wait == true)
						break;
				}
			}
			
			System.out.println("Hallo!");
			stop = LowLevel.timeMillis();
			System.out.println("StopTime: " + stop);
			time = stop-start;
			
			System.out.println("Hashtable Size: " + htable.size());
			
			// Read out Hashtable
			for (int i = 0; i<max; i++)
			{
				if (htable.containsKey(data[i].key)) 
				{
					String s1 = new String((String)htable.get(data[i].key));
					System.out.println("Key: " + data[i].key + " Value: " + s1);
				} 
				else 
				{
					System.out.println("Key: " + data[i].key + " not contained!");
				}
			}

			//System.out.println(htable.toString());
			//htable.clear();
			//System.out.println("Hashtable Cleared!");
			System.out.println("Hashtable test END");
			
			System.out.println("StartTime: " + start);
			System.out.println("StopTime: " + stop);
			System.out.println("TimeSpent: " + time);
			System.out.println("CPU0: " + test0 + " mal");
			System.out.println("CPU1: " + test1 + " mal");
			
			deleteInsertKeys(3);
			System.out.println("Size of Hashtable:" + htable.size());
			searchValue(15);
			System.out.println("Deleted Value Keys: " + test2);
			insertOverwriteKeys(57);
			System.out.println("Inserted Keys: " + test3);
		} 
		else                                       
		{		                                   
			if (cpu_id == 0x00000001)            
			{	
				for (int i=max-1; i>=0; i--)
				{
					if (htable.containsKey(data[i].key)) {
						//System.out.println("Contains");
						test1++;
					} else {
						htable.put(data[i].key, data[i].value);
					}
				}
				synchronized(lock){
					wait = true;}
				
				while(true);
			}  
			else
			{
				while(true);
			}
		}                                      
	}
	
	
	static void insertOverwriteKeys(long seed)
	{
		int number = 0; 
		Random r = new Random(seed);
		String string = new String("Test");
		
		for (int j=0; j<max; j++)
		{
			for (int i=0; i<max; i++)
			{
				number = (Math.abs(r.nextInt()))%max;
				htable.put(new Integer(number), string + number);
				test3++;
			}
		}
	}
	

	static void deleteInsertKeys(long seed)
	{
		int number = 0; 
		Random r = new Random(seed);
		String string = new String("Test");
		
		for (int j=0; j<max; j++)
		{
			for (int i=0; i<max; i++)
			{
				number = (Math.abs(r.nextInt()))%max;
				//System.out.println("Key: " + number);
				if (htable.containsKey(new Integer(number)))
				{
					htable.remove(new Integer(number));
				}
				else
				{
					htable.put(new Integer(number), string + i);
				}
			}	
		}
	}
	
	static void searchValue(long seed)
	{
		int number = 0;
		Random r = new Random(seed);
		String string = new String("Test");
		
		
		for (int j=0; j<max; j++)
		{
			for (int i=0; i<max; i++)
			{
				number = (Math.abs(r.nextInt()))%max;
				//System.out.println("Key: " + number);
				if (htable.contains(string + number))
				{
					test2++;
					htable.remove(new Integer(number));
				}
			}
		}
	}

}


/*if (htable.contains(i)) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}

if (!htable.containsKey(new Object())) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}

if (!htable.isEmpty()) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}

Enumeration hashenum;

// TODO: JOP says readMem: wrong address

// hashenum = vc.elements();
// System.out.println("Hashtable value Enumeration:");
// while (hashenum.hasMoreElements()) {
// // System.out.println(hashenum.nextElement().toString());
// }
//
// hashenum = vc.keys();
// System.out.println("Hashtable key Enumeration:");
// while (hashenum.hasMoreElements()) {
// System.out.println(hashenum.nextElement().toString());
// }

if (htable.get("one").equals(i)) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}
if (htable.size() == 3) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}
if (i.equals(htable.remove("one"))) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}
if (!(htable.size() == 3)) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}
if (htable.size() == 2) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}

System.out.println("Hashtable.toString():");
System.out.println(htable.toString());

htable.clear();
if (htable.isEmpty()) {
System.out.println("OK");
} else {
System.out.println("ERROR Hashtable");
}*/