package com.jopdesign.wcet08.uppaal.translator;

import java.util.Vector;

import com.jopdesign.wcet08.uppaal.model.NTASystem;

public class CacheSimBuilder {

	public void appendDeclarations(NTASystem system, String NUM_METHODS) {
	}

	public static StringBuilder constArray(Vector<?> elems) {
		StringBuilder sb = new StringBuilder("{ ");
		boolean first = true;
		for(Object o : elems) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(o);
		}
		sb.append(" }");
		return sb;
	}
	
}
