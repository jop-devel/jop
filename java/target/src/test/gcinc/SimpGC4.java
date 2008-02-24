/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package gcinc;
import com.jopdesign.sys.GC;
public class SimpGC4 implements Runnable {

	public myList list;
	public boolean test;
	private typeA myReference;
	int length,nr;
	public SimpGC4(int i) {
		length = i;
		}
	public void run() {
		
			if(test)
				{
				if (!myReference.testYourself(length)){
				throw new Error("Object is in wrong state");}
				synchronized(monitor)
				{shared=myReference;}
				
				myReference=null;
				test=false;
				}
			
			//System.out.println("running");
			if (SimpGC4.shared!=null){
			synchronized(monitor){
			myReference=SimpGC4.shared;
			SimpGC4.shared=null;
				}
			test=true;
			}
		}
		
	

	static typeA shared;
	static SimpGC4 a,b,c;
	static Object monitor;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpGC4(22);
		b = new SimpGC4(22);
		c = new SimpGC4(22);
		monitor=new Object();
		SimpGC4.shared=new typeA(22);
		
		for (;;) {
			a.run();
			System.gc();
			b.run();
			System.gc();
			c.run();
			System.gc();
		}
	}

}	