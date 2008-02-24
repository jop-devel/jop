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
public class SimpGC5 {

	public static void main(String[] args) {
		
		//Warning: required space grows with sum(2expn), n=0,1,2 ...
		
		SimpleTree st= new SimpleTree(8);
		
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		
		//Make this a couple of times 
		st=st.getLeftSubtree();
		st=st.getRightSubtree();
		st=st.getLeftSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		st=st.getRightSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		st=st.getLeftSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		System.out.println("Everything is fine");
	}

}
