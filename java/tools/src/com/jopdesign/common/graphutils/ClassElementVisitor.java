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
import org.apache.bcel.classfile.Deprecated;
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
 * This interface is used to visit a single element of a classInfo (including the class itself).
 * To visit all elements of a class, use a {@link DescendingClassTraverser}.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface ClassElementVisitor extends ClassVisitor {

    boolean visitMethod(MethodInfo methodInfo);

    void    finishMethod(MethodInfo methodInfo);

    void    visitMethodCode(MethodCode methodCode);

    boolean visitField(FieldInfo fieldInfo);

    void    finishField(FieldInfo fieldInfo);

    boolean visitConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg);

    void    finishConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg);


    
    void visitConstantClass(ClassInfo classInfo, ConstantClass constant);

    void visitConstantDouble(ClassInfo classInfo, ConstantDouble constant);

    void visitConstantField(ClassInfo classInfo, ConstantFieldref constant);

    void visitConstantFloat(ClassInfo classInfo, ConstantFloat constant);

    void visitConstantInteger(ClassInfo classInfo, ConstantInteger constant);

    void visitConstantLong(ClassInfo classInfo, ConstantLong constant);

    void visitConstantMethod(ClassInfo classInfo, ConstantMethodref constant);

    void visitConstantInterfaceMethod(ClassInfo classInfo, ConstantInterfaceMethodref constant);

    void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant);

    void visitConstantString(ClassInfo classInfo, ConstantString constant);

    void visitConstantUtf8(ClassInfo classInfo, ConstantUtf8 constant);


    void visitInnerClasses(ClassInfo classInfo, InnerClasses obj );

    void visitSourceFile(ClassInfo classInfo, SourceFile obj );

    void visitEnclosingMethod(ClassInfo classInfo, EnclosingMethod obj );

    void visitConstantValue(FieldInfo fieldInfo, ConstantValue obj );

    void visitCodeException(MethodInfo methodInfo, CodeExceptionGen obj );

    void visitLineNumber(MethodInfo methodInfo, LineNumberGen obj );

    void visitLocalVariable(MethodInfo methodInfo, LocalVariableGen obj );

    void visitStackMap(MethodInfo methodInfo, StackMap obj );

    void visitStackMapTable(MethodInfo methodInfo, StackMapTable obj );

    void visitSignature(MemberInfo memberInfo, Signature obj );

    void visitDeprecated(MemberInfo memberInfo, Deprecated obj );

    void visitSynthetic(MemberInfo memberInfo, Synthetic obj );

    void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj );

    void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj );

    void visitUnknown(MemberInfo memberInfo, Unknown obj, boolean isCodeAttribute );

    void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute );


    void visitCode(MethodInfo methodInfo, Code code);

    void visitExceptionTable(MethodInfo methodInfo, ExceptionTable table);

    void visitLineNumberTable(MethodInfo methodInfo, LineNumberTable table);

    void visitLocalVariableTable(MethodInfo methodInfo, LocalVariableTable table);
    
}
