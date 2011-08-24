/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.ipet;

import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;

import java.io.File;

public class IPETConfig {

    /**
     * Static cache approximations:
     * <ul>
     * <li/> ALL_FIT_REGIONS (in all fit regions, miss at most once)
     * <li/> ALL_FIT_SIMPLE (in all fit regions, miss exactly once)
     * <li/> ALWAYS_MISS (all accesses are cache misses)
     * <li/> ALWAYS_HIT (all accesses are hits) [UNSAFE]
     * <li/> GLOBAL_ALL_FIT (assume all methods fit in the cache) [UNSAFE]
     * <ul/>
     * For the all fit approximations, note that
     * <ul>
     * <li/> FIFO CACHE: If all fit, assume miss (at most) once on return
     * <li/> LRU CACHE: If all fit, assume miss (at most) once on invoke
     * </ul>
     */
    public enum CacheCostCalculationMethod {
        ALL_FIT_REGIONS, ALL_FIT_COST, ALL_FIT_SIMPLE, ALWAYS_MISS,
        /* unsafe */ GLOBAL_ALL_FIT, ALWAYS_HIT;

        public boolean needsInterProcIPET() {
            return this == CacheCostCalculationMethod.ALL_FIT_REGIONS ||
                   this == CacheCostCalculationMethod.GLOBAL_ALL_FIT;
        }
    }

    public static final EnumOption<CacheCostCalculationMethod> STATIC_CACHE_APPROX =
            new EnumOption<CacheCostCalculationMethod>(
                    "ipet-cache-approx",
                    "cache approximation for IPET",
                    CacheCostCalculationMethod.ALL_FIT_REGIONS);

    public static final BooleanOption ASSUME_MISS_ONCE_ON_INVOKE =
            new BooleanOption("ipet-assume-miss-once-on-invoke",
                    "assume method cache loads in miss-once areas always happen on invoke (unsafe)",
                    false);

    public static final BooleanOption DUMP_ILP =
            new BooleanOption("ipet-dump-ilp", "whether the LP problems should be dumped to files", true);

    public static final StringOption ILP_OUTDIR =
            new StringOption("ipet-out", "the output directory for the solver", "${outdir}/ilps");

    public static final Option<?>[] ipetOptions = {
            STATIC_CACHE_APPROX, ASSUME_MISS_ONCE_ON_INVOKE, DUMP_ILP, ILP_OUTDIR
    };

    private boolean assumeMissOnceOnInvoke;
    private boolean dumpIlp;
    private File outDir;

    private IPETConfig() {
    }

    public IPETConfig(Config c) {
        this.outDir = new File(c.getOption(ILP_OUTDIR));
        this.assumeMissOnceOnInvoke = c.getOption(ASSUME_MISS_ONCE_ON_INVOKE);
        this.dumpIlp = c.getOption(DUMP_ILP);
    }

    public void setAssumeMissOnceOnInvoke(boolean assumeMissOnceOnInvoke) {
        this.assumeMissOnceOnInvoke = assumeMissOnceOnInvoke;
    }

    public boolean doAssumeMissOnceOnInvoke() {
        return assumeMissOnceOnInvoke;
    }

    public boolean doDumpIlp() {
        return dumpIlp;
    }

    public File getOutDir() {
        return outDir;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    @Override
    public IPETConfig clone() {
        IPETConfig ipc = new IPETConfig();
        ipc.assumeMissOnceOnInvoke = this.assumeMissOnceOnInvoke;
        ipc.dumpIlp = this.dumpIlp;
        ipc.outDir = this.outDir;
        return ipc;
    }

    public static CacheCostCalculationMethod getRequestedCacheApprox(Config config) {
        return config.getOption(STATIC_CACHE_APPROX);
    }

}
