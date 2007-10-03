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

package com.jopdesign.debug.jdwp.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * A very simple output stream class to print a description
 * of the JDWP packets provided.
 * The main goal of this class is just for development purposes.
 * 
 * @author Paulo Abadie Guedes
 */

public class JDWPDescriptorOutputStream extends OutputStream
{
  private PrintStream out;
  
  public JDWPDescriptorOutputStream(PrintStream stream)
  {
    out = stream;
  }

  public JDWPDescriptorOutputStream()
  {
    this(System.out);
  }
  
  public void write(final int data) throws IOException
  {
    out.write(data);
  }
}
