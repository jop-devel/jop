/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct.type;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface TypeInfo {

    static final byte TYPE_VOID      = 0x00;
    static final byte TYPE_INT       = 0x01;
    static final byte TYPE_FLOAT     = 0x02;
    static final byte TYPE_LONG      = 0x03;
    static final byte TYPE_DOUBLE    = 0x04;
    static final byte TYPE_REFERENCE = 0x05;
    static final byte TYPE_ADDRESS   = 0x06;

    static final byte TYPE_BOOL      = 0x11;
    static final byte TYPE_CHAR      = 0x21;
    static final byte TYPE_BYTE      = 0x31;
    static final byte TYPE_SHORT     = 0x41;

    static final byte TYPE_ARRAYREF  = 0x15;
    static final byte TYPE_OBJECTREF = 0x25;
    static final byte TYPE_NULLREF   = 0x35;
    static final byte TYPE_STRING    = 0x45;

    static final BaseType CONST_VOID    = new BaseType(TYPE_VOID);
    static final BaseType CONST_INT     = new BaseType(TYPE_INT);
    static final BaseType CONST_FLOAT   = new BaseType(TYPE_FLOAT);
    static final BaseType CONST_LONG    = new BaseType(TYPE_LONG);
    static final BaseType CONST_DOUBLE  = new BaseType(TYPE_DOUBLE);
    static final BaseType CONST_BOOL    = new BaseType(TYPE_BOOL);
    static final BaseType CONST_CHAR    = new BaseType(TYPE_CHAR);
    static final BaseType CONST_BYTE    = new BaseType(TYPE_BYTE);
    static final BaseType CONST_SHORT   = new BaseType(TYPE_SHORT);

    static final ObjectRefType CONST_OBJECTREF  = new ObjectRefType(false);
    static final ObjectRefType CONST_NULLREF    = new ObjectRefType(true);
    static final BaseType      CONST_ADDRESSREF = new BaseType(TYPE_ADDRESS);
    static final StringType    CONST_STRING     = new StringType();

    byte getType();

    byte getMachineType();

    /**
     * Get the number of entries of this type in the local variable table.
     * @return the number of entries this type needs in the local variable table.
     */
    byte getLength();

    /**
     * Get a string representation of this type as defined in
     * in the Java Virtual Machine Edition 2nd Edition, chapter 4.3. 
     * @return a unique type string.
     */
    String getDescriptor();

    /**
     * Get a string representation of the type.
     * @return the name of the type as string.
     */
    String getTypeName();
    
    String getMachineTypeName();

}
