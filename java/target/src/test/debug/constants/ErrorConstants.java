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

package debug.constants;

/**
 * 
 * ErrorConstants.java
 * 
 * Interface to hold constants related to error codes.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 11:59:49
 */
public interface ErrorConstants
{
  public static final int ERROR_NONE = 0;
  public static final int ERROR_INVALID_THREAD = 10;
  public static final int ERROR_INVALID_THREAD_GROUP = 11;
  public static final int ERROR_INVALID_PRIORITY = 12;
  public static final int ERROR_THREAD_NOT_SUSPENDED = 13;
  public static final int ERROR_THREAD_SUSPENDED = 14;
  public static final int ERROR_INVALID_OBJECT = 20;
  public static final int ERROR_INVALID_CLASS = 21;
  public static final int ERROR_CLASS_NOT_PREPARED = 22;
  public static final int ERROR_INVALID_METHODID = 23;
  public static final int ERROR_INVALID_LOCATION = 24;
  public static final int ERROR_INVALID_FIELDID = 25;
  public static final int ERROR_INVALID_FRAMEID = 30;
  public static final int ERROR_NO_MORE_FRAMES = 31;
  public static final int ERROR_OPAQUE_FRAME = 32;
  public static final int ERROR_NOT_CURRENT_FRAME = 33;
  public static final int ERROR_TYPE_MISMATCH = 34;
  public static final int ERROR_INVALID_SLOT = 35;
  public static final int ERROR_DUPLICATE = 40;
  public static final int ERROR_NOT_FOUND = 41;
  public static final int ERROR_INVALID_MONITOR = 50;
  public static final int ERROR_NOT_MONITOR_OWNER = 51;
  public static final int ERROR_INTERRUPT = 52;
  public static final int ERROR_INVALID_CLASS_FORMAT = 60;
  public static final int ERROR_CIRCULAR_CLASS_DEFINITION = 61;
  public static final int ERROR_FAILS_VERIFICATION = 62;
  public static final int ERROR_ADD_METHOD_NOT_IMPLEMENTED = 63;
  public static final int ERROR_SCHEMA_CHANGE_NOT_IMPLEMENTED = 64;
  public static final int ERROR_INVALID_TYPESTATE = 65;
  public static final int ERROR_HIERARCHY_CHANGE_NOT_IMPLEMENTED = 66;
  public static final int ERROR_DELETE_METHOD_NOT_IMPLEMENTED = 67;
  public static final int ERROR_UNSUPPORTED_VERSION = 68;
  public static final int ERROR_NAMES_DONT_MATCH = 69;
  public static final int ERROR_CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 70;
  public static final int ERROR_METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 71;
  public static final int ERROR_NOT_IMPLEMENTED = 99;
  public static final int ERROR_NULL_POINTER = 100;
  public static final int ERROR_ABSENT_INFORMATION = 101;
  public static final int ERROR_INVALID_EVENT_TYPE = 102;
  public static final int ERROR_ILLEGAL_ARGUMENT = 103;
  public static final int ERROR_OUT_OF_MEMORY = 110;
  public static final int ERROR_ACCESS_DENIED = 111;
  public static final int ERROR_VM_DEAD = 112;
  public static final int ERROR_INTERNAL = 113;
  public static final int ERROR_UNATTACHED_THREAD = 115;
  public static final int ERROR_INVALID_TAG = 500;
  public static final int ERROR_ALREADY_INVOKING = 502;
  public static final int ERROR_INVALID_INDEX = 503;
  public static final int ERROR_INVALID_LENGTH = 504;
  public static final int ERROR_INVALID_STRING = 506;
  public static final int ERROR_INVALID_CLASS_LOADER = 507;
  public static final int ERROR_INVALID_ARRAY = 508;
  public static final int ERROR_TRANSPORT_LOAD = 509;
  public static final int ERROR_TRANSPORT_INIT = 510;
  public static final int ERROR_NATIVE_METHOD = 511;
  public static final int ERROR_INVALID_COUNT = 512;
}
