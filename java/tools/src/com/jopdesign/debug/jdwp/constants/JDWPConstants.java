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

import com.jopdesign.debug.jdwp.util.Util;

/**
 * 
 * JDWPConstants.java
 * 
 * Some constants necessary on the project.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 12:14:20
 *
 */
public class JDWPConstants
{
  public static final String JDWP_HANDSHAKE = "JDWP-Handshake";
  
  public static final byte[] JDWP_HANDSHAKE_BYTES = new byte[]
    {'J', 'D', 'W', 'P', '-', 'H', 'a', 'n', 'd', 's', 'h', 'a', 'k', 'e'};
  
  public static final int HANDSHAKE_PACKET_SIZE = JDWP_HANDSHAKE_BYTES.length;
  
  public static final String DEFAULT_SERVER_FILE = "serverJDWPMessages.dat";
  public static final String DEFAULT_CLIENT_FILE = "clientJDWPMessages.dat";
  
  public static final int fieldIDSize = 4;   //fieldID size in bytes
  public static final int methodIDSize = 4;   //methodID size in bytes  
  public static final int objectIDSize = 8;   //objectID size in bytes  
  public static final int referenceTypeIDSize = 8;   //referenceTypeID size in bytes  
  public static final int frameIDSize = 4;   //frameID size in bytes
  
  //------------------------------------------------------------
  // Reference constants for the Java Machine version
  //------------------------------------------------------------
  public static final String REFERENCE_JDWP_VERSION = "Java Debug Wire Protocol (Reference Implementation) version 1.4";
  public static final String REFERENCE_JDI_VERSION = "JVM Debug Interface version 1.3";
  public static final String REFERENCE_JVM_VERSION = "JVM version 1.4.2_14 (Java HotSpot(TM) Client VM, mixed mode)";
  public static final String REFERENCE_JRE_VERSION = "1.4.2_14";
  public static final String REFERENCE_JVM_NAME = "Java HotSpot(TM) Client VM";

  //------------------------------------------------------------
  // Framework constants for the Java Machine version
  //------------------------------------------------------------

  //TODO: check with Martin the correct values for the versions
  public static final String JOP_JDWP_VERSION = "Java Debug Wire Protocol (JOP Implementation) version 1.4";
  public static final String JOP_JDI_VERSION = "JVM Debug Interface version 1.3";
  public static final String JOP_JVM_VERSION = "JVM version 1.0 (JOP - Java Processor in a FPGA)";
  public static final String JOP_JRE_VERSION = "1.0";
  public static final String JOP_JVM_NAME = "JOP: Java Optimized Processor - Client Machine";

  public static final String JOP_VERSION = JOP_JDWP_VERSION + " " + 
    JOP_JDI_VERSION + " " + JOP_JVM_VERSION;
  
  // if version 1.3 is used, the debugger will ask for capabilities 
  // through the Capabilities command (1, 12). 
  // From version 1.4 onward, the debugger will send a 
  // CapabilitiesNew command (1, 17).
  public static final int JDWP_MAJOR = 1;
//  public static final int JDWP_MINOR = 3;
  public static final int JDWP_MINOR = 4;

  public static final String DEFAULT_SYMBOL_FILE = "TestJopDebugKernel.sym";  
  
  public static byte[] getJDWPHandshakeBytes()
  {
    byte[] bytes = Util.copyByteArray(JDWPConstants.JDWP_HANDSHAKE_BYTES);
    return bytes;
  }

  /**
   * The system class loader is identified by a null value.
   * This constant make explicit this semantics.
   * 
   * For more information, please check the JDWP specification.
   * 
   * Information can be found in the ReferenceType Command Set (2),
   * in the ClassLoader Command (2).
   * Check also the ClassLoaderReference Command Set (14), 
   * in the VisibleClasses (1) command.
   * 
   */
  public static final int SYSTEM_CLASS_LOADER = 0;

}
