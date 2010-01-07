package com.jopdesign.wcet.analysis;

import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;

public class AnalysisContextIpet extends AnalysisContext  {
	protected final StaticCacheApproximation cacheApprox;

	public AnalysisContextIpet(StaticCacheApproximation mode) {
		this.cacheApprox = mode;
	}

	public AnalysisContextIpet withCacheApprox(StaticCacheApproximation newMode) {
		return new AnalysisContextIpet(newMode);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + cacheApprox.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		AnalysisContextLocal other = (AnalysisContextLocal) obj;
		if (!cacheApprox.equals(other.cacheApprox)) return false;
		return true;
	}
	@Override
	public String toString() {
		return "ctx_"+cacheApprox.toString();
	}
}
