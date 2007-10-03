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
 * LineTable.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 13:59:39
 *
 */
public class LineTable
{
  private long start, end;
  
  private Vector table;
  
  public LineTable()
  {
    table = new Vector();
  }
  
  public int numLines()
  {
    return table.size();
  }

  public long getEnd()
  {
    return end;
  }

  public void setEnd(long end)
  {
    this.end = end;
  }

  public long getStart()
  {
    return start;
  }

  public void setStart(long start)
  {
    this.start = start;
  }
  
  public void addLine(Line line)
  {
    table.add(line);
  }
  
  public Line getLine(int index)
  {
    return (Line) table.get(index);
  }
}
