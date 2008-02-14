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
package joptimizer.actions;

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.bcel.BcelClassInfo;
import joptimizer.config.BoolOption;
import joptimizer.config.JopConfig;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractClassAction;
import joptimizer.framework.actions.ActionException;
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
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Go through all currently loaded classes and load all referenced classes. <br>
 * Already loaded classes are not reloaded. <br>
 *
 * This implementation uses some code from com.jopdesign.build.TransitiveHull
 * to resolve references in the ClassFinder. 
 *  
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class TransitiveHullGenerator extends AbstractClassAction {

    private class ClassFinder extends EmptyVisitor {

        private List queue;
        private Set visited;
        private List newClasses;
        private ConstantPool cp;
        private boolean error;

        public ClassFinder(List queue) {
            this.queue = queue;
            newClasses = new LinkedList();
            error = false;

            visited = new HashSet( queue.size() * 3 );
            for (Iterator it = queue.iterator(); it.hasNext();) {
                JavaClass javaClass = (JavaClass) it.next();
                visited.add(javaClass.getClassName());
            }
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public JavaClass getNext() {
            return (JavaClass) queue.remove(0);
        }
        
        public void startClass(ConstantPool cp) {
            this.cp = cp;
        }

        public boolean hasError() {
            return error;
        }

        public List getNewClasses() {
            return newClasses;
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
                Type type = Type.getReturnType(signature);

                if(type instanceof ObjectType) {
                    addClass(((ObjectType)type).getClassName());
                }

                Type[] types = Type.getArgumentTypes(signature);

                for(int i = 0; i < types.length; i++) {
                    type = types[i];
                    if(type instanceof ObjectType) {
                        addClass(((ObjectType)type).getClassName());
                    }
                }
            } else {
                Type type = Type.getType(signature);
                if(type instanceof ObjectType) {
                    addClass(((ObjectType)type).getClassName());
                }
            }

        }

        private void addClass(String className) {

            className = className.replace('/','.');

            if ( visited.contains(className) ) {
                return;
            } else {
                visited.add(className);
            }

            if ( doEnqueueClass(className) ) {
                JavaClass newClass = null;

                try {
                    newClass = loadJavaClass(className);
                } catch (ActionException e) {
                    logger.error("Could not load class {"+className+"}.",e);
                    error = true;
                }

                if ( newClass != null ) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add class " + newClass.getClassName());
                    }
                    queue.add(newClass);
                    newClasses.add(newClass);
                }
            }
        }

        private boolean doEnqueueClass(String className) {

            // ignore array classes
            if ( className.startsWith("[") ) {
                if (logger.isInfoEnabled()) logger.info("Ingored array class {" + className + "}.");
                return false;
            }

            if ( ignoreNative && getJopConfig().isNativeClassName(className) ) {
                if (logger.isInfoEnabled()) logger.info("Ignored system class {" + className + "}.");
                return false;
            }

            return !getJoptimizer().getAppStruct().contains(className);
        }

    }

    public static final String ACTION_NAME = "loadtransitivehull";

    public static final String CONF_NATIVE = "nonative";

    private static final Logger logger = Logger.getLogger(TransitiveHullGenerator.class);

    private boolean ignoreNative;

    public TransitiveHullGenerator(String name, JOPtimizer joptimizer) {
        super(name, joptimizer);
        ignoreNative = false;
    }

    public void appendActionArguments(String prefix, List options) {
        options.add( new BoolOption(CONF_NATIVE, "Do not load native classes.") );
    }

    public String getActionDescription() {
        return "Go through all classes and load all referenced classes.";
    }

    public boolean doModifyClasses() {
        return false;
    }

    public boolean configure(String prefix, JopConfig config) {

        ignoreNative = config.isEnabled(prefix + CONF_NATIVE);

        return true;
    }

    public void execute() throws ActionException {

        Collection classes = getJoptimizer().getAppStruct().getClassInfos();
        List queue = new LinkedList();

        int i = 0;
        for (Iterator it = classes.iterator(); it.hasNext();) {
            ClassInfo classInfo = (ClassInfo) it.next();
            JavaClass clazz = loadJavaClass(classInfo);
            
            if ( clazz != null ) {
                queue.add(clazz);
            }
        }

        loadTransitiveHull(queue);
    }

    public void execute(ClassInfo classInfo) throws ActionException {

        List queue = new LinkedList();
        JavaClass clazz = loadJavaClass(classInfo);

        if ( clazz != null ) {
            queue.add(clazz);
            loadTransitiveHull(queue);
        }
    }

    public void loadTransitiveHull(List queue) {

        ClassFinder classFinder = new ClassFinder(queue);

        if ( logger.isInfoEnabled() ) {
            logger.info("Starting transitive hull search..");
        }

        while ( classFinder.hasNext() ) {
            JavaClass next = classFinder.getNext();

            if (logger.isInfoEnabled()) {
                logger.info("Processing class " + next.getClassName());
            }

            classFinder.startClass(next.getConstantPool());
            new DescendingVisitor(next, classFinder).visit();
        }

        if ( logger.isInfoEnabled() ) {
            logger.info("Transitive hull search done; creating classInfos..");
        }

        getJoptimizer().addClasses(classFinder.getNewClasses());

    }

    private JavaClass loadJavaClass(ClassInfo classInfo) throws ActionException {
        if ( classInfo instanceof BcelClassInfo) {
            return ((BcelClassInfo) classInfo).getJavaClass();
        } else {
            return loadJavaClass( classInfo.getClassName() );
        }
    }

    private JavaClass loadJavaClass(String className) throws ActionException {
        try {
            return getJoptimizer().createJavaClass(className);
        } catch (IOException e) {
            if ( getJopConfig().doAllowIncompleteCode() ) {
                logger.warn("Could not load class {"+className+"}, ignored.", e);
            } else {
                throw new ActionException("Could not load class {"+className+"}.", e);
            }
        }
        return null;
    }

}
