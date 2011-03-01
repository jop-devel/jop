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

package com.jopdesign.common.graphutils;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MemberInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.bcel.EnclosingMethod;
import com.jopdesign.common.bcel.ParameterAnnotationAttribute;
import com.jopdesign.common.bcel.StackMapTable;
import org.apache.bcel.classfile.Code;
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
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Signature;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
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

    @Override
    public boolean visitMethod(MethodInfo methodInfo) {
        return true;
    }

    @Override
    public void finishMethod(MethodInfo methodInfo) {
    }

    @Override
    public void visitMethodCode(MethodCode methodCode) {
    }

    @Override
    public boolean visitField(FieldInfo fieldInfo) {
        return true;
    }

    @Override
    public void finishField(FieldInfo fieldInfo) {
    }

    @Override
    public boolean visitConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
        return true;
    }

    @Override
    public void finishConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
    }

    @Override
    public void visitConstantClass(ClassInfo classInfo, ConstantClass constant) {
    }

    @Override
    public void visitConstantDouble(ClassInfo classInfo, ConstantDouble constant) {
    }

    @Override
    public void visitConstantField(ClassInfo classInfo, ConstantFieldref constant) {
    }

    @Override
    public void visitConstantFloat(ClassInfo classInfo, ConstantFloat constant) {
    }

    @Override
    public void visitConstantInteger(ClassInfo classInfo, ConstantInteger constant) {
    }

    @Override
    public void visitConstantLong(ClassInfo classInfo, ConstantLong constant) {
    }

    @Override
    public void visitConstantMethod(ClassInfo classInfo, ConstantMethodref constant) {
    }

    @Override
    public void visitConstantInterfaceMethod(ClassInfo classInfo, ConstantInterfaceMethodref constant) {
    }

    @Override
    public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
    }

    @Override
    public void visitConstantString(ClassInfo classInfo, ConstantString constant) {
    }

    @Override
    public void visitConstantUtf8(ClassInfo classInfo, ConstantUtf8 constant) {
    }

    @Override
    public void visitInnerClasses(ClassInfo classInfo, InnerClasses obj) {
    }

    @Override
    public void visitSourceFile(ClassInfo classInfo, SourceFile obj) {
    }

    @Override
    public void visitEnclosingMethod(ClassInfo classInfo, EnclosingMethod obj) {
    }

    @Override
    public void visitConstantValue(FieldInfo fieldInfo, ConstantValue obj) {
    }

    @Override
    public void visitCodeException(MethodInfo methodInfo, CodeExceptionGen obj) {
    }

    @Override
    public void visitLineNumber(MethodInfo methodInfo, LineNumberGen obj) {
    }

    @Override
    public void visitLocalVariable(MethodInfo methodInfo, LocalVariableGen obj) {
    }

    @Override
    public void visitStackMap(MethodInfo methodInfo, StackMap obj) {
    }

    @Override
    public void visitStackMapTable(MethodInfo methodInfo, StackMapTable obj) {
    }

    @Override
    public void visitSignature(MemberInfo memberInfo, Signature obj) {
    }

    @Override
    public void visitDeprecated(MemberInfo memberInfo, org.apache.bcel.classfile.Deprecated obj) {
    }

    @Override
    public void visitSynthetic(MemberInfo memberInfo, Synthetic obj) {
    }

    @Override
    public void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj) {
    }

    @Override
    public void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj) {
    }

    @Override
    public void visitUnknown(MemberInfo memberInfo, Unknown obj, boolean isCodeAttribute) {
    }

    @Override
    public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
    }

    @Override
    public void visitCode(MethodInfo methodInfo, Code code) {
    }

    @Override
    public void visitExceptionTable(MethodInfo methodInfo, ExceptionTable table) {
    }

    @Override
    public void visitLineNumberTable(MethodInfo methodInfo, LineNumberTable table) {
    }

    @Override
    public void visitLocalVariableTable(MethodInfo methodInfo, LocalVariableTable table) {
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }
}
