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
public class MethodSignature extends Signature {

    private TypeInfo[] paramTypes;
    private String signatureString;

    public MethodSignature(String name, TypeInfo retType, TypeInfo[] paramTypes) {
        super(name, retType);
        this.paramTypes = paramTypes;
        signatureString = null;
    }

    public TypeInfo[] getParameterTypes() {
        return paramTypes;
    }

    public String getSignature() {
        if ( signatureString == null ) {
            signatureString = TypeHelper.getSignatureString(paramTypes, getType());
        }
        return signatureString;
    }

    /**
     * create a method name consisting of its name and the signature.
     *
     * @param name the name of the Method.
     * @param signature the signature of the params as String.
     * @return the full method name with signature.
     */
    public static String createFullName(String name, String signature) {
        return name + signature;
    }

    public String getFullName() {
        return createFullName(getName(), getSignature());
    }

}
