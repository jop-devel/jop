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
 * A packet handler is an object who knows how to
 * handle or consume JDWP packets in some way.
 * 
 * It works closely with a PacketQueue, which holds the
 * packets for consumption.
 * 
 * Classes are free to decide what to do with the packets:
 * modify, check, answer, log, discard, change, describe, 
 * count or whatever is necessary.
 * Handlers may even be combined to build more complex
 * handlers based on existing ones.
 * 
 * Since handling usually requires some form of communication,
 * the handling process may throw IOException.
 * 
 * @author Paulo Abadie Guedes
 */
public interface PacketHandler
{
  /**
   * This method is responsible for packet handling.
   */
  // TODO: change this class to add JDWPException to it.
//  public void handlePacket(PacketWrapper packet) throws IOException, JDWPException;
  public void handlePacket(PacketWrapper packet) throws IOException;
}
