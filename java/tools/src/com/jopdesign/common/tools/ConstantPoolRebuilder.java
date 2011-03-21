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
import com.jopdesign.common.bcel.Annotation;
import com.jopdesign.common.bcel.AnnotationAttribute;
import com.jopdesign.common.bcel.AnnotationElement;
import com.jopdesign.common.bcel.AnnotationElementValue;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.bcel.EnclosingMethod;
import com.jopdesign.common.bcel.ParameterAnnotationAttribute;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.graphutils.DescendingClassTraverser;
import com.jopdesign.common.graphutils.EmptyClassElementVisitor;
import com.jopdesign.common.misc.JavaClassFormatError;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
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
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.Signature;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.StackMapType;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This helper class rebuilds the constantpool of a ClassInfo, and should only be used by ClassInfo itself.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantPoolRebuilder implements ClassVisitor {

    public static Constant copyConstant(Map<Integer,Integer> idMap, Constant c) {
        if (c instanceof ConstantClass) {
            int index = ((ConstantClass)c).getNameIndex();
            return new ConstantClass(idMap.get(index));
        } else if (c instanceof ConstantFieldref) {
            int clsIdx = ((ConstantFieldref)c).getClassIndex();
            int nameIdx = ((ConstantFieldref)c).getNameAndTypeIndex();
            return new ConstantFieldref(idMap.get(clsIdx), idMap.get(nameIdx));
        } else if (c instanceof ConstantMethodref) {
            int clsIdx = ((ConstantMethodref)c).getClassIndex();
            int nameIdx = ((ConstantMethodref)c).getNameAndTypeIndex();
            return new ConstantMethodref(idMap.get(clsIdx), idMap.get(nameIdx));
        } else if (c instanceof ConstantInterfaceMethodref) {
            int clsIdx = ((ConstantInterfaceMethodref)c).getClassIndex();
            int nameIdx = ((ConstantInterfaceMethodref)c).getNameAndTypeIndex();
            return new ConstantInterfaceMethodref(idMap.get(clsIdx), idMap.get(nameIdx));
        } else if (c instanceof ConstantString) {
            int index = ((ConstantString)c).getStringIndex();
            return new ConstantString(idMap.get(index));
        } else if (c instanceof ConstantInteger) {
            return new ConstantInteger((ConstantInteger) c);
        } else if (c instanceof ConstantFloat) {
            return new ConstantFloat((ConstantFloat) c);
        } else if (c instanceof ConstantLong) {
            return new ConstantLong((ConstantLong) c);
        } else if (c instanceof ConstantDouble) {
            return new ConstantDouble((ConstantDouble) c);
        } else if (c instanceof ConstantNameAndType) {
            int nameIdx = ((ConstantNameAndType)c).getNameIndex();
            int sigIdx = ((ConstantNameAndType)c).getSignatureIndex();
            return new ConstantNameAndType(idMap.get(nameIdx), idMap.get(sigIdx));
        } else if (c instanceof ConstantUtf8) {
            return new ConstantUtf8((ConstantUtf8) c);
        }
        throw new JavaClassFormatError("Unknown constant type "+c);
    }

    private class AttributeVisitor extends EmptyClassElementVisitor {
        private final ConstantPool cp;

        private AttributeVisitor(ConstantPool cp) {
            this.cp = cp;
        }

        @Override
        public void visitInnerClasses(ClassInfo classInfo, InnerClasses obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            for (InnerClass ic : obj.getInnerClasses()) {
                ic.setInnerClassIndex(mapIndex(ic.getInnerClassIndex()));
                ic.setOuterClassIndex(mapIndex(ic.getOuterClassIndex()));
                ic.setInnerNameIndex(mapIndex(ic.getInnerNameIndex()));
            }
        }

        @Override
        public void visitSourceFile(ClassInfo classInfo, SourceFile obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            obj.setSourceFileIndex(mapIndex(obj.getSourceFileIndex()));
        }

        @Override
        public void visitEnclosingMethod(ClassInfo classInfo, EnclosingMethod obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));
            obj.setClassIndex(mapIndex(obj.getClassIndex()));
        }

        @Override
        public void visitConstantValue(FieldInfo fieldInfo, ConstantValue obj) {
            throw new AssertionError("Working on FieldGen " +fieldInfo+", ConstantValue should not be present.");
        }

        @Override
        public void visitStackMap(MethodInfo methodInfo, StackMap obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            for (StackMapEntry e : obj.getStackMap()) {
                e.setConstantPool(cp);
                for (StackMapType t : e.getTypesOfLocals()) {
                    t.setConstantPool(cp);
                    if (t.getIndex() != -1 && t.getType() == Constants.ITEM_Object) {
                        t.setIndex(mapIndex(t.getIndex()));
                    }
                }
                for (StackMapType t : e.getTypesOfStackItems()) {
                    t.setConstantPool(cp);
                    if (t.getIndex() != -1 && t.getType() == Constants.ITEM_Object) {
                        t.setIndex(mapIndex(t.getIndex()));
                    }
                }
            }
        }

        @Override
        public void visitSignature(MemberInfo memberInfo, Signature obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            obj.setSignatureIndex(mapIndex(obj.getSignatureIndex()));
        }

        @Override
        public void visitDeprecated(MemberInfo memberInfo, org.apache.bcel.classfile.Deprecated obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));
        }

        @Override
        public void visitSynthetic(MemberInfo memberInfo, Synthetic obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));
        }

        @Override
        public void visitAnnotation(MemberInfo memberInfo, AnnotationAttribute obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            for (Annotation a : obj.getAnnotations()) {
                visitAnnotation(a);
            }
        }

        @Override
        public void visitParameterAnnotation(MemberInfo memberInfo, ParameterAnnotationAttribute obj) {
            obj.setConstantPool(cp);
            obj.setNameIndex(mapIndex(obj.getNameIndex()));

            for (int i = 0; i < obj.getNumParameters(); i++) {
                for (Annotation a : obj.getAnnotations(i)) {
                    visitAnnotation(a);
                }
            }
        }

        @Override
        public void visitUnknown(MemberInfo memberInfo, Unknown obj, boolean isCodeAttribute) {
            throw new JavaClassFormatError("Unsupported attribute in " +memberInfo);
        }

        @Override
        public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
            throw new JavaClassFormatError("Unsupported attribute in " +memberInfo);
        }

        private void visitAnnotation(Annotation a) {
            a.setConstantPool(cp);
            a.setTypeIndex((short)mapIndex(a.getTypeIndex()));

            for (AnnotationElement e : a.getElements()) {
                e.setConstantPool(cp);
                e.setNameIndex((short)mapIndex(e.getNameIndex()));

                AnnotationElementValue v = e.getValue();
                v.setConstantPool(cp);
                if (v.isConstValue()) {
                    v.setConstValueIndex((short)mapIndex(v.getConstValueIndex()));
                } else {
                    throw new JavaClassFormatError("Unsupported annotation value type: "+v);
                }
            }
        }
    }

    private ConstantPoolGen newPool;
    private final Map<Integer, Integer> idMap;

    public ConstantPoolRebuilder() {
        idMap = new HashMap<Integer, Integer>();
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {
        classInfo.rebuildConstantPool(this);
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }

    public ConstantPoolGen createNewConstantPool(ConstantPoolGen oldPool, Set<Integer> usedIndices) {

        // We add all used entries to the new pool in the same order as in the old pool
        // to avoid indices getting larger than before
        Integer[] ids = new ArrayList<Integer>(usedIndices).toArray(new Integer[usedIndices.size()]);
        Arrays.sort(ids);

        // First thing we need is a map, since we need to relink the constantpool entries
        idMap.clear();

        // Note that index 0 is not valid, so skip it
        List<Constant> constants = new ArrayList<Constant>(usedIndices.size()+1);
        constants.add(null);
        int newPos = 1;

        for (int id : ids) {
            Constant c = oldPool.getConstant(id);
            // we cannot use newPool.addConstant here, because this would add all referenced constants too,
            // and we do not want that..
            constants.add(c);
            idMap.put(id, newPos++);
            if (c instanceof ConstantLong || c instanceof ConstantDouble) {
                // reserve an additional slot for those
                constants.add(null);
                newPos++;
            }
        }

        // now we create new constants and map the references
        Constant[] newConstants = new Constant[constants.size()];
        for (int i=1; i < constants.size(); i++) {
            Constant c = constants.get(i);
            if (c == null) continue;
            newConstants[i] = copyConstant(idMap, c);
        }

        newPool = new ConstantPoolGen(newConstants);

        return newPool;
    }

    public void updateClassGen(ClassInfo classInfo, ClassGen classGen) {

        classGen.setConstantPool(newPool);

        // Update name and superclass name index, everything else is stored by value in ClassGen

        classGen.setClassName(classGen.getClassName());

        classGen.setSuperclassName(classGen.getSuperclassName());

        updateAttributes(classInfo, classGen.getAttributes());
    }

    public void updateMethodGen(MethodInfo methodInfo, MethodGen methodGen) {

        methodGen.setConstantPool(newPool);

        if (methodInfo.hasCode()) {
            
            // update all instructions
            InstructionList il = methodInfo.getCode().getInstructionList();

            class InstructionVisitor extends EmptyVisitor {
                @Override
                public void visitCPInstruction(CPInstruction obj) {
                    obj.setIndex(mapIndex(obj.getIndex()));
                }
            }

            InstructionVisitor iv = new InstructionVisitor();

            for (InstructionHandle ih : il.getInstructionHandles()) {
                ih.getInstruction().accept(iv);
            }

            updateAttributes(methodInfo, methodGen.getCodeAttributes());
        }

        updateAttributes(methodInfo, methodGen.getAttributes());
    }

    public void updateFieldGen(FieldInfo fieldInfo, FieldGen fieldGen) {

        fieldGen.setConstantPool(newPool);

        // FieldGen stores everything else by value, not by index

        updateAttributes(fieldInfo, fieldGen.getAttributes());
    }

    private void updateAttributes(MemberInfo memberInfo, Attribute[] attributes) {
        AttributeVisitor visitor = new AttributeVisitor(newPool.getConstantPool());
        new DescendingClassTraverser(visitor).visitAttributes(memberInfo, attributes);
    }

    private int mapIndex(int oldIndex) {
        if (oldIndex == 0) return 0;
        // we could also lookup the old constant in the old pool and try add it to the new one
        // to get the index, if we do not want idMap to be a field
        //Constant c = oldPool.getConstant(oldIndex);
        //return newPool.addConstant(c, oldPool);
        return idMap.get(oldIndex);
    }
}
