/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Martin Schoeberl (martin@jopdesign.com)

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
package test;

import com.jopdesign.sys.Memory;

public class ScopeNesting {

	   Memory scMemory = new Memory(4096);

	   public void run(){

	       MyLogic logic = new MyLogic();

	       // When the enter() method returns, all the objects created within
	       // the run() method of the MyLogic instance should be removed, including the
	       // scoped memory areas created in logic.run().

	       for(int i=0; i<10000; i++){

	    	   // println(i) generates garbage
	           System.out.print(i);
	           System.out.println();
	           scMemory.enter(logic);
	       }
	   }

	   public static void main(String[] args) {

	       ScopeNesting myApp = new ScopeNesting();
	       myApp.run();
	   }

	   class MyLogic implements Runnable{

	       @Override
	       public void run() {
	           // TODO Auto-generated method stub
	           System.out.println("A");
	           Memory nestScMem = new Memory(512,512);
	           MyLogic_2 logic_2 = new MyLogic_2();
	           nestScMem.enter(logic_2);
	       }
	   }

	   class MyLogic_2 implements Runnable{

	       @Override
	       public void run() {
	           // TODO Auto-generated method stub
	           System.out.println("B");
	       }
	   }


}
