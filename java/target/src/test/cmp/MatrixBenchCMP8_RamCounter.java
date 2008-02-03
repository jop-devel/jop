package cmp;

import java.util.Random;

import jbe.LowLevel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class MatrixBenchCMP8_RamCounter {

	// Shared Variables
	public static int[][] arrayA;
	public static int[][] arrayB;
	public static int[][] arrayC;
	public static int rowCounter = 0;
	public static int endCalculation = 0;
	public static int M = 100; // Number of rows of A
	public static int N = 100; // Number of columns of A, number of rows of B
	public static int P = 100; // Number of columns of B	
	static Object lock;
	public static int test0 = 0;
	public static int test1 = 0;
	public static int test2 = 0;
	public static int test3 = 0;
	public static int test4 = 0;
	public static int test5 = 0;
	public static int test6 = 0;
	public static int test7 = 0;

	
	public static void main(String[] args) {		
		
		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			int count0 = 0;
			int count1 = 0;
			int us0 = 0;
			int us1 = 0;
			int count_result = 0;
			int us_result = 0;
			
			long seed = 13;
			initializeMultiplication(seed);
			
			System.out.println("Bandwidth:");
			
			// Startpoint of measuring
			count0 = Native.rdMem(Const.IO_RAMCNT);
			us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
			
			// Start CPUs
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			
			test0 = processCalculation();
			
			while(true)
			{
				synchronized(lock)
				{
					if (endCalculation == N)
						break;
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
		else
		{
			if (cpu_id == 0x00000001)            
			{
				test1 = processCalculation();
				while(true);
			}  
			else
			{
				if (cpu_id == 0x00000002) 
				{	
					test2 = processCalculation();
					while(true);
				}
				else
				{
					if (cpu_id == 0x00000003) 
					{	
						test3 = processCalculation();
						while(true);
					}
					else
					{
						if (cpu_id == 0x00000004) 
						{	
							test4 = processCalculation();
							while(true);
						}
						else
						{
							if (cpu_id == 0x00000005) 
							{	
								test5 = processCalculation();
								while(true);
							}
							else
							{
								if (cpu_id == 0x00000006) 
								{	
									test6 = processCalculation();
									while(true);
								}
								else
								{
									test7 = processCalculation();
									while(true);
								}
							}
						}
					}
				}
			}	
		}
			
	}
	
	static void initializeMultiplication(long seed)
	{
		// Initialize the Arrays
		int max = 10;			
		Random r = new Random(seed);
		
		arrayA = new int[M][N];
		arrayB = new int[N][P];
		arrayC = new int[M][P];
		
		for (int i=0; i<M; i++)
		{
			for (int j=0; j<N; j++)
			{
				arrayA[i][j]= (Math.abs(r.nextInt()))%max;
			}
		}
		
		for (int i=0; i<N; i++)
		{
			for (int j=0; j<P; j++)
			{
				arrayB[i][j]= (Math.abs(r.nextInt()))%max;
			}
		}
		
		for (int i=0; i<M; i++)
		{
			for (int j=0; j<P; j++)
			{
				arrayC[i][j]= 0;
			}
		}
	}
	
	static int processCalculation()
	{
		int myRow = 0;
		int counter = 0;
		
		while(true){
			synchronized(lock)
			{
				if (rowCounter == N){
					break;}
				else{
					myRow = rowCounter;
					rowCounter++;}
			}
			
			for (int i=0; i<P; i++)  // column
			{
				arrayC[myRow][i] = 0;
				
				for( int j=0; j<M; j++)
				{
					arrayC[myRow][i]= arrayC[myRow][i]+arrayA[myRow][j]*arrayB[j][i];
				}
			}
			
			synchronized(lock)
			{
				endCalculation++;
			}
			
			counter++;
		}
		return counter;
	}
}