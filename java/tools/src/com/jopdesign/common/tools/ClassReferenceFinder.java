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
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.graphutils.DescendingClassTraverser;
import com.jopdesign.common.graphutils.EmptyClassElementVisitor;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantNameAndTypeInfo;
import com.jopdesign.common.type.Descriptor;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

import java.util.HashSet;
import java.util.Set;

/**
 * A helper class to perform various ClassInfo/MethodInfo/FieldInfo query tasks.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassReferenceFinder {

    /**
     * Get a set of all classes referenced by the given class, including superclasses, interfaces and
     * references from the code, from parameters and from attributes.
     *
     * @param classInfo the classInfo to search
     * @return a set of all fully qualified class names referenced by this class.
     */
    public static Set<String> findReferencedClasses(ClassInfo classInfo) {

        final Set<String> names = new HashSet<String>();

        class ClassNameVisitor extends EmptyClassElementVisitor {

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
            public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
                ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) classInfo.getConstantInfo(constant);
                processDescriptor(nat.getValue().getMemberDescriptor());
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

            @Override
            public void visitInnerClass(ClassInfo classInfo, InnerClass obj) {
                ConstantPoolGen cpg = classInfo.getConstantPoolGen();
                processUtf8(cpg, obj.getInnerClassIndex());
                processUtf8(cpg, obj.getOuterClassIndex());
            }

            private void processUtf8(ConstantPoolGen cpg, int index) {
                if (index == 0) {
                    return;
                }
                ConstantUtf8 constant = (ConstantUtf8) cpg.getConstant(index);
                processClassName(constant.getBytes().replace('/','.'));
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

            private void processClassName(String name) {
                names.add(name);
            }
        }

        ClassNameVisitor visitor = new ClassNameVisitor();
        new DescendingClassTraverser(visitor).visitClass(classInfo);

        return names;
    }


}
