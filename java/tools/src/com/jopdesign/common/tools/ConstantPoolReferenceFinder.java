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

package com.jopdesign.common.tools;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MemberInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.bcel.Annotation;
import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.common.bcel.AnnotationElement;
import com.jopdesign.common.bcel.AnnotationElementValue;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.bcel.EnclosingMethod;
import com.jopdesign.common.bcel.ParameterAnnotationAttribute;
import com.jopdesign.common.bcel.StackMapTable;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.graphutils.ClassElementVisitor;
import com.jopdesign.common.graphutils.DescendingClassTraverser;
import com.jopdesign.common.graphutils.EmptyClassElementVisitor;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantFieldInfo;
import com.jopdesign.common.type.ConstantMethodInfo;
import com.jopdesign.common.type.ConstantNameAndTypeInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.Constants;
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
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.StackMapType;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper class to perform various ClassInfo/MethodInfo/FieldInfo query tasks.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantPoolReferenceFinder {

    ////////////////////////////////////////////////////////////////
    // Helper classes to find something in a class
    ////////////////////////////////////////////////////////////////

    /**
     * Applying this visitor to a method, field, class or constantpool entries collects all
     * found member references.
     */
    public abstract static class ConstantPoolMemberVisitor extends EmptyClassElementVisitor {

        @Override
        public boolean visitMethod(MethodInfo methodInfo) {
            processDescriptor(methodInfo.getDescriptor());
            return true;
        }

        @Override
        public boolean visitField(FieldInfo fieldInfo) {
            processDescriptor(fieldInfo.getDescriptor());
            return true;
        }

        @Override
        public void visitConstantClass(ClassInfo classInfo, ConstantClass constant) {
            processClassRef(classInfo.getConstantInfo(constant).getClassRef());
        }

        @Override
        public void visitConstantField(ClassInfo classInfo, ConstantFieldref constant) {
            ConstantFieldInfo field = (ConstantFieldInfo) classInfo.getConstantInfo(constant);
            processFieldRef(field.getValue());
        }

        @Override
        public void visitConstantMethod(ClassInfo classInfo, ConstantMethodref constant) {
            ConstantMethodInfo method = (ConstantMethodInfo) classInfo.getConstantInfo(constant);
            processMethodRef(method.getValue());
        }

        @Override
        public void visitConstantInterfaceMethod(ClassInfo classInfo, ConstantInterfaceMethodref constant) {
            ConstantMethodInfo method = (ConstantMethodInfo) classInfo.getConstantInfo(constant);
            processMethodRef(method.getValue());
        }

        @Override
        public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
            ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) classInfo.getConstantInfo(constant);
            processDescriptor(nat.getValue().getDescriptor());
        }

        @Override
        public void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj) {
            visitCustomAttribute(memberInfo, obj, true);
        }

        @Override
        public void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj) {
            visitCustomAttribute(memberInfo, obj, true);
        }

        @Override
        public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
            String[] classes = obj.getReferencedClassNames();
            if ( classes != null ) {
                for (String cName : classes) {
                    processClassName(cName);
                }
            }
        }

        private void processClassRef(ClassRef ref) {
            processType( ref.getType() );
        }

        private void processDescriptor(Descriptor d) {
            Type ret = d.getType();
            processType(ret);

            if (d.isMethod()) {
                for (Type t : d.getArgumentTypes()) {
                    processType(t);
                }
            }
        }

        private void processType(Type  type) {

            if ( type instanceof ArrayType) {
                processType( ((ArrayType)type).getBasicType() );
            } else if ( type instanceof ObjectType) {
                processClassName( ((ObjectType)type).getClassName() );
            }
        }

        public abstract void processClassName(String name);

        public abstract void processFieldRef(FieldRef fieldRef);

        public abstract void processMethodRef(MethodRef methodRef);
    }

    /**
     * Collect only class names
     */
    public static class ClassNameVisitor extends ConstantPoolMemberVisitor {
        private Set<String> names;

        public ClassNameVisitor(Set<String> names) {
            this.names = names;
        }

        @Override
        public void processClassName(String name) {
            // also called for the classname part of field- and method-refs
            names.add(name);
        }

        @Override
        public void processFieldRef(FieldRef fieldRef) {
        }

        @Override
        public void processMethodRef(MethodRef methodRef) {
        }
    }

    /**
     * Collect all member references, using a syntax which is always unambiguous
     */
    public static class ClassMemberVisitor extends ConstantPoolMemberVisitor {
        private Set<String> members;

        public ClassMemberVisitor(Set<String> members) {
            this.members = members;
        }

        @Override
        public void processClassName(String name) {
            members.add(name);
        }

        @Override
        public void processFieldRef(FieldRef fieldRef) {
            String className = fieldRef.getClassName();
            members.add(className + MemberID.ALT_MEMBER_SEPARATOR + fieldRef.getName());
        }

        @Override
        public void processMethodRef(MethodRef methodRef) {
            String className = methodRef.getClassName();
            members.add(className + MemberID.ALT_MEMBER_SEPARATOR + methodRef.getMethodSignature());
        }
    }

    ////////////////////////////////////////////////////////////////
    // Find/replace references, member names, Pool entries
    ////////////////////////////////////////////////////////////////

    public static Set<Integer> findPoolReferences(ClassInfo classInfo, boolean checkMembers) {

        JavaClass javaClass = classInfo.compile();

        Set<Integer> ids = findPoolReferences(classInfo, javaClass);

        if (checkMembers) {
            for (Field field : javaClass.getFields()) {
                FieldInfo fieldInfo = classInfo.getFieldInfo(field.getName());
                ids.addAll( findPoolReferences(fieldInfo, field) );
            }
            for (Method method : javaClass.getMethods()) {
                MethodInfo methodInfo = classInfo.getMethodInfo(method.getName()+method.getSignature());
                ids.addAll( findPoolReferences(methodInfo, method) );
            }
        }

        return ids;
    }

    public static Set<Integer> findPoolReferences(MethodInfo methodInfo) {
        return findPoolReferences(methodInfo, methodInfo.compile());
    }

    public static Set<Integer> findPoolReferences(FieldInfo fieldInfo) {
        return findPoolReferences(fieldInfo, fieldInfo.getField());
    }

    /**
     * Get a set of all referenced classes and class members for a method.
     *
     * @param methodInfo the method to search.
     * @return a set of class names and class member IDs found in the method.
     */
    public static Set<String> findReferencedMembers(MethodInfo methodInfo) {
        Set<String> members = new HashSet<String>();
        ClassMemberVisitor visitor = new ClassMemberVisitor(members);

        Set<Integer> ids = findPoolReferences(methodInfo);

        // fill the members list with info from the method descriptor
        visitor.visitMethod(methodInfo);
        // fill the members list with all found constantpool references
        visitPoolReferences(methodInfo.getClassInfo(), visitor, ids);
        
        return members;
    }

    /**
     * Get a set of all referenced classes and class members for a field.
     *
     * @param fieldInfo the field to search.
     * @return a set of class names and class member IDs found in the field.
     */
    public static Set<String> findReferencedMembers(FieldInfo fieldInfo) {
        Set<String> members = new HashSet<String>();
        ClassMemberVisitor visitor = new ClassMemberVisitor(members);

        Set<Integer> ids = findPoolReferences(fieldInfo);

        // fill the members list with info from the type descriptor
        visitor.visitField(fieldInfo);
        // fill the members list with all found constantpool references
        visitPoolReferences(fieldInfo.getClassInfo(), visitor, ids);

        return members;
    }

    /**
     * Find all referenced members in a class.
     * @param classInfo the class to search.
     * @param checkMembers if false, do not check fields and methods. Else check everything.
     * @return a set of class names and class member signatures found in the class.
     */
    public static Set<String> findReferencedMembers(ClassInfo classInfo, boolean checkMembers) {

        // Else we need to go into details..
        Set<String> members = new HashSet<String>();
        ClassMemberVisitor visitor = new ClassMemberVisitor(members);

        JavaClass javaClass = classInfo.compile();
        Set<Integer> ids = findPoolReferences(classInfo, javaClass);
        List<InvokeSite> invokes = new ArrayList<InvokeSite>();

        if (checkMembers) {
            for (Field field : javaClass.getFields()) {
                FieldInfo fieldInfo = classInfo.getFieldInfo(field.getName());
                // add members found in the field
                visitor.visitField(fieldInfo);
                // there are no invokesites in a field, only add found ids
                ids.addAll( findPoolReferences(fieldInfo, field) );
            }
            for (Method method : javaClass.getMethods()) {
                MethodInfo methodInfo = classInfo.getMethodInfo(method.getName()+method.getSignature());
                // add members found in the method
                visitor.visitMethod(methodInfo);
                // add all ids for checking, add all invoke sites
                ids.addAll( findPoolReferences(methodInfo, method) );
            }
        }

        // fill the members list with all found constantpool references
        visitor.visitClass(classInfo);
        visitPoolReferences(classInfo, visitor, ids);

        return members;
    }

    /**
     * @param members a set of member names returned by a findReferencedMembers() method.
     * @return a set of all classnames found in the given set.
     */
    public static Set<String> getClassNames(Set<String> members) {
        Set<String> classNames = new HashSet<String>();
        for (String name : members) {
            classNames.add( MemberID.getClassName(name, false) );
        }
        return classNames;
    }

    /**
     * Get a set of all classes referenced by the given class, including superclasses, interfaces and
     * references from the code, from parameters and from attributes.
     *
     * @param classInfo the classInfo to search
     * @return a set of all fully qualified class names referenced by this class.
     */
    public static Set<String> findReferencedClasses(ClassInfo classInfo) {

        final Set<String> names = new HashSet<String>();

        ClassNameVisitor visitor = new ClassNameVisitor(names);
        new DescendingClassTraverser(visitor).visitClass(classInfo);

        return names;
    }

    ////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////

    /**
     * This is a helper visitor to find all constant pool references in classes / methods / fields.
     *
     * TODO this is nasty and 'fine-tuned' stuff, reduce number of indirections and make this code more robust
     *
     * This is supposed to work the following way:
     * - findPoolReferences() applies this visitor to all indices found directly, then visits the attributes
     * - IdFinderVisitor.visitConstant*(int) adds the index to the set, recurses down referenced constants using:
     * - IdFinderVisitor.visitConstant*(Constant) visits all indices in the constant using above
     * - A DescendingClassTraverser is used to
     *   - Visit various attributes
     *      - visitor-methods recurse down using visitConstant(int)
     *   - Visit referenced constants with unknown type, using the visitConstant(int) method
     */
    private static class IdFinderVisitor implements ClassElementVisitor {
        private final ClassInfo classInfo;
        private final Set<Integer> ids;
        private final ConstantPool cp;
        private final ConstantPoolGen cpg;
        private final DescendingClassTraverser classTraverser;

        /**
         * Create a new visitor.
         *
         * @param classInfo the classinfo which will be visited.
         */
        public IdFinderVisitor(ClassInfo classInfo) {
            this.classInfo = classInfo;
            this.ids = new HashSet<Integer>();
            this.cpg = classInfo.getConstantPoolGen();
            this.cp = cpg.getConstantPool();
            // A helper traverser to visit parts of classes/methods/..
            this.classTraverser = new DescendingClassTraverser(this);
        }

        public Set<Integer> getIds() {
            return ids;
        }

        @Override
        public boolean visitMethod(MethodInfo methodInfo) {
            // ignored, handled in findPoolReferences(Method)
            return true;
        }

        @Override
        public void finishMethod(MethodInfo methodInfo) {
            // nothing to do
        }

        @Override
        public void visitMethodCode(MethodCode methodCode) {
            throw new AssertionError("Visited methodCode, but we visit only Method attributes");
        }

        @Override
        public boolean visitField(FieldInfo fieldInfo) {
            // ignored, handled in findPoolReferences(Field)
            return true;
        }

        @Override
        public void finishField(FieldInfo fieldInfo) {
            // nothing to do
        }

        @Override
        public boolean visitConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
            // ignored
            return true;
        }

        @Override
        public void finishConstantPoolGen(ClassInfo classInfo, ConstantPoolGen cpg) {
            // nothing to do
        }

        @Override
        public void visitConstantClass(ClassInfo classInfo, ConstantClass constant) {
            visitConstantUtf8(constant.getNameIndex());
        }

        @Override
        public void visitConstantDouble(ClassInfo classInfo, ConstantDouble constant) {
            // no indices here
        }

        @Override
        public void visitConstantField(ClassInfo classInfo, ConstantFieldref constant) {
            visitConstantClass(constant.getClassIndex());
            visitConstantNameAndType(constant.getNameAndTypeIndex());
        }

        @Override
        public void visitConstantFloat(ClassInfo classInfo, ConstantFloat constant) {
            // no indices here
        }

        @Override
        public void visitConstantInteger(ClassInfo classInfo, ConstantInteger constant) {
            // no indices here
        }

        @Override
        public void visitConstantLong(ClassInfo classInfo, ConstantLong constant) {
            // no indices here
        }

        @Override
        public void visitConstantMethod(ClassInfo classInfo, ConstantMethodref constant) {
            visitConstantClass(constant.getClassIndex());
            visitConstantNameAndType(constant.getNameAndTypeIndex());
        }

        @Override
        public void visitConstantInterfaceMethod(ClassInfo classInfo, ConstantInterfaceMethodref constant) {
            visitConstantClass(constant.getClassIndex());
            visitConstantNameAndType(constant.getNameAndTypeIndex());
        }

        @Override
        public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
            visitConstantUtf8(constant.getNameIndex());
            visitConstantUtf8(constant.getSignatureIndex());
        }

        @Override
        public void visitConstantString(ClassInfo classInfo, ConstantString constant) {
            visitConstantUtf8(constant.getStringIndex());
        }

        @Override
        public void visitConstantUtf8(ClassInfo classInfo, ConstantUtf8 constant) {
            // no indices here
        }

        @Override
        public void visitInnerClasses(ClassInfo classInfo, InnerClasses obj) {
            visitConstantUtf8(obj.getNameIndex());
            for (InnerClass ic : obj.getInnerClasses()) {
                visitConstantClass(ic.getInnerClassIndex());
                if (ic.getOuterClassIndex() != 0) {
                    visitConstantClass(ic.getOuterClassIndex());
                }
                if (ic.getInnerNameIndex() != 0) {
                    visitConstantUtf8(ic.getInnerNameIndex());
                }
            }
        }

        @Override
        public void visitSourceFile(ClassInfo classInfo, SourceFile obj) {
            visitConstantUtf8(obj.getNameIndex());
            visitConstantUtf8(obj.getSourceFileIndex());
        }

        @Override
        public void visitEnclosingMethod(ClassInfo classInfo, EnclosingMethod obj) {
            visitConstantUtf8(obj.getNameIndex());
            visitConstantClass(obj.getClassIndex());
            int index = obj.getMethodIndex();
            if (index != 0) {
                visitConstantNameAndType(index);
            }
        }

        @Override
        public void visitConstantValue(FieldInfo fieldInfo, ConstantValue obj) {
            visitConstantUtf8(obj.getNameIndex());
            int index = obj.getConstantValueIndex();
            Constant c = cp.getConstant(index);
            if (c instanceof ConstantString) {
                visitConstantString(index);
            }
        }

        @Override
        public void visitCodeException(MethodInfo methodInfo, CodeExceptionGen obj) {
            ObjectType type = obj.getCatchType();
            if (type != null) {
                // we do not have an index here, we need to find it
                visitConstantClass(cpg.addClass(type));
            }
        }

        @Override
        public void visitLineNumber(MethodInfo methodInfo, LineNumberGen obj) {
            // is redundant, but we do it anyway: add the name of the linenr table attribute
            visitConstantUtf8(cpg.addUtf8("LineNumberTable"));
            // No other constantpool references here
        }

        @Override
        public void visitLocalVariable(MethodInfo methodInfo, LocalVariableGen obj) {
            // is redundant, but we do it anyway: add the name of the linenumber table attribute
            visitConstantUtf8(cpg.addUtf8("LocalVariableTable"));
            // we do not know the indices, so we need to find them
            visitConstantUtf8(cpg.addUtf8(obj.getName()));
            visitConstantUtf8(cpg.addUtf8(obj.getType().getSignature()));
        }

        @Override
        public void visitStackMap(MethodInfo methodInfo, StackMap obj) {
            visitConstantUtf8(obj.getNameIndex());

            for (StackMapEntry e : obj.getStackMap()) {
                for (StackMapType t : e.getTypesOfLocals()) {
                    visitStackType(t);
                }
                for (StackMapType t : e.getTypesOfStackItems()) {
                    visitStackType(t);
                }
            }
        }

        @Override
        public void visitStackMapTable(MethodInfo methodInfo, StackMapTable obj) {
            visitCustomAttribute(methodInfo, obj, true);
        }

        @Override
        public void visitSignature(MemberInfo memberInfo, org.apache.bcel.classfile.Signature obj) {
            visitConstantUtf8(obj.getNameIndex());
            visitConstantUtf8(obj.getSignatureIndex());
        }

        @Override
        public void visitDeprecated(MemberInfo memberInfo, org.apache.bcel.classfile.Deprecated obj) {
            visitConstantUtf8(obj.getNameIndex());
        }

        @Override
        public void visitSynthetic(MemberInfo memberInfo, Synthetic obj) {
            visitConstantUtf8(obj.getNameIndex());
        }

        @Override
        public void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj) {
            visitConstantUtf8(obj.getNameIndex());
            for (Annotation a : obj.getAnnotations()) {
                visitAnnotation(memberInfo.getClassInfo(), a);
            }
        }

        @Override
        public void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj) {
            visitConstantUtf8(obj.getNameIndex());
            for (int i = 0; i < obj.getNumParameters(); i++) {
                for (Annotation a : obj.getAnnotations(i)) {
                    visitAnnotation(memberInfo.getClassInfo(), a);
                }
            }
        }

        @Override
        public void visitUnknown(MemberInfo memberInfo, Unknown obj, boolean isCodeAttribute) {
            if ("LocalVariableTypeTable".equals(obj.getName())) {
                // TODO quick workaround, just ignore this attribute for now
                return;
            }
            throw new JavaClassFormatError("Unknown Attribute "+obj.getName()+" in "+memberInfo+" found, not supported!");
        }

        @Override
        public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
            Collection<Integer> ids = obj.getConstantPoolIDs();
            if (ids == null) {
                throw new JavaClassFormatError("CustomAttribute "+obj.getName()+" in "+memberInfo+" not supported!");
            }
            for (Integer i : ids) {
                visitConstant(i);
            }
        }

        @Override
        public void visitCode(MethodInfo methodInfo, Code code) {
            visitConstantUtf8(code.getNameIndex());

            InstructionList il = new InstructionList(code.getCode());
            visitInstructionList(methodInfo.getCode(), il);
            il.dispose();

            for (CodeException ce : code.getExceptionTable()) {
                if (ce.getCatchType() != 0) {
                    visitConstantClass(ce.getCatchType());
                }
            }

            new DescendingClassTraverser(this).visitAttributes(methodInfo, code.getAttributes());
        }

        @Override
        public void visitExceptionTable(MethodInfo methodInfo, ExceptionTable table) {
            visitConstantUtf8(table.getNameIndex());
            for (int index : table.getExceptionIndexTable()) {
                visitConstantClass(index);
            }
        }

        @Override
        public void visitLineNumberTable(MethodInfo methodInfo, LineNumberTable table) {
            visitConstantUtf8(table.getNameIndex());

        }

        @Override
        public void visitLocalVariableTable(MethodInfo methodInfo, LocalVariableTable table) {
            visitConstantUtf8(table.getNameIndex());

        }

        @Override
        public boolean visitClass(ClassInfo classInfo) {
            // handled in findPoolReferences(JavaClass)
            return true;
        }

        @Override
        public void finishClass(ClassInfo classInfo) {
            // nothing to do
        }

        //////// Instruction visitor /////////

        private void visitInstructionList(MethodCode methodCode, InstructionList il) {
            for (InstructionHandle ih : il.getInstructionHandles()) {
                Instruction i = ih.getInstruction();
                if (i instanceof CPInstruction) {
                    visitConstant(((CPInstruction)i).getIndex());
                }
            }
        }

        ///////// End Instruction visitor /////////

        private void visitStackType(StackMapType type) {
            if (type.getType() == Constants.ITEM_Object && type.getIndex() != -1) {
                visitConstantClass(type.getIndex());
            }
        }

        private void visitAnnotation(ClassInfo classInfo, Annotation annotation) {
            visitConstantUtf8(annotation.getTypeIndex());

            for (AnnotationElement e : annotation.getElements()) {
                visitConstantUtf8(e.getNameIndex());

                AnnotationElementValue v = e.getValue();
                if (v.isConstValue()) {
                    visitConstant(v.getConstValueIndex());
                } else {
                    throw new JavaClassFormatError("Unsupported annotation type "+v.getTag());
                }
            }
        }

        private void visitConstant(int index) {
            if (index == 0) return;
            ids.add(index);
            classTraverser.visitConstant(classInfo, index);
        }

        private void visitConstantClass(int index) {
            if (index == 0) return;
            ids.add(index);
            ConstantClass c = (ConstantClass) cp.getConstant(index);
            visitConstantClass(classInfo, c);
        }

        private void visitConstantNameAndType(int index) {
            if (index == 0) return;
            ids.add(index);
            ConstantNameAndType c = (ConstantNameAndType) cp.getConstant(index);
            visitConstantNameAndType(classInfo, c);
        }

        private void visitConstantString(int index) {
            if (index == 0) return;
            ids.add(index);
            ConstantString c = (ConstantString) cp.getConstant(index);
            visitConstantString(classInfo, c);
        }

        private void visitConstantUtf8(int index) {
            if (index == 0) return;
            ids.add(index);
        }
    }

    /**
     * Helper method which applies a visitor to all constants given by a set of ids.
     * @param classInfo the class containing the constant pool
     * @param visitor the visitor to apply
     * @param ids a set of ids of pool entries to visit
     */
    private static void visitPoolReferences(ClassInfo classInfo, ClassElementVisitor visitor, Set<Integer> ids) {
        DescendingClassTraverser traverser = new DescendingClassTraverser(visitor);
        ConstantPoolGen cpg = classInfo.getConstantPoolGen();
        // iterate over the given pool entries, call the visitor
        for (Integer id : ids) {
            traverser.visitConstant(classInfo, cpg.getConstant(id));
        }
    }

    private static Set<Integer> findPoolReferences(ClassInfo classInfo, JavaClass javaClass) {

        // we do not need to handle invoke sites here specially, since we do not visit code here
        IdFinderVisitor visitor = new IdFinderVisitor(classInfo);

        // now, find *every* used constantpool index in the class, except for methods and fields
        visitor.visitConstantClass(javaClass.getClassNameIndex());
        visitor.visitConstantClass(javaClass.getSuperclassNameIndex());
        for (int index : javaClass.getInterfaceIndices()) {
            visitor.visitConstantClass(index);
        }

        DescendingClassTraverser traverser = new DescendingClassTraverser(visitor);
        traverser.visitAttributes(classInfo, javaClass.getAttributes());

        return visitor.getIds();
    }

    private static Set<Integer> findPoolReferences(MethodInfo methodInfo, Method method) {
        ClassInfo classInfo = methodInfo.getClassInfo();

        IdFinderVisitor visitor = new IdFinderVisitor(classInfo);

        visitor.visitConstantUtf8(method.getNameIndex());
        visitor.visitConstantUtf8(method.getSignatureIndex());

        DescendingClassTraverser traverser = new DescendingClassTraverser(visitor);
        // Code is an attribute, since we visit Method, not MethodGen
        traverser.visitAttributes(methodInfo, method.getAttributes());

        return visitor.getIds();
    }

    private static Set<Integer> findPoolReferences(FieldInfo fieldInfo, Field field) {
        ClassInfo classInfo = fieldInfo.getClassInfo();

        // we do not need to handle invoke sites here specially, since we do not visit code here
        IdFinderVisitor visitor = new IdFinderVisitor(classInfo);

        visitor.visitConstantUtf8(field.getNameIndex());
        visitor.visitConstantUtf8(field.getSignatureIndex());

        DescendingClassTraverser traverser = new DescendingClassTraverser(visitor);
        traverser.visitAttributes(fieldInfo, field.getAttributes());

        return visitor.getIds();
    }

}


