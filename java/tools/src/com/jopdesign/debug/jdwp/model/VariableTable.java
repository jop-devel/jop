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

package com.jopdesign.debug.jdwp.model;

import java.util.Vector;

/**
 * 
 * VariableTable.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 14:21:02
 *
 */
public class VariableTable
{
  private Vector table;
  
  // The number of words in the frame used by arguments. 
  //Eight-byte arguments use two words; all others use one.
  private int   argCnt;    
  private int   slots;
  
  public VariableTable()
  {
    table = new Vector();
  }

  public int getArgCnt()
  {
    return argCnt;
  }

  public void setArgCnt(int argCnt)
  {
    this.argCnt = argCnt;
  }

  public int getSlots()
  {
    return slots;
  }

  public void setSlots(int slots)
  {
    this.slots = slots;
  }
  
  public void addVariable(Variable variable)
  {
    table.add(variable);
  }

  public Variable getVariable(int index)
  {
    Variable variable;
    variable = (Variable) table.get(index);
    return variable;
  }

  public int size()
  {
    return table.size();
  }
}
