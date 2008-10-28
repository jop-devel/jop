package com.jopdesign.dfa.framework;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class MethodHelper {

	public static int getArgSize(InvokeInstruction m, ConstantPoolGen cp) {
		int retval = 0;
		if (!(m instanceof INVOKESTATIC)) {
			retval += 1;
		}
        Type at[] = m.getArgumentTypes(cp);
        for (int i = 0; i < at.length; ++i) {
        	retval += at[i].getSize();
        }
        return retval;
	}

	public static int getArgSize(MethodGen m) {
		int retval = 0;
		if (!m.isStatic()) {
			retval += 1;
		}	
        Type at[] = m.getArgumentTypes();
        for (int i = 0; i < at.length; ++i) {
        	retval += at[i].getSize();
        }
        return retval;
	}

}
