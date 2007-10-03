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
 * Capabilities.java
 * 
 * An interface to indicate the possible capabilities of
 * this Java Machine.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 12:13:12
 *
 */
public interface Capabilities
{

  //------------------------------------------------------------
  // constants for the Java Machine capabilities
  //------------------------------------------------------------
  public static final boolean canWatchFieldModification = true; // Can the VM watch field modification, and therefore can it send the Modification Watchpoint Event?
  public static final boolean canWatchFieldAccess = true; // Can the VM watch field access, and therefore can it send the Access Watchpoint Event?
  public static final boolean canGetBytecodes = false; // Can the VM get the bytecodes of a given method?
  public static final boolean canGetSyntheticAttribute = false; // Can the VM determine whether a field or method is synthetic? (that is, can the VM determine if the method or the field was invented by the compiler?)
  public static final boolean canGetOwnedMonitorInfo = false; // Can the VM get the owned monitors infornation for a thread?
  public static final boolean canGetCurrentContendedMonitor = false; // Can the VM get the current contended monitor of a thread?
  public static final boolean canGetMonitorInfo = false; // Can the VM get the monitor information for a given object?
  public static final boolean canRedefineClasses = false; // Can the VM redefine classes?
  public static final boolean canAddMethod = false; // Can the VM add methods when redefining classes?
  public static final boolean canUnrestrictedlyRedefineClasses = false; // Can the VM redefine classesin arbitrary ways?
  public static final boolean canPopFrames = false; // Can the VM pop stack frames?
  public static final boolean canUseInstanceFilters = false; // Can the VM filter events by specific object?
  public static final boolean canGetSourceDebugExtension = false; // Can the VM get the source debug extension?
  public static final boolean canRequestVMDeathEvent = false; // Can the VM request VM death events?
  public static final boolean canSetDefaultStratum = false; // Can the VM set a default stratum?

  // 17 flags were reserved for future capability
  public static final boolean reserved16 = false;
  public static final boolean reserved17 = false;
  public static final boolean reserved18 = false;
  public static final boolean reserved19 = false;
  public static final boolean reserved20 = false;
  public static final boolean reserved21 = false;
  public static final boolean reserved22 = false;
  public static final boolean reserved23 = false;
  public static final boolean reserved24 = false;
  public static final boolean reserved25 = false;
  public static final boolean reserved26 = false;
  public static final boolean reserved27 = false;
  public static final boolean reserved28 = false;
  public static final boolean reserved29 = false;
  public static final boolean reserved30 = false;
  public static final boolean reserved31 = false;
  public static final boolean reserved32 = false;
}
