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

package com.jopdesign.common.graph;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MemberInfo;
import com.jopdesign.common.MethodInfo;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class EmptyClassElementVisitor implements ClassElementVisitor {

    public boolean visitMethod(MethodInfo methodInfo) {
        return true;
    }

    public void finishMethod(MethodInfo methodInfo) {
    }

    public boolean visitField(FieldInfo fieldInfo) {
        return true;
    }

    public void finishField(FieldInfo fieldInfo) {
    }

    public boolean visitConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
        return true;
    }

    public void finishConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
    }

    public void visitConstantClass(ClassInfo classInfo, ConstantClass constant) {
    }

    public void visitConstantDouble(ClassInfo classInfo, ConstantDouble constant) {
    }

    public void visitConstantField(ClassInfo classInfo, ConstantFieldref constant) {
    }

    public void visitConstantFloat(ClassInfo classInfo, ConstantFloat constant) {
    }

    public void visitConstantInteger(ClassInfo classInfo, ConstantInteger constant) {
    }

    public void visitConstantLong(ClassInfo classInfo, ConstantLong constant) {
    }

    public void visitConstantMethod(ClassInfo classInfo, ConstantMethodref constant) {
    }

    public void visitConstantInterfaceMethod(ClassInfo classInfo, ConstantInterfaceMethodref constant) {
    }

    public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
    }

    public void visitConstantString(ClassInfo classInfo, ConstantString constant) {
    }

    public void visitConstantUtf8(ClassInfo classInfo, ConstantUtf8 constant) {
    }

    public void visitInnerClass(ClassInfo member, InnerClass obj) {
    }

    public void visitInnerClasses(ClassInfo member, InnerClasses obj) {
    }

    public void visitSourceFile(ClassInfo member, SourceFile obj) {
    }

    public void visitConstantValue(FieldInfo member, ConstantValue obj) {
    }

    public void visitCodeException(MethodInfo member, CodeException obj) {
    }

    public void visitExceptionTable(MethodInfo member, ExceptionTable obj) {
    }

    public void visitLineNumber(MethodInfo member, LineNumber obj) {
    }

    public void visitLineNumberTable(MethodInfo member, LineNumberTable obj) {
    }

    public void visitLocalVariable(MethodInfo member, LocalVariable obj) {
    }

    public void visitLocalVariableTable(MethodInfo member, LocalVariableTable obj) {
    }

    public void visitStackMap(MethodInfo member, StackMap obj) {
    }

    public void visitStackMapEntry(MethodInfo member, StackMapEntry obj) {
    }

    public void visitDeprecated(MemberInfo member, org.apache.bcel.classfile.Deprecated obj) {
    }

    public void visitSynthetic(MemberInfo member, Synthetic obj) {
    }

    public void visitUnknown(MemberInfo member, Unknown obj) {
    }

    public boolean visitClass(ClassInfo classInfo) {
        return true;
    }

    public void finishClass(ClassInfo classInfo) {
    }
}
