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

import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.TypeInfo;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import java.util.Collection;

/**
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class ClassInfo extends MemberInfo {

    private final ClassGen classGen;
    private final ConstantPoolGen cpg;

    public ClassInfo(AppInfo appInfo, ClassGen classGen) {
        super(appInfo, classGen);
        this.classGen = classGen;
        cpg = classGen.getConstantPool();
    }
    
    @Override
    public ClassInfo getClassInfo() {
        return this;
    }

    public ConstantInfo getConstantInfo(int i) {
        if ( i < 0 || i >= cpg.getSize() ) {
            return null;
        }
        Constant c = cpg.getConstant(i);
        return ConstantInfo.createFromConstant(getAppInfo(), cpg.getConstantPool(), c);
    }

    public int setConstantInfo(int i, ConstantInfo constant) {

        return i;
    }

    public int addConstantInfo(ConstantInfo constant) {
        return setConstantInfo(cpg.getSize(), constant);
    }

    public int lookupConstantInfo(ConstantInfo constant) {
        return -1;
    }

    public int getConstantPoolSize() {
        return cpg.getSize();
    }

    public ConstantInfo removeConstantInfo(int i) {
        if ( i < 0 || i >= cpg.getSize() ) {
            return null;
        }
        
        return null;
    }

    public boolean isInterface() {
        return classGen.isInterface();
    }

    public boolean isAbstract() {
        return classGen.isAbstract();
    }

    public void setAbstract(boolean val) {
        classGen.isAbstract(val);
    }

    public boolean isStrictFP() {
        return classGen.isStrictfp();
    }

    public void setStrictFP(boolean val) {
        classGen.isStrictfp(val);
    }

    public String getSuperClassName() {
        return classGen.getSuperclassName();
    }
    
    public FieldInfo getFieldInfo(String name) {
        return null;
    }

    public MethodInfo getMethodInfo(String signature) {
        return null;
    }

    public MethodInfo[] getMethodByName(String name) {
        return new MethodInfo[]{};
    }

    public FieldInfo createField(String name, TypeInfo type) {
        return null;
    }

    public MethodInfo createMethod(String name, Descriptor descriptor) {
        return null;
    }

    public FieldInfo removeField(String name) {
        return null;
    }

    public MethodInfo removeMethod(String signature) {
        return null;
    }

    public String getClassName() {
        return classGen.getClassName();
    }

    public Collection<FieldInfo> getFields() {
        return null;
    }

    public Collection<MethodInfo> getMethods() {
        return null;
    }

    @Override
    public String getModifierString() {
        String s = super.getModifierString();
        if ( isInterface() ) {
            s += "interface ";
        } else {
            s += "class ";
        }
        return s;
    }

    public ClassRef getClassRef() {
        return new ClassRef(this);
    }
}
