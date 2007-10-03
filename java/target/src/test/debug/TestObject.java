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

package debug;

/**
 * TestObject.java
 * 
 * A simple object to test direct field access.
 * 
 * @author Paulo Guedes
 * 21/05/2007 - 16:19:23
 *
 */
public class TestObject
{
  public static int staticValue = 0;
  
  public long teste;
  
  private int value;
  
  public int value2;
  public int value3;
  public int value4;
  
  public TestObject()
  {
    value = 0;
    
    value2 = 0;
    value3 = 0;
    value4 = 0;
    teste = 0;
  }

  public int getValue()
  {
    return value;
  }

  public void setValue(int x)
  {
    this.value = x;
  }
}
