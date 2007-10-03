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

package com.jopdesign.debug.jdwp.sniffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A very simple class to model a Socket. This class uses the
 * "Adapter" design pattern. Quoting the patterns book:
 * "Adapter lets classes work together that couldn't otherwise
 * because of incompatible interfaces".
 * 
 * For instance, this class can be used as an interface 
 * to help another class to read or write JDWP packets
 * from any input/output streams without much effort.
 * 
 * @author Paulo Abadie Guedes
 */
public class MockSocket extends Socket
{
  private InputStream input;
  private OutputStream output;
  
  public MockSocket(InputStream inputStream, OutputStream outputStream)
  {
    super();
    input = inputStream;
    output = outputStream;
  }
  
  public InputStream getInputStream() throws IOException
  {
    return input;
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    return output;
  }
}
