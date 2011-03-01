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
import com.jopdesign.common.logger.LogConfig;
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

    ///////////////////////////////////////////////////////////////////
    // Private BCEL visitor delegator
    ///////////////////////////////////////////////////////////////////

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

        public void setMemberInfo(MemberInfo member) {
            if (member instanceof MethodInfo) {
                setMethodInfo((MethodInfo) member);
            } else if (member instanceof FieldInfo) {
                setFieldInfo((FieldInfo) member);
            } else if (member instanceof ClassInfo) {
                setClassInfo((ClassInfo) member);
            } else {
                throw new AssertionError("A member which is neither class, field nor method?? " + member);
            }
        }

        public boolean isCode() {
            return code;
        }

        public void setCode(boolean code) {
            this.code = code;
        }

        public MemberInfo getMemberInfo() {
            if (classInfo != null) return classInfo;
            if (methodInfo != null) return methodInfo;
            return fieldInfo;
        }

        public ClassInfo getClassInfo() {
            return classInfo;
        }

        public MethodInfo getMethodInfo() {
            return methodInfo;
        }

        public void visitCode(Code obj) {
            visitor.visitCode(methodInfo, obj);
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
            visitor.visitExceptionTable(methodInfo, obj);
        }

        public void visitField(Field obj) {
            throw new JavaClassFormatError("Visiting Field, but this should not happen.");
        }

        public void visitInnerClass(InnerClass obj) {
            throw new JavaClassFormatError("Visiting InnerClass, but we do not call this..");
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
            visitor.visitLineNumberTable(methodInfo, obj);
        }

        public void visitLocalVariable(LocalVariable obj) {
            throw new JavaClassFormatError("Visiting LocalVariable, but this should not happen.");
        }

        public void visitLocalVariableTable(LocalVariableTable obj) {
            visitor.visitLocalVariableTable(methodInfo, obj);
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
            throw new JavaClassFormatError("Visiting StackMapEntry, but we do not call this..");
        }
    }

    ///////////////////////////////////////////////////////////////////
    // Constructor, ClassVisitor implementation
    ///////////////////////////////////////////////////////////////////

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".DescendingClassTraverser");

    private final ClassElementVisitor visitor;
    private final BcelVisitor bcelVisitor;

    private boolean returnOnSkipClass = true;

    public DescendingClassTraverser(ClassElementVisitor visitor) {
        this.visitor = visitor;
        bcelVisitor = new BcelVisitor();
    }

    public ClassElementVisitor getVisitor() {
        return visitor;
    }

    /**
     * Do we want to terminate iteration over classes when the ClassElementVisitor wants to terminate iteration
     * for a class? Default is not to terminate.
     * @param terminate if true, return the same return value in {@link #visitClass(ClassInfo)} as the visitor,
     *                  else only skip the current class and continue with the next class.
     */
    public void setTerminateAfterClassSkipped(boolean terminate) {
        returnOnSkipClass = !terminate;
    }

    /**
     * @return True if we terminate the traversion of all classes when the ClassElementVisitor terminates
     *              iteration for a class. Default is false.
     */
    public boolean doTerminateAfterClassSkipped() {
        return !returnOnSkipClass;
    }

    public boolean visitClass(ClassInfo classInfo) {

        if (!visitor.visitClass(classInfo)) {
            return returnOnSkipClass;
        }

        visitConstantPool(classInfo);

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

            visitMethodCode(m);

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


    ///////////////////////////////////////////////////////////////////
    // Other methods to visit only parts of a class
    ///////////////////////////////////////////////////////////////////

    public void visitConstantPool(ClassInfo classInfo) {
        ConstantPoolGen cpg = classInfo.getConstantPoolGen();

        if (visitor.visitConstantPoolGen(classInfo, cpg)) {

            bcelVisitor.setClassInfo(classInfo);

            for (int i = 1; i < cpg.getSize(); i++) {
                Constant c = cpg.getConstant(i);
                // Some entries might be null (continuation of previous entry)
                if (c == null) continue;
                c.accept(bcelVisitor);
            }

            visitor.finishConstantPoolGen(classInfo, cpg);
        }
    }

    public void visitConstant(ClassInfo classInfo, int index) {
        ConstantPoolGen cpg = classInfo.getConstantPoolGen();
        visitConstant(classInfo, cpg.getConstant(index));
    }

    public void visitConstant(ClassInfo classInfo, Constant constant) {
        if (constant == null) return;
        bcelVisitor.setClassInfo(classInfo);
        constant.accept(bcelVisitor);
    }

    public void visitMethodCode(MethodInfo methodInfo) {
        if (methodInfo.hasCode()) {
            bcelVisitor.setCode(true);

            MethodCode code = methodInfo.getCode();

            visitor.visitMethodCode(code);

            for (CodeExceptionGen ex : code.getExceptionHandlers()) {
                visitor.visitCodeException(methodInfo, ex);
            }

            for (LineNumberGen lng : code.getLineNumbers()) {
                visitor.visitLineNumber(methodInfo, lng);
            }
            for (LocalVariableGen lvg : code.getLocalVariables()) {
                visitor.visitLocalVariable(methodInfo, lvg);
            }
            visitAttributes(code.getAttributes());

            bcelVisitor.setCode(false);
        }
    }

    public void visitAttributes(MemberInfo member, Attribute[] attributes) {
        bcelVisitor.setMemberInfo(member);
        visitAttributes(attributes);
    }


    ///////////////////////////////////////////////////////////////////
    // Private stuff
    ///////////////////////////////////////////////////////////////////

    private void visitAttributes(Attribute[] attributes) {
        for (Attribute a : attributes) {
            if (a instanceof EnclosingMethod) {
                visitor.visitEnclosingMethod(bcelVisitor.getClassInfo(), (EnclosingMethod) a);
            } else if (a instanceof AnnotationAttribute) {
                visitor.visitAnnotation(bcelVisitor.getMemberInfo(), (AnnotationAttribute) a);
            } else if (a instanceof ParameterAnnotationAttribute) {
                visitor.visitParameterAnnotation(bcelVisitor.getMemberInfo(), (ParameterAnnotationAttribute) a);
            } else if (a instanceof StackMapTable) {
                visitor.visitStackMapTable(bcelVisitor.getMethodInfo(), (StackMapTable) a);
            } else if (a instanceof CustomAttribute) {
                visitor.visitCustomAttribute(bcelVisitor.getMemberInfo(), (CustomAttribute) a, bcelVisitor.isCode());
            } else {
                a.accept(bcelVisitor);
            }
        }
    }

}
