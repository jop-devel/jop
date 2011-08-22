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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.Type;

import java.util.Arrays;

/**
 * A helper class for parsing, generating and other descriptor related tasks (type descriptors
 * without a name or class text). For signatures containing a member name or class name, see {@link MemberID}.
 *
 * @see MemberID
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Descriptor {

    private final Type type;
    private final Type[] arguments;
    private String descriptor;

    /**
     * Constructor used by parse() method.
     * Using Descriptor.parse() instead of new Descriptor(String) to be similar to MemberID,
     * and to indicate that there is more workload to it than a simple assignments.
     *
     * @param descriptor the parsed descriptor
     * @param type the return type
     * @param arguments the params or null if not a method descriptor.
     */
    private Descriptor(String descriptor, Type type, Type[] arguments) {
        this.type = type;
        this.arguments = arguments;
        this.descriptor = descriptor;
    }

    public Descriptor(Type type) {
        this.type = type;
        arguments = null;
    }

    public Descriptor(Type type, Type[] arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    public static String compileDescriptor(Type type, Type[] params) {
        if ( params != null ) {
            return Type.getMethodSignature(type, params);
        } else {
            return type.getSignature();
        }
    }

    public static Descriptor parse(String descriptor) {
        if ( descriptor.charAt(0) == '(' ) {
            return new Descriptor(descriptor, Type.getReturnType(descriptor), Type.getArgumentTypes(descriptor) );
        } else {
            return new Descriptor(descriptor, Type.getType(descriptor), null);
        }
    }

    public boolean isArray() {
        return type.getType() == Constants.T_ARRAY;
    }

    public boolean isMethod() {
        return arguments != null;
    }

    public Type[] getArgumentTypes() {
        return arguments;
    }

    /**
     * @return the return type
     */
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        if (arguments != null) {
            for (Type t : arguments) {
                hash = 31 * hash + t.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Descriptor)) {
            return false;
        }
        Descriptor d = (Descriptor) o;
        return d.getType().equals(type) && this.equalArguments(d);
    }

    public boolean equalArguments(Descriptor d) {
        return Arrays.equals(arguments, d.getArgumentTypes());
    }

    public String toString() {
        if ( descriptor == null ) {
            descriptor = compileDescriptor(type, arguments);
        }
        return descriptor;
    }

}
