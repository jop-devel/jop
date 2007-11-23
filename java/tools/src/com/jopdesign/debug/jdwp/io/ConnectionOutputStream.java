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

import com.sun.tools.jdi.SocketConnectionWrapper;

/**
 * 
 * ConnectionOutputStream.java
 * 
 * A class to link a ConnectionService to an OutputStream.
 * 
 * @author Paulo Abadie Guedes
 * 13/05/2007 - 23:10:34
 *
 */
public class ConnectionOutputStream extends OutputStream
{
  private SocketConnectionWrapper connection;
  
  public ConnectionOutputStream(SocketConnectionWrapper service)
  {
    this.connection = service;
  }

  public void write(int b) throws IOException
  {
    // TODO: will this conversion be an issue?
    connection.sendByte((byte) b);
  }

}
