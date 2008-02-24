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
public class SimpGC6  {

	private SimpGC6 reference;
	public void setReference(SimpGC6 simp){
	reference=simp;
	
	}
	
	
	public void createChainedObjects(){
		SimpGC6 ref1,ref2;
		ref1=new SimpGC6();
		ref2=new SimpGC6();
		ref1.setReference(ref2);
		ref2.setReference(ref1);
	}
	
	
	public static void main(String[] args) {
		
		SimpGC6 sgc=new SimpGC6();
		
		//Start our experiment with a clean heap
		System.out.println("call GC1");
		GC.gc();
		
		//Measure how many objects fit into memory-make GC trigger
		for(int i=0; i<9730; i++){
			new myObject();
			System.out.print("nc:");
			System.out.print(i);
			System.out.print(" ");
		}
		//Clean objects remaining from last for
		System.out.println("call GC2");
		GC.gc();		
		
		//Create chained objects
		for(int i=0; i<5000; i++){
		sgc.createChainedObjects();
		}
		//Clean them
		System.out.println("call GC3");
		GC.gc();	
		
		//If the objects were cleaned GC should trigger at the same point as
		//in the first loop
		for(int i=0; i<9730; i++){
			new myObject();
			System.out.print("sc:");
			System.out.print(i);
		}
		
		
	}

}