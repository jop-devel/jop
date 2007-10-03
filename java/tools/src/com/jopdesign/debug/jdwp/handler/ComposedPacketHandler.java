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

package com.jopdesign.debug.jdwp.handler;

import java.io.IOException;

import com.sun.tools.jdi.PacketWrapper;

/**
 * A ComposedPacketHandler is class which can handle packets
 * based on the knowledge of its internal PacketHandler objects.
 * 
 * When a packet arrives, it is passed to the first handler
 * and then to the second. 
 * 
 * Since this class is also a PacketHandler, it can accept instances
 * of its own class to help build more complex structures. 
 * 
 * @author Paulo Abadie Guedes
 */
public class ComposedPacketHandler implements PacketHandler
{
  private PacketHandler firstHandler;
  private PacketHandler secondHandler;
  
  public ComposedPacketHandler(PacketHandler first, PacketHandler second)
  {
    firstHandler = first;
    secondHandler = second;
  }
  
  public void handlePacket(PacketWrapper packet) throws IOException
  {
    firstHandler.handlePacket(packet);
    secondHandler.handlePacket(packet);
  }
}
