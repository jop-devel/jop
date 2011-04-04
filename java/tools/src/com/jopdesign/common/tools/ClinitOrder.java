/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)
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

/*
 * Created on 04.06.2005
 *
 */
package com.jopdesign.common.tools;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantMethodInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find a correct order of static class initializers (<clinit>)
 *
 * @author martin
 */
public class ClinitOrder implements ClassVisitor {

    // TODO maybe do this the other way round? (define clinitName,.. in Config or something,
    //      use Config.clinitName... in ClinitOrder; but this way it is easier to remember ..)    
    public static final String clinitName = "<clinit>";
    public static final String clinitDesc = "()V";
    public static final String clinitSig = "<clinit>()V";

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".ClinitOrder");

    private Map<ClassInfo, Set<ClassInfo>> clinit = new HashMap<ClassInfo, Set<ClassInfo>>();

    public ClinitOrder() {
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {
        MethodInfo mi = classInfo.getMethodInfo(clinitSig);
        if (mi != null) {
            Set<ClassInfo> depends = findDependencies(mi, false);
            clinit.put(classInfo, depends);
        }
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }

    private Set<ClassInfo> findDependencies(MethodInfo method, boolean inRec) {

//		System.out.println("find dep. in "+cli.clazz.getClassName()+":"+mi.getMethod().getName());

        Set<ClassInfo> depends = new HashSet<ClassInfo>();
        if (method.isNative() || method.isAbstract()) {
            // nothing to do
            // or should we look for all possible subclasses on
            // abstract.... or in general also all possible
            // subclasses???? :-(
            return depends;
        }

        ClassInfo classInfo = method.getClassInfo();
        ConstantPoolGen cpoolgen = method.getConstantPoolGen();

        InstructionList il = method.getCode().getInstructionList();
        InstructionFinder f = new InstructionFinder(il);

        // TODO can we encounter an empty instruction list?
        //if(il.getStart() == null) {
        //    return depends;
        //}

        // find instructions that access the constant pool
        // collect all indices to constants in ClassInfo
        String cpInstr = "CPInstruction";
        for (Iterator it = f.search(cpInstr); it.hasNext();) {
            InstructionHandle[] match = (InstructionHandle[]) it.next();
            InstructionHandle first = match[0];

            CPInstruction ii = (CPInstruction) first.getInstruction();
            int idx = ii.getIndex();

            Constant co = cpoolgen.getConstant(idx);
            ClassRef classRef = null;
            Set addDepends = null;
            ClassInfo clinfo;
            MethodInfo minfo;

            switch (co.getTag()) {
                case Constants.CONSTANT_Class:
                    classRef = classInfo.getConstantInfo(co).getClassRef();

                    clinfo = classRef.getClassInfo();

                    if (clinfo != null) {
                        minfo = clinfo.getMethodInfo("<init>()V");
                        if (minfo != null) {
                            addDepends = findDependencies(minfo, true);
                        }
                    }
                    // check for all sub classes when no going up the hierarchy
//				if (!inRec) {
//					Object[] y = clinfo.getSubClasses().toArray();
//					for (int i=0; i<y.length; ++i) {
//						clinfo = (ClassInfo) y[i];
//						minfo = clinfo.getMethodInfo("<init>()V");
//						if (minfo!=null) {
//							System.out.println("known sub classes with this method");
//							System.out.println(((ClassInfo) y[i]).clazz.getClassName());						
//						}
//					}					
//				}

                    break;
                case Constants.CONSTANT_Fieldref:
                case Constants.CONSTANT_InterfaceMethodref:
                    classRef = classInfo.getConstantInfo(co).getClassRef();
                    break;
                case Constants.CONSTANT_Methodref:
                    ConstantMethodInfo mref = (ConstantMethodInfo) classInfo.getConstantInfo(co);

                    classRef = mref.getClassRef();
                    minfo = mref.getMethodRef().getMethodInfo();

                    if (minfo != null) {
                        addDepends = findDependencies(minfo, true);
                    }
                    // check for all sub classes when no going up the hierarchy
//				if (!inRec) {
//					Object[] x = clinfo.getSubClasses().toArray();
//					for (int i=0; i<x.length; ++i) {
//						clinfo = (ClassInfo) x[i];
//						minfo = clinfo.getMethodInfo(sigstr);
//						if (minfo!=null) {
//							System.out.println("known sub classes with this method");
//							System.out.println(((ClassInfo) x[i]).clazz.getClassName());						
//						}
//					}					
//				}

                    break;
            }
            if (classRef != null) {
                ClassInfo clinf = classRef.getClassInfo();
                if (clinf != null) {
                    if (clinf.getMethodInfo(clinitSig) != null) {
                        // don't add myself as dependency
                        if (!clinf.equals(method.getClassInfo())) {
                            depends.add(clinf);
                        }
                    }
                }
            }

            if (addDepends != null) {
                for (Object addDepend : addDepends) {
                    ClassInfo addCli = (ClassInfo) addDepend;
                    if (addCli.equals(method.getClassInfo())) {
                        throw new JavaClassFormatError("cyclic indirect <clinit> dependency");
                    }
                    depends.add(addCli);
                }
            }


        }

        return depends;
    }

    /**
     * Print the dependency for debugging. Not used at the moment.
     * @param warn if true, print with warning level
     */
    private void printDependency(boolean warn) {

        Priority lvl = warn ? Level.WARN : Level.DEBUG;

        Set<ClassInfo> cliSet = clinit.keySet();
        for (ClassInfo clinf : cliSet) {
            logger.log(lvl, "Class " + clinf.getClassName());
            Set<ClassInfo> depends = clinit.get(clinf);

            for (ClassInfo clf : depends) {
                logger.log(lvl, "\tdepends " + clf.getClassName());
            }
        }
    }

    /**
     * Find a 'correct' oder for the static <clinit>.
     * Throws an error on cyclic dependencies.
     *
     * @return the ordered list of classes
     * @throws JavaClassFormatError if a cyclic dependency has been found.
     */
    public List<ClassInfo> findOrder() {

        printDependency(false);

        Set<ClassInfo> cliSet = clinit.keySet();
        List<ClassInfo> order = new LinkedList<ClassInfo>();
        int maxIter = cliSet.size();

        // maximum loop bound detects cyclic dependency
        for (int i = 0; i < maxIter && cliSet.size() != 0; ++i) {

            Iterator itCliSet = cliSet.iterator();
            while (itCliSet.hasNext()) {
                ClassInfo clinf = (ClassInfo) itCliSet.next();
                Set<ClassInfo> depends = clinit.get(clinf);
                if (depends.size() == 0) {
                    order.add(clinf);
                    // check all depends sets and remove the added
                    // element (a leave in the dependent tree
                    for (ClassInfo clinfInner : clinit.keySet()) {
                        Set<ClassInfo> dep = clinit.get(clinfInner);
                        dep.remove(clinf);
                    }
                    itCliSet.remove();
                }
            }
        }

        if (cliSet.size() != 0) {
            printDependency(true);
            throw new JavaClassFormatError("Cyclic dependency in <clinit>");
        }

        return order;
    }

}
