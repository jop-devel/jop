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

import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.TypeException;
import org.apache.bcel.generic.Type;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TypeHelper {

    private static class RetType {
        private int pos;
        private TypeInfo type;
    }

    public static TypeInfo parseType(AppStruct appStruct, String type) throws TypeException {
        return parseType(appStruct, type, 0).type;
    }

    public static TypeInfo parseType(AppStruct appStruct, Type type) throws TypeException {
        // NOTICE direct translate from Bcel-Type to TypeInfo?
        return parseType(appStruct,type.getSignature());
    }

    public static String getSignatureString(TypeInfo[] signature, TypeInfo retType) {
        StringBuffer out = new StringBuffer("(");

        for (int i = 0; i < signature.length; i++) {
            out.append(signature[i].getDescriptor());
        }
        out.append(")");

        out.append(retType.getDescriptor());

        return out.toString();
    }

    public static MethodSignature parseSignature(AppStruct appStruct, String name, String signature)
            throws TypeException
    {
        if ( signature.charAt(0) != '(' ) {
            throw new TypeException("Invalid method signature {"+signature+"} of method {"+name+"}.");
        }

        List types = new LinkedList();
        int pos = 1;
        RetType ret;

        while ( signature.charAt(pos) != ')' ) {
            ret = parseType(appStruct, signature, pos);
            pos = ret.pos;
            types.add(ret.type);
            if ( pos >= signature.length() ) {
                throw new TypeException("Missing ')' in method signature {"+signature+"} of method {"+name+"}.");
            }
        }

        pos++;
        ret = parseType(appStruct, signature, pos);
        TypeInfo retType = ret.type;

        TypeInfo[] paramTypes = (TypeInfo[]) types.toArray(new TypeInfo[types.size()]);

        return new MethodSignature(name, retType, paramTypes);
    }

    /**
     * Get the first classname within this type string.
     * @param type the type string to be searched.
     * @return the first classname or null if not found.
     */
    public static String getClassName(String type) {
        int start = type.indexOf("L");
        if ( start == -1 ) {
            return null;
        }
        int end = type.indexOf(";", ++start);
        if ( end == -1 ) {
            return null;
        }

        return type.substring(start, end).replace("/",".");
    }

    private static RetType parseType(AppStruct appStruct, String type, int pos) throws TypeException {

        char c = type.charAt(pos++);

        RetType retType = new RetType();
        retType.pos = pos;

        switch (c) {
            case 'V':  retType.type = new BaseType(TypeInfo.TYPE_VOID); break;
            case 'B':  retType.type = new BaseType(TypeInfo.TYPE_BYTE); break;
            case 'C':  retType.type = new BaseType(TypeInfo.TYPE_CHAR); break;
            case 'D':  retType.type = new BaseType(TypeInfo.TYPE_DOUBLE); break;
            case 'F':  retType.type = new BaseType(TypeInfo.TYPE_FLOAT); break;
            case 'I':  retType.type = new BaseType(TypeInfo.TYPE_INT); break;
            case 'J':  retType.type = new BaseType(TypeInfo.TYPE_LONG); break;
            case 'S':  retType.type = new BaseType(TypeInfo.TYPE_SHORT); break;
            case 'Z':  retType.type = new BaseType(TypeInfo.TYPE_BOOL); break;
            case 'L':
                int endPos = type.indexOf(';',pos);
                if ( endPos == -1 ) break;

                String className = type.substring(pos,endPos).replace("/",".");
                ClassInfo classInfo = null;
                if ( appStruct != null ) {
                    classInfo = appStruct.getClassInfo(className);

                    // try to load missing class if not found, else create type with name only.
                    if ( classInfo == null ) {
                        classInfo = appStruct.tryLoadMissingClass(className);
                    }
                }

                if ( classInfo == null ) {
                    retType.type = new ObjectRefType(className);
                } else {
                    retType.type = new ObjectRefType(classInfo);
                }
                retType.pos = endPos + 1;

                break;
            case '[':

                int dimension = 1;
                while ( type.charAt(pos) == '[' ) {
                    dimension++;
                    pos++;
                }

                RetType aRetType = parseType(appStruct, type, pos);

                retType.pos = aRetType.pos;
                retType.type = new ArrayRefType(dimension, aRetType.type);

                break;
            default:
                throw new TypeException("Invalid type descriptor at pos {"+(pos-1)+"} in {"+type+"}.");
        }


        return retType;
    }
}
