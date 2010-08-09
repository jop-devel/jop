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
import com.jopdesign.common.bcel.CustomAttribute;
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
 * This interface is used to visit a single element of a classInfo (including the class itself).
 * To visit all elements of a class, use a {@link DescendingClassTraverser}.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface ClassElementVisitor extends ClassVisitor {

    boolean visitMethod(MethodInfo methodInfo);

    void    finishMethod(MethodInfo methodInfo);

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


    void visitInnerClass(ClassInfo member, InnerClass obj );

    void visitInnerClasses(ClassInfo member, InnerClasses obj );

    void visitSourceFile(ClassInfo member, SourceFile obj );

    void visitConstantValue(FieldInfo member, ConstantValue obj );

    void visitCodeException(MethodInfo member, CodeExceptionGen obj );

    void visitLineNumber(MethodInfo member, LineNumberGen obj );

    void visitLocalVariable(MethodInfo member, LocalVariableGen obj );

    void visitStackMap(MethodInfo member, StackMap obj );

    void visitStackMapEntry(MethodInfo member, StackMapEntry obj );

    void visitSignature(MemberInfo member, Signature obj );

    void visitDeprecated(MemberInfo member, Deprecated obj );

    void visitSynthetic(MemberInfo member, Synthetic obj );

    void visitUnknown(MemberInfo member, Unknown obj, boolean isCodeAttribute );

    void visitCustomAttribute(MemberInfo member, CustomAttribute obj, boolean isCodeAttribute );
}
