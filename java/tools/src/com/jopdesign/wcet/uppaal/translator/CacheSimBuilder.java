package com.jopdesign.wcet.uppaal.translator;

import java.util.Vector;

import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.model.Transition;

public abstract class CacheSimBuilder {
	public void appendDeclarations(NTASystem system, String NUM_METHODS) {
	}
	
	public void onHit(Transition trans) {
		trans.getAttrs().appendGuard("lastHit");
	}

	public void onMiss(Transition trans) {
		trans.getAttrs().appendGuard("! lastHit");
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

	public boolean isDynamic() {
		return true;
	}
	public boolean isAlwaysMiss() {
		return false;
	}
}
