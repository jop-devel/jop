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
import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.bcel.EnclosingMethod;
import com.jopdesign.common.bcel.ParameterAnnotationAttribute;
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
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.Signature;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;

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

    public void visitInnerClass(ClassInfo classInfo, InnerClass obj) {
    }

    public void visitInnerClasses(ClassInfo classInfo, InnerClasses obj) {
    }

    public void visitSourceFile(ClassInfo classInfo, SourceFile obj) {
    }

    public void visitEnclosingMethod(ClassInfo classInfo, EnclosingMethod obj) {
    }

    public void visitConstantValue(FieldInfo fieldInfo, ConstantValue obj) {
    }

    public void visitCodeException(MethodInfo methodInfo, CodeExceptionGen obj) {
    }

    public void visitLineNumber(MethodInfo methodInfo, LineNumberGen obj) {
    }

    public void visitLocalVariable(MethodInfo methodInfo, LocalVariableGen obj) {
    }

    public void visitStackMap(MethodInfo methodInfo, StackMap obj) {
    }

    public void visitStackMapEntry(MethodInfo methodInfo, StackMapEntry obj) {
    }

    public void visitSignature(MemberInfo memberInfo, Signature obj) {
    }

    public void visitDeprecated(MemberInfo memberInfo, org.apache.bcel.classfile.Deprecated obj) {
    }

    public void visitSynthetic(MemberInfo memberInfo, Synthetic obj) {
    }

    public void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj) {
    }

    public void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj) {
    }

    public void visitUnknown(MemberInfo memberInfo, Unknown obj, boolean isCodeAttribute) {
    }

    public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
    }

    public boolean visitClass(ClassInfo classInfo) {
        return true;
    }

    public void finishClass(ClassInfo classInfo) {
    }
}
