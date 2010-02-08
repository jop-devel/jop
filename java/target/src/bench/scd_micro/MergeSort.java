package scd_micro;

import java.util.ArrayList;
/** Merge sort algorith, adopted from http://cs.joensuu.fi/~zhao/DAA2009/Mergesort.htm.
 *  STL's adaptive merge copies the first block into the tmp buffer, and so do we.
 *  On my system, it is much faster on random test data then the JDK implementation.
 *
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class MergeSort {
	
	//  For the benchmark, we know that data.size <= 2, and added according annotations.
	public static<T extends Comparable<T>> void sort(ArrayList<T> data) {
		if(data.size() > 2) {
			throw new Error("Merge sort was configured to handle at most 2 elements");
		}
		Object[] tmp = new Object[data.size()];     //@WCA loop=2
		for(int m = 1; m <= data.size(); m <<= 1) { //@WCA loop=2
			// merge sort with bucket size m
			for(int i = 0; i < data.size() - m; i += m*2 ) { //@WCA loop=1
				int start1,end1, start2,end2;
				start1 = i;
				end1 = start2 = i+m;				
				end2 = i + m*2;
				if(end2 > data.size()) end2 = data.size();
				// copy buffer
				for(int j = start1; j < end1; j++) { //@WCA loop=1
					tmp[j] = data.get(j); 
				}
				// merge tmp[start1,end1) and data[start2,end2)
				for(int j = start1; j < end2; j++) //@WCA loop=2
				{
					if(start1==end1) break; // Finished
					if(start2==end2) {
						data.set(j,(T) tmp[start1++]);
					} else {
						T v1 = (T) tmp[start1];
						T v2 = data.get(start2);
						if(v1.compareTo(v2) <= 0) {
							data.set(j,v1);
							start1++;
						} else {
							data.set(j,v2);
							start2++;
						}
					}
				}
			}
		}
	}
	// Testing: Run this on your desktop machine
//	private static class Entry<T extends Comparable<T>> 
//		implements Comparable<Entry<T>> {
//
//		public Entry(T r, int i) {
//			val = r;
//			stable = i;
//		}
//		T      val;
//		int    stable;
//		public int compareTo(Entry<T> o) {
//			return val.compareTo(o.val);
//		}
//		public boolean equals(Object o) {
//			Entry<?> oth = (Entry<?>) o;
//			return val.equals(oth.val) && stable==oth.stable;
//		}
//		public String toString() {
//			return String.format("Entry{ val = %s, stable = %d}",val.toString() ,stable);
//		}
//	}
//	private static final int TEST_COUNT = 100000;
//	public static void main(String[] argv) {
//		Random r = new java.util.Random(12341235);
//		ArrayList<Entry<Long>> testdata = new ArrayList<Entry<Long>>();
//		for(int i = 0; i < TEST_COUNT; i++) {
//			testdata.add(new Entry<Long>(r.nextLong() % (TEST_COUNT / 3),i));
//		}
//		long start ;
//		ArrayList<Entry<Long>> testin1 = new ArrayList<Entry<Long>>(testdata);
//		ArrayList<Entry<Long>> testin2 = new ArrayList<Entry<Long>>(testdata);
//
//		start = System.currentTimeMillis();
//		Collections.sort(testin1);
//		System.out.println("Stop Collections.sort: "+(System.currentTimeMillis()-start)+" ms");
//		start = System.currentTimeMillis();
//		sort(testin2);
//		System.out.println("Stop WCET sort: "+(System.currentTimeMillis()-start)+" ms");
//		for(int i = 0; i < TEST_COUNT; i++) {
//			Entry<Long> v1 = testin1.get(i);
//			Entry<Long> v2 = testin2.get(i);
//			if(TEST_COUNT<50) System.out.println(""+i+": "+v2);
//			if(v1 != v2) {
//				System.out.println("Mismatch at position "+i+": "+v1+" / "+v2);
//			}
//		}
//		
//	}
}
