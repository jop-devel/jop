package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.wcet.uppaal.model.NTASystem;

public abstract class CacheSimBuilder {
	public void appendDeclarations(NTASystem system, String NUM_METHODS) {
	}
	public abstract boolean isDynamic();
	public abstract boolean isAlwaysMiss();
}
