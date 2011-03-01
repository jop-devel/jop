/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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

package com.jopdesign.common.processormodel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.tools.JopInstr;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JOPModel implements ProcessorModel {

    public static final String JVM_CLASS = "com.jopdesign.sys.JVM";
    public static final String STARTUP_CLASS = "com.jopdesign.sys.Startup";
    public static final String JOP_NATIVE = "com.jopdesign.sys.Native";

    private final String identifier;
    private final JOPConfig config;

    public static boolean isNewOp(Instruction ii) {
        return 	(ii instanceof NEW || ii instanceof NEWARRAY || ii instanceof ANEWARRAY);
    }
    public static boolean isThrowOp(Instruction ii) {
        return ii instanceof ATHROW;
    }

    public JOPModel(Config configData) {
        StringBuffer key = new StringBuffer();
        this.config = new JOPConfig(configData);
        key.append("jop");
        if(config.isCmp()) key.append("-cmp");
        // TODO this may differ from WCETProcessorModel.getName(), check.
        key.append("-").append(config.getCacheName());
        identifier = key.toString();
    }

    public String getName() {
        return identifier;
    }

    public JOPConfig getConfig() {
        return config;
    }

    public boolean isSpecialInvoke(MethodInfo context, Instruction i) {
        if(! (i instanceof INVOKESTATIC)) return false;
        INVOKESTATIC isi = (INVOKESTATIC) i;
        ReferenceType refTy = isi.getReferenceType(context.getConstantPoolGen());
        if(refTy instanceof ObjectType){
            ObjectType objTy = (ObjectType) refTy;
            String className = objTy.getClassName();
            return (className.equals(JOP_NATIVE));
        } else {
            return false;
        }
    }

    /* FIXME: [NO THROW HACK] */
    public boolean isImplementedInJava(Instruction ii) {
        return (JopInstr.isInJava(ii.getOpcode()) && !isNewOp(ii) && !isThrowOp(ii));
    }

    public MethodInfo getJavaImplementation(AppInfo ai, MethodInfo context, Instruction instr) {
        ClassInfo receiver = ai.getClassInfo(JVM_CLASS);
        String methodName = "f_"+instr.getName();

        Set<MethodInfo> mi = receiver.getMethodByName(methodName);
        if (!mi.isEmpty()) {
            if (mi.size() > 1) {
                throw new JavaClassFormatError("JVM class " + JVM_CLASS + " has more than one implementation of "
                        + methodName);
            }
            return mi.iterator().next();
        }
        return null;
    }

    public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
        int opCode = getNativeOpCode(context, instruction);
        if(opCode >= 0) return JopInstr.len(opCode);
        else throw new AssertionError("Invalid opcode: "+context+" : "+instruction);
    }

    /* performance hot spot */
    public int getNativeOpCode(MethodInfo context, Instruction instr) {
        if(isSpecialInvoke(context,instr)) {
            INVOKESTATIC isi = (INVOKESTATIC) instr;
            String methodName = isi.getMethodName(context.getConstantPoolGen());
            return JopInstr.getNative(methodName);
        } else {
            return instr.getOpcode();
        }
    }

    public List<String> getJVMClasses() {
        List<String> jvmClasses = new ArrayList<String>(1);
        jvmClasses.add(JVM_CLASS);
        jvmClasses.add(STARTUP_CLASS);
        return jvmClasses;
    }

    @Override
    public List<String> getNativeClasses() {
        List<String> jvmClasses = new ArrayList<String>(1);
        jvmClasses.add(JOP_NATIVE);
        return jvmClasses;
    }

}
