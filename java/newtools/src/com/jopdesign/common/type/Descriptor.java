/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.common.type;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.misc.InvalidSignatureException;

import java.util.LinkedList;
import java.util.List;

/**
 * A helper class for parsing, generating and other descriptor related tasks (type descriptors
 * without a name or class text). For signatures containing a member name or class name, see {@link Signature}. 
 *
 * @see Signature
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Descriptor {

    private final String descriptor;
    private final TypeInfo typeInfo;
    private final TypeInfo[] params;

    private Descriptor(String descriptor, TypeInfo typeInfo, TypeInfo[] params) {
        this.typeInfo = typeInfo;
        this.params = params;
        this.descriptor = descriptor;
    }

    public Descriptor(TypeInfo typeInfo) {
        descriptor = typeInfo.toString();
        this.typeInfo = typeInfo;
        this.params = null;
    }

    public Descriptor(TypeInfo typeInfo, TypeInfo[] params) {
        descriptor = compileDescriptor(typeInfo, params);
        this.typeInfo = typeInfo;
        this.params = params;
    }

    public static String compileDescriptor(TypeInfo type, TypeInfo[] params) {
        StringBuffer s = new StringBuffer();
        if ( params != null ) {
            s.append('(');
            for (TypeInfo param : params) {
                s.append(param);
            }
            s.append(')');
        }
        s.append(type.toString());

        return s.toString();
    }

    public static Descriptor parse(String descriptor) throws InvalidSignatureException {

        int pos = 0;

        if ( descriptor == null || descriptor.length() == 0 ) {
            return null;
        }

        TypeInfo type = null;
        TypeInfo[] params = null;

        if ( descriptor.charAt(0) == '(' ) {
            pos++;
            if ( descriptor.length() == 1 ) {
                throw new InvalidSignatureException("Descriptor '"+descriptor+"' is malformed");
            }
            while (descriptor.charAt(pos) != ')') {

            }
        }

        return new Descriptor(type, params);
    }

    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    public boolean isMethod() {
        return descriptor.startsWith("(");
    }

    public TypeInfo[] getParameters() {
        return params;
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public String toString() {
        return descriptor;
    }

}
