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
import com.jopdesign.common.MethodInfo;
import org.apache.bcel.classfile.Code;
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
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Signature;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.classfile.Visitor;

/**
 * A class visitor which traverses all elements of a classInfo. Similar to BCELs DescendingVisitor.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DescendingClassTraverser implements ClassVisitor {

    private final ClassElementVisitor visitor;

    private class BcelVisitor implements Visitor {

        public void visitCode(Code obj) {
        }

        public void visitCodeException(CodeException obj) {
        }

        public void visitConstantClass(ConstantClass obj) {
        }

        public void visitConstantDouble(ConstantDouble obj) {
        }

        public void visitConstantFieldref(ConstantFieldref obj) {
        }

        public void visitConstantFloat(ConstantFloat obj) {
        }

        public void visitConstantInteger(ConstantInteger obj) {
        }

        public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {
        }

        public void visitConstantLong(ConstantLong obj) {
        }

        public void visitConstantMethodref(ConstantMethodref obj) {
        }

        public void visitConstantNameAndType(ConstantNameAndType obj) {
        }

        public void visitConstantPool(ConstantPool obj) {
        }

        public void visitConstantString(ConstantString obj) {
        }

        public void visitConstantUtf8(ConstantUtf8 obj) {
        }

        public void visitConstantValue(ConstantValue obj) {
        }

        public void visitDeprecated(org.apache.bcel.classfile.Deprecated obj) {
        }

        public void visitExceptionTable(ExceptionTable obj) {
        }

        public void visitField(Field obj) {
        }

        public void visitInnerClass(InnerClass obj) {
        }

        public void visitInnerClasses(InnerClasses obj) {
        }

        public void visitJavaClass(JavaClass obj) {
        }

        public void visitLineNumber(LineNumber obj) {
        }

        public void visitLineNumberTable(LineNumberTable obj) {
        }

        public void visitLocalVariable(LocalVariable obj) {
        }

        public void visitLocalVariableTable(LocalVariableTable obj) {
        }

        public void visitMethod(Method obj) {
        }

        public void visitSignature(Signature obj) {
        }

        public void visitSourceFile(SourceFile obj) {
        }

        public void visitSynthetic(Synthetic obj) {
        }

        public void visitUnknown(Unknown obj) {
        }

        public void visitStackMap(StackMap obj) {
        }

        public void visitStackMapEntry(StackMapEntry obj) {
        }
    }

    public DescendingClassTraverser(ClassElementVisitor visitor) {
        this.visitor = visitor;
    }

    public ClassElementVisitor getVisitor() {
        return visitor;
    }

    public boolean visitClass(ClassInfo classInfo) {

        if ( !visitor.visitClass(classInfo) ) {
            // TODO we might want to make this return-value configurable
            return true;
        }

        if (visitor.visitConstantPoolGen(classInfo, classInfo.getConstantPoolGen())) {



            visitor.finishConstantPoolGen(classInfo, classInfo.getConstantPoolGen());
        }

        // methods and fields are final, no need to call accept()
        for (FieldInfo f : classInfo.getFields()) {
            if (!visitor.visitField(f)) {
                continue;
            }


            visitor.finishField(f);
        }
        for (MethodInfo m : classInfo.getMethods()) {
            if (!visitor.visitMethod(m)) {
                continue;
            }


            visitor.finishMethod(m);
        }

        // TODO visit constants using accept(), visit attributes


        visitor.finishClass(classInfo);

        return true;
    }

    public void finishClass(ClassInfo classInfo) {
    }

}
