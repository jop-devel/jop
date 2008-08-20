/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Christof Pitter

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

package cmp;

import java.util.Hashtable;
import jbe.LowLevel;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;


public class HashtableBenchCMP implements Runnable {

	// Shared Variables
	public static int signal = 0; 
	public static Hashtable htable;
	public static DataClass[] data;
	public static int max = 256;
	static Object lock;
	public static int [] low_array;
	public static int [] high_array;
	
	int cpu_id;
	int low_bound;
	int high_bound;
	Hashtable hTable;
	
	public HashtableBenchCMP (int identity, int low, int high, Hashtable hashtable){
		cpu_id = identity;
		low_bound = low;
		high_bound = high;
		hTable = hashtable;
	}
	
	
	public static void main(String[] args) {		
		
		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;
		
		System.out.println("Hashtable Benchmark:");
			
		// Initialize the data Array 
		String string = new String("Test");
		data = new DataClass[max];
			
		for (int i=0; i<max; i++) 
		{
			data[i] = new DataClass(new Integer(i), string + i);
		}
			
		htable = new Hashtable(max);
			
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		
		// Calculates the bounds for each CPU
		low_array = new int [sys.nrCpu];
		high_array = new int [sys.nrCpu];
		
		int rest = max%sys.nrCpu;
		int div = max/sys.nrCpu;
		
		for (int j=0; j<sys.nrCpu; j++){
			if (j == 0){
				low_array[j] = j*div;
				if (rest != 0){
					high_array[j] = (j+1)*div;
					rest--;}
				else{
					high_array[j] = (j+1)*div-1;}
			}
			else{
				low_array[j] = high_array[j-1]+1;
				if (rest != 0){
					high_array[j] = high_array[j-1]+div+1;
					rest--;}
				else{
					high_array[j] = high_array[j-1]+div;
				}
			}		
		}
		
		
		for (int i=0; i<sys.nrCpu-1; i++) {
			Runnable r = new HashtableBenchCMP(i+1, low_array[i+1], high_array[i+1], htable);
			Startup.setRunnable(r, i);
		}
		
		// Start of measurement
		start = LowLevel.timeMillis();
					
		// Start of all other CPUs
		sys.signal = 1;
			
		// Fill Hashtable
		for (int j=low_array[0]; j<(high_array[0]); j++)
		{
			if (htable.containsKey(data[j].key)) {
			} else {
				htable.put(data[j].key, data[j].value);
			}
		}
			
		// Read Table
		for (int i=low_array[0]; i<high_array[0]; i++)
		{
			if (htable.contains(data[i].value))
			{
				data[i].value = (String)htable.get(data[i].key);
			}
		}
		
		// Delete Hashtable
		for (int i=low_array[0]; i<(high_array[0]); i++)
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
				if (signal == sys.nrCpu-1)
					break;
			}
		}

		// End of measurement
		stop = LowLevel.timeMillis();
		
		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop-start;
		System.out.println("TimeSpent: " + time);
	} 
	
	
	public void run() {
		// Fill Hashtable
		for (int j=low_bound; j<high_bound; j++)
		{
			if (hTable.containsKey(data[j].key)) {
			} else {
				hTable.put(data[j].key, data[j].value);
			}
		}
		
		// Read Table
		for (int i=low_bound; i<high_bound; i++)
		{
			if (hTable.contains(data[i].value))
			{
				data[i].value = (String)hTable.get(data[i].key);
			}
		}
		
		// Delete Hashtable
		for (int i=low_bound; i<high_bound; i++)
		{
			if (hTable.containsKey(data[i].key))
			{
				hTable.remove(data[i].key);
			}
		}
		
		synchronized(lock){
			signal++;}
		while(true);
	}
}
		