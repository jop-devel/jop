package com.jopdesign.wcet08.frontend;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.graphutils.Pair;

public class MethodRef extends Pair<ClassInfo, String> {
	private static final long serialVersionUID = 1L;
	public ClassInfo getReceiver() { return fst(); }
	public String getMethodId()    { return snd(); }
 	public MethodRef(ClassInfo ci, String methodRef) {
		super(ci, methodRef);
	}
 	@Override public String toString() {
 		return getReceiver().clazz.getClassName()+"."+getMethodId();
 	}
	public static MethodRef fromMethodInfo(MethodInfo mi) {
		return new MethodRef(mi.getCli(), mi.methodId);
	}
}
