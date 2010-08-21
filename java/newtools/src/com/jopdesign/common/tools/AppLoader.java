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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MemberInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.bcel.CustomAttribute;
import com.jopdesign.common.graph.DescendingClassTraverser;
import com.jopdesign.common.graph.EmptyClassElementVisitor;
import com.jopdesign.common.logger.LogConfig;
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
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppLoader {

    private final List<ClassInfo> queue;
    private final Set<String> visited;
    private final List<ClassInfo> newClasses;
    private boolean followNatives;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_LOADING + ".AppLoader");

    public AppLoader() {
        queue = new LinkedList<ClassInfo>();
        visited = new HashSet<String>();
        newClasses = new LinkedList<ClassInfo>();
        followNatives = true;
    }

    public AppLoader(boolean followNatives) {
        queue = new LinkedList<ClassInfo>();
        visited = new HashSet<String>();
        newClasses = new LinkedList<ClassInfo>();
        this.followNatives = followNatives;
    }

    public boolean doProcessNatives() {
        return followNatives;
    }

    public void setFollowNatives(boolean followNatives) {
        this.followNatives = followNatives;
    }

    public void reset() {
        queue.clear();
        visited.clear();
        newClasses.clear();
    }

    /**
     * Load the complete transitive hull of all classes currently in AppInfo.
     */
    public void loadAll() {
        loadAll(false);
    }
    
    public void loadAll(boolean startFromRootsOnly) {
        AppInfo appInfo = AppInfo.getSingleton();
        if ( startFromRootsOnly ) {
            enqueue( appInfo.getRootClasses() );
        } else {
            enqueue( appInfo.getClassInfos() );
        }

        processQueue();
    }

    public void loadTransitiveHull(Collection<ClassInfo> roots) {
        enqueue(roots);
        processQueue();
    }

    public void loadTransitiveHull(ClassInfo root) {
        enqueue(root);
        processQueue();
    }

    public void loadSuperClasses(ClassInfo classInfo) {

    }

    public Collection<ClassInfo> getLoadedClasses() {
        return newClasses;
    }

    private void processQueue() {

        if (logger.isInfoEnabled()) {
            logger.info("Starting transitive hull loader");
        }

        while (!queue.isEmpty()) {
            ClassInfo next = queue.remove(0);

            if (logger.isDebugEnabled()) {
                logger.debug("Processing class: "+next.getClassName());
            }

            int found = processClass(next);

            if (logger.isDebugEnabled()) {
                logger.debug("Found "+found+" new classes in " +next.getClassName());
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("AppLoader loaded " + newClasses.size() + " new classes");
        }
    }

    private int processClass(ClassInfo classInfo) {

        class ProcessVisitor extends EmptyClassElementVisitor {
            private int cnt = 0;
            public int getCount() { return cnt; }

            @Override
            public boolean visitMethod(MethodInfo methodInfo) {
                cnt += processDescriptor(methodInfo.getDescriptor());
                return true;
            }

            @Override
            public boolean visitField(FieldInfo fieldInfo) {
                cnt += processDescriptor(fieldInfo.getDescriptor());
                return true;
            }

            @Override
            public void visitConstantClass(ClassInfo classInfo, ConstantClass constant) {
                cnt += processClassRef(classInfo.getConstantInfo(constant).getClassRef());
            }

            @Override
            public void visitConstantNameAndType(ClassInfo classInfo, ConstantNameAndType constant) {
                ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) classInfo.getConstantInfo(constant);
                cnt += processDescriptor(nat.getValue().getMemberDescriptor());
            }

            @Override
            public void visitCustomAttribute(MemberInfo memberInfo, CustomAttribute obj, boolean isCodeAttribute) {
                String[] classes = obj.getReferencedClassNames();
                if ( classes != null ) {
                    for (String cName : classes) {
                        cnt += processClassName(cName);
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
                cnt += processClassName(constant.getBytes());
            }
        }

        ProcessVisitor visitor = new ProcessVisitor();
        new DescendingClassTraverser(visitor).visitClass(classInfo);

        return visitor.getCount();
    }

    private int processDescriptor(Descriptor d) {
        int cnt = 0;

        Type ret = d.getType();
        cnt += processType(ret);

        if (d.isMethod()) {
            for (Type t : d.getArgumentTypes()) {
                cnt += processType(t);
            }
        }

        return cnt;
    }

    private int processType(Type type) {

        if ( type instanceof ArrayType ) {
            return processType( ((ArrayType)type).getBasicType() );
        }
        if ( type instanceof ObjectType ) {
            return processClassName( ((ObjectType)type).getClassName() );
        }
        return 0;
    }

    private int processClassRef(ClassRef ref) {
        return processType( ref.getType() );
    }

    private int processClassName(String className) {
        AppInfo appInfo = AppInfo.getSingleton();
        int cnt = 0;

        ClassInfo cls;
        if ( appInfo.hasClassInfo(className) ) {
            cls = appInfo.getClassInfo(className);
        } else {
            cls = appInfo.loadClass(className);
            if ( cls != null ) {
                newClasses.add(cls);
                cnt++;
            }
        }

        if ( cls != null ) {
            enqueue(cls);
        }
        return cnt;
    }

    private void enqueue(Collection<ClassInfo> roots) {
        for (ClassInfo cls : roots) {
            enqueue(cls);
        }
    }

    private void enqueue(ClassInfo classInfo) {
        if ( !followNatives && AppInfo.getSingleton().isNative(classInfo.getClassName()) ) {
            return;
        }
        if ( visited.contains(classInfo.getClassName()) ) {
            return;
        }
        queue.add(classInfo);
        visited.add(classInfo.getClassName());
    }    

}
