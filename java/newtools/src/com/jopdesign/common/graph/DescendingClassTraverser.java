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
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.CustomAttribute;
import com.jopdesign.common.misc.JavaClassFormatError;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Constant;
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
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.log4j.Logger;

/**
 * A class visitor which traverses all elements of a classInfo. Similar to BCELs DescendingVisitor.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DescendingClassTraverser implements ClassVisitor {

    private final ClassElementVisitor visitor;
    
    private BcelVisitor bcelVisitor;
    
    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT+".DescendingClassTraverser");

    private class BcelVisitor implements Visitor {

        private ClassInfo classInfo;
        private FieldInfo fieldInfo;
        private MethodInfo methodInfo;
        private boolean code = false;

        private BcelVisitor() {
        }

        public void setClassInfo(ClassInfo classInfo) {
            this.classInfo = classInfo;
            fieldInfo = null;
            methodInfo = null;
        }

        public void setFieldInfo(FieldInfo fieldInfo) {
            this.fieldInfo = fieldInfo;
            methodInfo = null;
            classInfo = null;
        }

        public void setMethodInfo(MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            fieldInfo = null;
            classInfo = null;
        }

        public boolean isCode() {
            return code;
        }

        public void setCode(boolean code) {
            this.code = code;
        }

        public MemberInfo getMemberInfo() {
            if ( classInfo != null ) return classInfo;
            if ( methodInfo != null ) return methodInfo;
            return fieldInfo;
        }
        
        
        public void visitCode(Code obj) {
            logger.warn("Visiting Code attribute, but MethodInfo should not have one. Skipping.");
        }

        public void visitCodeException(CodeException obj) {
            logger.warn("Visiting CodeException attribute, but MethodInfo should not have one. Skipping.");
        }

        public void visitConstantClass(ConstantClass obj) {
            visitor.visitConstantClass(classInfo, obj);
        }

        public void visitConstantDouble(ConstantDouble obj) {
            visitor.visitConstantDouble(classInfo, obj);
        }

        public void visitConstantFieldref(ConstantFieldref obj) {
            visitor.visitConstantField(classInfo, obj);
        }

        public void visitConstantFloat(ConstantFloat obj) {
            visitor.visitConstantFloat(classInfo, obj);
        }

        public void visitConstantInteger(ConstantInteger obj) {
            visitor.visitConstantInteger(classInfo, obj);
        }

        public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {
            visitor.visitConstantInterfaceMethod(classInfo, obj);
        }

        public void visitConstantLong(ConstantLong obj) {
            visitor.visitConstantLong(classInfo, obj);
        }

        public void visitConstantMethodref(ConstantMethodref obj) {
            visitor.visitConstantMethod(classInfo, obj);
        }

        public void visitConstantNameAndType(ConstantNameAndType obj) {
            visitor.visitConstantNameAndType(classInfo, obj);
        }

        public void visitConstantPool(ConstantPool obj) {
            throw new JavaClassFormatError("Visiting ConstantPool, but this should not happen.");
        }

        public void visitConstantString(ConstantString obj) {
            visitor.visitConstantString(classInfo, obj);
        }

        public void visitConstantUtf8(ConstantUtf8 obj) {
            visitor.visitConstantUtf8(classInfo, obj);
        }

        public void visitConstantValue(ConstantValue obj) {
            visitor.visitConstantValue(fieldInfo, obj);
        }

        public void visitDeprecated(org.apache.bcel.classfile.Deprecated obj) {
            visitor.visitDeprecated(getMemberInfo(), obj);
        }

        public void visitExceptionTable(ExceptionTable obj) {
            logger.warn("Visiting ExceptionTable attribute, but MethodInfo should not have one. Skipping.");
        }

        public void visitField(Field obj) {
            throw new JavaClassFormatError("Visiting Field, but this should not happen.");
        }

        public void visitInnerClass(InnerClass obj) {
            visitor.visitInnerClass(classInfo, obj);
        }

        public void visitInnerClasses(InnerClasses obj) {
            visitor.visitInnerClasses(classInfo, obj);
        }

        public void visitJavaClass(JavaClass obj) {
            throw new JavaClassFormatError("Visiting JavaClass, but this should not happen.");
        }

        public void visitLineNumber(LineNumber obj) {
            throw new JavaClassFormatError("Visiting LineNumber, but this should not happen.");
        }

        public void visitLineNumberTable(LineNumberTable obj) {
            logger.warn("Visiting LineNumberTable attribute, but MethodInfo should not have one. Skipping.");
        }

        public void visitLocalVariable(LocalVariable obj) {
            throw new JavaClassFormatError("Visiting LocalVariable, but this should not happen.");
        }

        public void visitLocalVariableTable(LocalVariableTable obj) {
            logger.warn("Visiting LocalVariableTable attribute, but MethodInfo should not have one. Skipping.");
        }

        public void visitMethod(Method obj) {
            throw new JavaClassFormatError("Visiting Method, but this should not happen.");
        }

        public void visitSignature(Signature obj) {
            visitor.visitSignature(getMemberInfo(), obj);
        }

        public void visitSourceFile(SourceFile obj) {
            visitor.visitSourceFile(classInfo, obj);
        }

        public void visitSynthetic(Synthetic obj) {
            visitor.visitSynthetic(getMemberInfo(), obj);
        }

        public void visitUnknown(Unknown obj) {
            visitor.visitUnknown(getMemberInfo(), obj, code);
        }

        public void visitStackMap(StackMap obj) {
            visitor.visitStackMap(methodInfo, obj);
        }

        public void visitStackMapEntry(StackMapEntry obj) {
            visitor.visitStackMapEntry(methodInfo, obj);
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
            // TODO we might want to make this return-value configurable, default should remain 'true'
            return true;
        }

        bcelVisitor = new BcelVisitor();
        ConstantPoolGen cpg = classInfo.getConstantPoolGen();

        if (visitor.visitConstantPoolGen(classInfo, cpg)) {

            bcelVisitor.setClassInfo(classInfo);

            for (int i = 1; i < cpg.getSize(); i++) {
                Constant c = cpg.getConstant(i);
                c.accept(bcelVisitor);
            }

            visitor.finishConstantPoolGen(classInfo, cpg);
        }

        // methods and fields are final, no need to call accept()
        for (FieldInfo f : classInfo.getFields()) {
            if (!visitor.visitField(f)) {
                continue;
            }

            bcelVisitor.setFieldInfo(f);
            
            visitAttributes(f.getAttributes());

            visitor.finishField(f);
        }
        for (MethodInfo m : classInfo.getMethods()) {
            if (!visitor.visitMethod(m)) {
                continue;
            }

            bcelVisitor.setMethodInfo(m);
            
            for (CodeExceptionGen ex : m.getExceptionHandlers()) {
                visitor.visitCodeException(m, ex);
            }
            
            bcelVisitor.setCode(true);
            
            for (LineNumberGen lng : m.getLineNumbers()) {
                visitor.visitLineNumber(m, lng);
            }
            for (LocalVariableGen lvg : m.getLocalVariables()) {
                visitor.visitLocalVariable(m, lvg);
            }
            visitAttributes(m.getCodeAttributes());
            
            bcelVisitor.setCode(false);
            visitAttributes(m.getAttributes());
            
            visitor.finishMethod(m);
        }

        bcelVisitor.setClassInfo(classInfo);

        visitAttributes(classInfo.getAttributes());
        
        visitor.finishClass(classInfo);

        return true;
    }

    public void finishClass(ClassInfo classInfo) {
    }

    private void visitAttributes(Attribute[] attributes) {
        for (Attribute a : attributes) {
            if ( a instanceof CustomAttribute ) {
                visitor.visitCustomAttribute(bcelVisitor.getMemberInfo(), (CustomAttribute) a, bcelVisitor.isCode());
            } else {
                a.accept(bcelVisitor);
            }
        }        
    }
    
}
