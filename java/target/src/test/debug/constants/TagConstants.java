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

package debug.constants;

/**
 * TagConstants.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 23/05/2007 - 19:24:27
 *
 */
public interface TagConstants
{
  public static final byte ARRAY = 91;
  public static final byte BYTE = 66;
  public static final byte CHAR = 67;
  public static final byte OBJECT = 76;
  public static final byte FLOAT = 70;
  public static final byte DOUBLE = 68;
  public static final byte INT = 73;
  public static final byte LONG = 74;
  public static final byte SHORT = 83;
  public static final byte VOID = 86;
  public static final byte BOOLEAN = 90;
  public static final byte STRING = 115;
  public static final byte THREAD = 116;
  public static final byte THREAD_GROUP = 103;
  public static final byte CLASS_LOADER = 108;
  public static final byte CLASS_OBJECT = 99;
}
