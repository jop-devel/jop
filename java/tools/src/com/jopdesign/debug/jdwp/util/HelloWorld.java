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

package com.jopdesign.debug.jdwp.util;

/**
 * The widely known "Hello world" program.
 * 
 * In this case, this tiny program is being very useful since
 * it is one of the smallest programs that may be... debugged!
 * 
 * It is being used on this project as a debug target, to
 * help understand, develop and debug the JDWP module
 * for JOP.
 * 
 * Running it on a remote JVM allows to see and log the JDWP
 * messages, observe and test the debugger behaviour and
 * do a lot of different experiments related to debugging.
 * For instance, to learn the subtle differences between distinct
 * debuggers, such as Sun's native jdb, the Java jdb 
 * (a JPDA sample) and Eclipse's debugger.
 * 
 * It's interesting to see that debugging this 
 * small program under Eclipse 3.2 requires more 
 * than a hundred JDWP messsages to be sent,
 * from the debugger program to the debugee only.
 * Besides, another hundred or so messages needs to go 
 * back to the debugger in response. Just to launch
 * and perform three "simple" debug steps over "HelloWorld".
 * 
 * I believe this is one of the finest uses of "HelloWorld" 
 * I've ever seen (as well as for teaching, of course).
 * 
 * @author Paulo Abadie Guedes
 */
public class HelloWorld
{
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    System.out.println("Hello World!");
  }
}
