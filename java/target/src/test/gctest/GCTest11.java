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

/*******************************************************************************

    An implementation of the Java Debug Wire Protocol (JDWP) for JOP
    Copyright (C) 2007 Paulo Abadie Guedes

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
    
*******************************************************************************/

package gctest;

import com.jopdesign.sys.*;

/**
 * GCTest11.java
 * 
 * A test to check if the GC is correctly collecting objects when
 * there are integers on the stack which are *not* actual references
 * (plain integers) but which may have, by chance, 
 * the same values as actual handlers. 
 * 
 * If this test fails, it shows it's necessary to use type information
 * also for the stack. Otherwise it may cause floating garbage in some
 * (maybe rare?) cases.
 * 
 * This test is based on class GCTest9.
 * Check also GC.push().
 * 
 * @author Paulo Abadie Guedes
 *
 * 28/01/2008 - 12:03:02
 * 
 */
public class GCTest11
{
  // this dummy value is necessary to make the object use 
  // some memory when created.
  public int dummyValue;
  
  public static void main(String s[])
  {
	// variables to hold memory levels during the test.
    int free1, free2, free3, free4;
    int i;
    
    GC.gc();
    System.out.println("GC");
    System.out.print("Free memory 1: before -> ");
    free1 = GC.freeMemory();
    System.out.println(free1);
    
    Object object = new GCTest11();
    
    // get the object handle and assign it to an int.
    // this is NOT a real object reference and should be ignored
    // by the GC during garbage collection. It's just a plain int.
    i = Native.toInt(object);
    
    System.out.print("Object handle: ");
    System.out.println(i);
    
    // now GC should not recycle it.
    GC.gc();
    
    System.out.print("Free memory 2: object is still reachable -> ");
    free2 = GC.freeMemory();
    System.out.println(free2);
    if(free1 <= free2)
    {
    System.out.println("FAIL. free memory 2 should be less than 1.");
    }
    
    // cut the reference here
    object = null;
    
    // now GC should recycle it!
    GC.gc();
    
    System.out.print("Free memory 3: should be equals to free memory 1. ->");
    free3 = GC.freeMemory();
    System.out.println(free3);
    
    if(free3 != free1)
    {
    System.out.println("FAIL. the object should have been collected.");
    }
    
    // now if the following line makes GC collect the object, GC is wrong!
    i = 0;
    
    // GC should have recycle it already!
    GC.gc();
    
    System.out.print("Free memory 4: ");
    free4 = GC.freeMemory();
    System.out.println(free4);
    if(free3 != free4)
    {
    System.out.println("FAIL. Free memory in 3) and 4) should be the same.");
    }
  }
}
