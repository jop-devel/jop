/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct.bcel;

import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantPoolInfo;
import com.jopdesign.libgraph.struct.TypeException;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelClassInfo extends ClassInfo {

    /**
     * A simple class reference finder using BCEL visitors.
     *
     * This implementation uses some code from com.jopdesign.build.TransitiveHull
     * to resolve references in the ClassFinder.
     */
    private class ClassRefFinder extends EmptyVisitor {

        private Set visited;
        private ConstantPool cp;

        public ClassRefFinder(ConstantPool cp) {
            visited = new HashSet();
            this.cp = cp;
        }

        public Set getVisited() {
            return visited;
        }

        public void visitConstantMethodref(ConstantMethodref obj) {
            visitRef(obj, true);
        }

        public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {
            visitRef(obj, true);
        }

        public void visitConstantFieldref(ConstantFieldref obj) {
            visitRef(obj, false);
        }

        public void visitConstantClass(ConstantClass obj) {

            // TODO check: bcel-5.2 goes nuts here, loads *everything*

            String className = (String) obj.getConstantValue(cp);
            if (logger.isDebugEnabled()) {
                logger.debug("Found class reference {" + className + "}.");
            }
            addClass(className);
        }

        private void visitRef(ConstantCP ccp, boolean method) {

            // add referenced class
            String className = ccp.getClass(cp);

            ConstantNameAndType cnat = (ConstantNameAndType)cp.
                getConstant(ccp.getNameAndTypeIndex(), Constants.CONSTANT_NameAndType);

            // add types referenced by signature
            String signature = cnat.getSignature(cp);

            if (logger.isDebugEnabled()) {
                logger.debug("Found field/method reference {" + cnat.getName(cp) + signature +
                        "} of class {" + className + "}");
            }

            addClass(className);

            if(method) {
                addType( Type.getReturnType(signature) );

                Type[] types = Type.getArgumentTypes(signature);

                for(int i = 0; i < types.length; i++) {
                    addType( types[i] );
                }
            } else {
                addType( Type.getType(signature) );
            }

        }

        private void addType(Type type) {
            if ( type instanceof ObjectType ) {
                addClass(((ObjectType)type).getClassName());
            } else if ( type instanceof ArrayType ) {
                addType(((ArrayType)type).getElementType());
            }
        }

        private void addClass(String className) {
            className = className.replace('/','.');
            visited.add(className);
        }
    }

    private class MethodVisitor extends EmptyVisitor {

        public MethodVisitor() {
        }

        public void visitMethod(Method obj) {
            addMethodInfo(new BcelMethodInfo(BcelClassInfo.this, obj));
        }
    }

    private class FieldVisitor extends EmptyVisitor {

        public FieldVisitor() {
        }


        public void visitField(Field obj) {
            try {
                addFieldInfo(new BcelFieldInfo(BcelClassInfo.this, obj));
            } catch (TypeException e) {
                BcelClassInfo.logger.error("Could not load field {" + obj.getName() +
                        "} for class {"+BcelClassInfo.this.getClassName()+"}.", e);
            }
        }
    }

    private JavaClass javaClass;
    private ClassGen classGen;
    private BcelConstantPoolInfo constantPool;
    private ConstantPoolGen cpg;
    private boolean modified;

    private static Logger logger = Logger.getLogger(BcelClassInfo.class);

    public BcelClassInfo(AppStruct appStruct, JavaClass javaClass) {
        super(appStruct);
        this.javaClass = javaClass;
        classGen = new ClassGen(javaClass);
        cpg = classGen.getConstantPool();
        modified = false;
        constantPool = new BcelConstantPoolInfo(this);
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    protected Set loadInterfaces() throws TypeException {
        String[] names = javaClass.getInterfaceNames();
        Set interfaces = new HashSet(names.length);

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            ClassInfo cls = getAppStruct().getClassInfo(name, true);
            if ( cls == null ) {
                // TODO set interface as ConstantClass, set class to 'incomplete' state (errors are thrown by getClassInfo)

                if (logger.isInfoEnabled()) {
                    logger.info("Could not find interface class {" + name + "} for class {" + getClassName() + "}");
                }
            } else {
                interfaces.add(cls);
            }
        }
        return interfaces;
    }

    public boolean isPublic() {
        return classGen.isPublic();
    }

    public boolean isPrivate() {
        return classGen.isPrivate();
    }

    public boolean isProtected() {
        return classGen.isProtected();
    }

    public boolean isFinal() {
        return classGen.isFinal();
    }

    public boolean isStatic() {
        return classGen.isStatic();
    }

    public boolean isSynchronized() {
        return classGen.isSynchronized();
    }

    public void setFinal(boolean val) {
        int af = classGen.getModifiers();
        if ( val ) {
            classGen.setModifiers(af | Constants.ACC_FINAL);
        } else {
            classGen.setModifiers(af & (~Constants.ACC_FINAL));
        }
        modified = true;
    }

    public void setStatic(boolean val) {
        int af = classGen.getModifiers();
        if ( val ) {
            classGen.setModifiers(af | Constants.ACC_STATIC);
        } else {
            classGen.setModifiers(af & (~Constants.ACC_STATIC));
        }
    }

    public void setAccessType(int type) {
        int af = classGen.getAccessFlags() & ~(Constants.ACC_PRIVATE|Constants.ACC_PROTECTED|Constants.ACC_PUBLIC);
        switch (type) {
            case ACC_PRIVATE: af |= Constants.ACC_PRIVATE; break;
            case ACC_PROTECTED: af |= Constants.ACC_PROTECTED; break;
            case ACC_PUBLIC: af |= Constants.ACC_PUBLIC; break;
        }
        classGen.setAccessFlags(af);
        modified = true;
    }

    public String getClassName() {
        return javaClass.getClassName();
    }

    public String getSuperClassName() {
        return "java.lang.Object".equals(getClassName()) ? null : javaClass.getSuperclassName();
    }

    public boolean isInterface() {
        return javaClass.isInterface();
    }

    public ConstantPoolInfo getConstantPoolInfo() {
        return constantPool;
    }

    public Set getReferencedClassNames() {
        updateClass();

        ClassRefFinder finder = new ClassRefFinder(javaClass.getConstantPool());
        new DescendingVisitor(javaClass, finder).visit();

        return finder.getVisited();
    }

    /**
     * replace an existing method. used when code is modified.
     *
     * @param method the bcel-method to use.
     */
    public void setMethod(Method method) {
        Method[] methods = javaClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method oldMethod = methods[i];
            if ( oldMethod.getName().equals(method.getName()) &&
                 oldMethod.getSignature().equals(method.getSignature()))
            {
                classGen.setMethodAt(method, i);
                modified = true;
                break;
            }
        }

    }

    public void writeClassFile(OutputStream out) throws IOException {
        updateClass();
        javaClass.dump(new DataOutputStream(out));
    }

    public ConstantPoolGen getConstantPoolGen() {
        return cpg;
    }

    public void setModified() {
        modified = true;
    }

    protected void loadMethodInfos() {
        new DescendingVisitor(javaClass, new MethodVisitor()).visit();
    }

    protected void loadFieldInfos() {
        new DescendingVisitor(javaClass, new FieldVisitor()).visit();
    }

    private void updateClass() {
        if ( modified ) {
            modified = false;
            javaClass = classGen.getJavaClass();
        }
    }
}
