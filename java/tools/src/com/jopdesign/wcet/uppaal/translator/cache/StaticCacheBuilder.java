package com.jopdesign.wcet.uppaal.translator.cache;


public class StaticCacheBuilder extends CacheSimBuilder {

	private boolean alwaysMiss;
	public StaticCacheBuilder(boolean alwaysMiss) {
		this.alwaysMiss = alwaysMiss;
	}
	@Override
	public boolean isDynamic() {
		return false;
	}
	@Override
	public boolean isAlwaysMiss() {
		return alwaysMiss;
	}
}
