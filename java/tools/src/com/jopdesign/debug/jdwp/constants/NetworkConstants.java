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

package com.jopdesign.debug.jdwp.constants;

/**
 * NetworkConstants.java
 * 
 * Constants related to network aspects of the application
 * and other command-line parameters.
 * 
 * @author Paulo Guedes
 * 29/05/2007 - 17:15:24
 * 
 */
public interface NetworkConstants
{

  public static final String SERVER_FILE_PREFIX = "serverFile:";
  public static final String CLIENT_FILE_PREFIX = "clientFile:";
  
  public static final String INPUT_PORT_PREFIX = "inputPort:";
  public static final String OUTPUT_PORT_PREFIX = "outputPort:";
  public static final String TARGET_HOST_PREFIX = "host:";
  
  public static final String DEFAULT_HOST = "localhost";
  
  public static final int DEFAULT_OUTPUT_PORT_NUMBER = 8001;
  public static final int DEFAULT_INPUT_PORT_NUMBER = 8000;
  
  // default ports to run the several distinct configurations
  // possible for JDWP server, sniffer and JOP server.
  public static final int DEFAULT_DEBUGGER_PORT_NUMBER = 8000;
  public static final int DEFAULT_SNIFFER_PORT_NUMBER = 8001;
  public static final int DEFAULT_JDWP_SERVER_PORT_NUMBER = 8002;
  public static final int DEFAULT_JOP_SNIFFER_PORT_NUMBER = 8003;
  public static final int DEFAULT_JOP_SERVER_PORT_NUMBER = 8004;
  
  public static String[] NETWORK_SETTINGS = new String[]
  {
//    SERVER_FILE_PREFIX, CLIENT_FILE_PREFIX, 
    INPUT_PORT_PREFIX, 
    OUTPUT_PORT_PREFIX, 
    TARGET_HOST_PREFIX
  };
                                                  
  public static String[] DEFAULT_NETWORK_SETTINGS = new String[]
 {
// SERVER_FILE_PREFIX, CLIENT_FILE_PREFIX, 
   INPUT_PORT_PREFIX + DEFAULT_INPUT_PORT_NUMBER, 
   OUTPUT_PORT_PREFIX + DEFAULT_OUTPUT_PORT_NUMBER, 
   TARGET_HOST_PREFIX + DEFAULT_HOST
 };
  
  public static String[] DEFAULT_SETTINGS_JOP_SERVER = new String[]
  {
    INPUT_PORT_PREFIX + DEFAULT_JOP_SERVER_PORT_NUMBER, 
    OUTPUT_PORT_PREFIX + DEFAULT_JOP_SERVER_PORT_NUMBER, 
    TARGET_HOST_PREFIX + DEFAULT_HOST
  };

  public static String[] DEFAULT_SETTINGS_JOP_DEBUG_MANAGER = new String[]
  {
    INPUT_PORT_PREFIX + DEFAULT_JDWP_SERVER_PORT_NUMBER, 
    OUTPUT_PORT_PREFIX + DEFAULT_JOP_SERVER_PORT_NUMBER, 
    TARGET_HOST_PREFIX + DEFAULT_HOST
  };
}
