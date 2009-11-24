/**
 *
 */
package com.jopdesign.wcet.analysis;

import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;

public class AnalysisContextLocal extends AnalysisContextIpet {

	public final CallString callString;

	public AnalysisContextLocal(StaticCacheApproximation mode, CallString callString) {
		super(mode);
		this.callString = callString;
	}
	public AnalysisContextLocal(StaticCacheApproximation mode) {
		super(mode);
		this.callString = CallString.EMPTY;
	}
	@Override
	public AnalysisContextLocal withCacheApprox(StaticCacheApproximation mode) {
		return new AnalysisContextLocal(mode, this.callString);
	}
	public AnalysisContextLocal withCallString(CallString cs)
	{
		return new AnalysisContextLocal(this.cacheApprox, cs);
	}
	@Override
	public CallString getCallString()
	{
		return callString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + callString.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		AnalysisContextLocal other = (AnalysisContextLocal) obj;
		if (!cacheApprox.equals(other.cacheApprox)) return false;
		if (!callString.equals(other.callString)) return false;
		return true;
	}
	@Override
	public String toString() {
		String s = super.toString();
		if(getCallString().isEmpty()) return s;
		return s+"-"+getCallString();
	}
}
