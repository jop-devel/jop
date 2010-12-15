package com.jopdesign.wcet.uppaal.translator.cache;

import com.jopdesign.wcet.uppaal.model.Transition;

public abstract class DynamicCacheBuilder extends CacheSimBuilder {
	public boolean isDynamic() {
		return true;
	}
	public boolean isAlwaysMiss() {
		return false;
	}
	public void onHit(Transition trans) {
		trans.getAttrs().appendGuard("lastHit");
	}

	public void onMiss(Transition trans) {
		trans.getAttrs().appendGuard("! lastHit");
	}

}
