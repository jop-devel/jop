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

package com.jopdesign.common;

import com.jopdesign.common.code.CodeRepresentation;
import com.jopdesign.common.type.Descriptor;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class MethodInfo extends ClassMemberInfo {

    private final MethodGen methodGen;

    private CodeRepresentation codeRep;

    public MethodInfo(ClassInfo classInfo, MethodGen methodGen) {
        super(classInfo, methodGen);
        this.methodGen = methodGen;
    }

    public boolean isAbstract() {
        return methodGen.isAbstract();
    }

    public void setAbstract(boolean val) {
        methodGen.isAbstract(val);
    }

    public boolean isSynchronized() {
        return methodGen.isSynchronized();
    }

    public void setSynchronized(boolean val) {
        methodGen.isSynchronized(val);
    }

    public boolean isNative() {
        return methodGen.isNative();
    }

    public void setNative(boolean val) {
        methodGen.isNative(val);
    }

    public boolean isStrictFP() {
        return methodGen.isStrictfp();
    }

    public void setStrictFP(boolean val) {
        methodGen.isStrictfp(val);
    }

    public MethodGen getMethodGen() {
        compileCodeRep();
        return new MethodGen(methodGen.getMethod(), methodGen.getClassName(), methodGen.getConstantPool());
    }

    public void setMethodGen(MethodGen method) {
        codeRep = null;

        // TODO copy all code relevant infos from method, excluding name, params and access flags
        methodGen.setInstructionList(method.getInstructionList());
    }

    public InstructionList getInstructionList() {
        compileCodeRep();
        return methodGen.getInstructionList();
    }

    public void setInstructionList(InstructionList il) {
        methodGen.setInstructionList(il);
        codeRep = null;
    }

    public void removeNOPs() {
        methodGen.removeNOPs();
    }

    @SuppressWarnings({"unchecked"})
    public <T extends CodeRepresentation> T getCode(T codeRep) {
        if ( this.codeRep == null ) {
            this.codeRep = codeRep;
            codeRep.load(this);
            return codeRep;
        }

        if ( codeRep.getClass().isInstance(this.codeRep) &&
             codeRep.isSameType(this.codeRep) )
        {
            // this.codeRep is same type and already contains the current code.
            // cast is checked above
            return (T) this.codeRep;
        }

        // this.codeRep is set but different
        compileCodeRep();
        this.codeRep = codeRep;
        codeRep.load(this);
        return codeRep;
    }

    public boolean hasCodeRep() {
        return codeRep != null;
    }

    public void compileCodeRep() {
        if ( codeRep != null ) {
            codeRep.compile(this);
        }
    }

    public boolean rollbackCodeRep() {
        if ( codeRep == null ) {
            return false;
        }
        codeRep = null;
        return true;
    }

    public Descriptor getDescriptor() {
        return getSignature().getMemberDescriptor();
    }
}
