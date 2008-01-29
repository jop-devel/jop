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
 * JOPConstants.java
 * 
 * Some constants related to JOP and the internal structure of the
 * compiled JOP file.
 * 
 * @author Paulo Abadie Guedes
 *
 * 21/01/2008 - 12:07:41
 * 
 */
public interface JOPConstants
{
  // size of the class header
  public static final int CLASS_HEADER_SIZE = 5;
  
  // constants related to the internal structure of class headers 
  public static final int CLASS_OFFSET_INSTANCE_SIZE = 0;
  public static final int CLASS_OFFSET_STATIC_PRIMITIVE_FIELDS = 1;
  public static final int CLASS_OFFSET_GC_INFO = 2;
  public static final int CLASS_OFFSET_SUPERCLASS_POINTER = 3;
  public static final int CLASS_OFFSET_INTERFACE_TABLE_POINTER = 4;
}
