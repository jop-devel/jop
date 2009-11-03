package com.jopdesign.wcet.ipet;

import com.jopdesign.wcet.config.BooleanOption;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.EnumOption;
import com.jopdesign.wcet.config.IntegerOption;
import com.jopdesign.wcet.config.Option;

public class IpetConfig {
	  /** Static cache approximations:
	 * <ul>
	 *   <li/> ALL_FIT_REGIONS (in all fit regions, miss at most once)
	 *   <li/> ALL_FIT_SIMPLE (in all fit regions, miss exactly once)
	 *   <li/> ALWAYS_MISS (all accesses are cache misses)
	 *   <li/> ALWAYS_HIT (all accesses are hits) [UNSAFE]
	 *   <li/> GLOBAL_ALL_FIT (assume all methods fit in the cache) [UNSAFE]
	 * <ul/>
	 * For the all fit approximations, note that
	 * <ul>
	 *     <li/> FIFO CACHE: If all fit, assume miss (at most) once on return
	 *     <li/> LRU CACHE: If all fit, assume miss (at most) once on invoke
	 * </ul>
	 */
	public enum StaticCacheApproximation {
		ALL_FIT_REGIONS, ALL_FIT_SIMPLE, ALWAYS_MISS, GLOBAL_ALL_FIT, ALWAYS_HIT;
		public boolean needsInterProcIPET() {
			return this == StaticCacheApproximation.ALL_FIT_REGIONS ||
			       this == StaticCacheApproximation.GLOBAL_ALL_FIT; }
	};
	public static final EnumOption<StaticCacheApproximation> STATIC_CACHE_APPROX =
		new EnumOption<StaticCacheApproximation>(
				"ipet-cache-approx",
				"cache approximation for IPET",
				StaticCacheApproximation.ALL_FIT_REGIONS);

	public static final BooleanOption ASSUME_MISS_ONCE_ON_INVOKE =
		new BooleanOption("ipet-assume-miss-once-on-invoke",
						  "assume method cache loads in miss-once areas always happen on invoke (unsafe)",
						  false);

	public static final BooleanOption DUMP_ILP =
		new BooleanOption("ipet-dump-ilp","whether the LP problems should be dumped to files",true);


	public static final Option<?>[] ipetOptions = {
		STATIC_CACHE_APPROX, ASSUME_MISS_ONCE_ON_INVOKE, DUMP_ILP
	};
	public boolean assumeMissOnceOnInvoke;
	public boolean dumpIlp;

	private IpetConfig() {}
	public IpetConfig(Config c) {
		this.assumeMissOnceOnInvoke = c.getOption(ASSUME_MISS_ONCE_ON_INVOKE);
		this.dumpIlp = c.getOption(DUMP_ILP);
	}
	@Override
	public IpetConfig clone() {
		IpetConfig ipc = new IpetConfig();
		ipc.assumeMissOnceOnInvoke = this.assumeMissOnceOnInvoke;
		ipc.dumpIlp = this.dumpIlp;
		return ipc;
	}
	public static StaticCacheApproximation getPreciseCacheApprox(Config config) {
		return config.getOption(STATIC_CACHE_APPROX);
	}

}
