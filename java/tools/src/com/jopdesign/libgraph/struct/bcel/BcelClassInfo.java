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
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelClassInfo extends ClassInfo {

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

    protected Set loadInterfaces() {
        String[] names = javaClass.getInterfaceNames();
        Set interfaces = new HashSet(names.length);

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            ClassInfo cls = getAppStruct().getClassInfo(name);
            if ( cls == null ) {
                logger.error("Could not find interface class {"+name+"} for class {" + getClassName() + "}" );
            } else {
                interfaces.add(cls);
            }
        }
        return interfaces;
    }

    public boolean isPublic() {
        return javaClass.isPublic();
    }

    public boolean isPrivate() {
        return javaClass.isPrivate();
    }

    public boolean isProtected() {
        return javaClass.isProtected();
    }

    public boolean isFinal() {
        return javaClass.isFinal();
    }

    public boolean isStatic() {
        return javaClass.isStatic();
    }

    public boolean isSynchronized() {
        return javaClass.isSynchronized();
    }

    public void setFinal(boolean val) {
        //
    }

    public void setAccessType(int type) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    public void writeClassFile(String filename) throws IOException {
        updateClass();
        javaClass.dump(filename);
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
