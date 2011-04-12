/*
This file is part of JOP, the Java Optimized Processor
	see <http://www.jopdesign.com/>

Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.common.processormodel;

import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.IntegerOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.timing.WCETInstruction;
import com.jopdesign.timing.jop.MicrocodeAnalysis;

import java.io.File;

public class JOPConfig {

    public static final StringOption ASM_FILE =
            new StringOption("jop-asm-file", "JOP assembler file", MicrocodeAnalysis.DEFAULT_ASM_FILE.getPath());

    // FIXME: default values are fetched from WCETInstruction until transition to
    // new timing system is complete
    public static final IntegerOption READ_WAIT_STATES =
            new IntegerOption("jop-rws", "JOP read wait states", WCETInstruction.r);
    public static final IntegerOption WRITE_WAIT_STATES =
            new IntegerOption("jop-wws", "JOP write wait states", WCETInstruction.w);
    public static final BooleanOption MULTIPROCESSOR =
            new BooleanOption("jop-cmp", "JOP multiprocessor configuration", WCETInstruction.CMP_WCET);
    public static final IntegerOption CMP_CPUS =
            new IntegerOption("jop-cmp-cpus", "JOP number of processors", WCETInstruction.CPUS);
    public static final IntegerOption CMP_TIMESLOT =
            new IntegerOption("jop-cmp-timeslot", "JOP arbiter timeslot cycles", WCETInstruction.TIMESLOT);

    public static final BooleanOption OBJECT_CACHE =
            new BooleanOption("jop-object-cache", "JOP object cache configuration", false);
    public static final IntegerOption OBJECT_CACHE_ASSOCIATIVITY =
            new IntegerOption("jop-ocache-associativity", "JOP object associativity", 16);
    public static final IntegerOption OBJECT_CACHE_WORDS_PER_LINE =
            new IntegerOption("jop-ocache-words-per-line", "JOP object cache: words per line", 16);
    public static final IntegerOption OBJECT_CACHE_BLOCK_SIZE =
            new IntegerOption("jop-ocache-fill", "JOP object cache: size of a cache block in words (burst)", 1);
    private static final IntegerOption OBJECT_CACHE_HIT_CYCLES =
            new IntegerOption("jop-ocache-hit-cycles", "JOP object access cycles on cache hit", 1);
    private static final IntegerOption OBJECT_CACHE_LOAD_FIELD_CYCLES =
            new IntegerOption("jop-ocache-load-field-cycles", "JOP object cache load cycles for field (bypass)", 2);
    private static final IntegerOption OBJECT_CACHE_LOAD_BLOCK_CYCLES =
            new IntegerOption("jop-ocache-load-line-cycles", "JOP object cache load cycles for cache block (miss)", 2);

    /**
     * Supported method cache implementations:
     * <ul>
     * <li/> LRU_CACHE: N - Block LRU cache
     * <li/> FIFO_VARBLOCK_CACHE: Variable Block cache
     * <li/> NO_METHOD_CACHE: Assume there are no method cache misses
     * </ul>
     */
    public enum CacheImplementation {
        LRU_CACHE, FIFO_CACHE,
        LRU_VARBLOCK_CACHE, FIFO_VARBLOCK_CACHE,
        NO_METHOD_CACHE
    }

    public static final EnumOption<CacheImplementation> CACHE_IMPL =
            new EnumOption<CacheImplementation>(
                    "cache-impl",
                    "method cache implementation",
                    CacheImplementation.FIFO_VARBLOCK_CACHE);

    public static final IntegerOption CACHE_BLOCKS =
            new IntegerOption("cache-blocks", "number of cache blocks", 16);

    public static final IntegerOption CACHE_SIZE_WORDS =
            new IntegerOption("cache-size-words",
                    "size of the cache in words",
                    1024);

    private static final Option<?>[] jopOptions = {
            ASM_FILE, READ_WAIT_STATES, WRITE_WAIT_STATES,

            MULTIPROCESSOR, CMP_CPUS, CMP_TIMESLOT,

            CACHE_IMPL, CACHE_BLOCKS, CACHE_SIZE_WORDS,

            OBJECT_CACHE, OBJECT_CACHE_ASSOCIATIVITY, OBJECT_CACHE_WORDS_PER_LINE,
            OBJECT_CACHE_BLOCK_SIZE, OBJECT_CACHE_HIT_CYCLES,
            OBJECT_CACHE_LOAD_FIELD_CYCLES, OBJECT_CACHE_LOAD_BLOCK_CYCLES
    };


    private int rws;
    private int wws;
    private boolean cmp;
    private int cpus;
    private int timeslot;
    private File asmFile;

    private CacheImplementation objectCacheName;
    private int objectCacheAssociativity;
    private int objectCacheBlockSize;
    private boolean objectCacheFieldTag;
    private int objectCacheLineSize;
    private long objectCacheHitCycles;
    private long objectCacheLoadFieldCycles;
    private long objectCacheLoadBlockCycles;

    public static OptionGroup getOptions(Config configData) {
        return configData.getOptions().getGroup("jop");
    }

    public static void registerOptions(Config configData) {
        OptionGroup jopConfig = getOptions(configData);
        jopConfig.addOptions(jopOptions);
    }

    public JOPConfig(Config configData) {
    	OptionGroup jopConfig = getOptions(configData);
        this.asmFile = new File(jopConfig.getOption(ASM_FILE));

        this.rws = jopConfig.getOption(READ_WAIT_STATES).intValue();
        this.wws = jopConfig.getOption(WRITE_WAIT_STATES).intValue();
        this.cmp = jopConfig.getOption(MULTIPROCESSOR);
        this.cpus = jopConfig.getOption(CMP_CPUS).intValue();
        this.timeslot = jopConfig.getOption(CMP_TIMESLOT).intValue();

        this.objectCacheName = jopConfig.getOption(CACHE_IMPL);

        this.objectCacheAssociativity = jopConfig.getOption(OBJECT_CACHE_ASSOCIATIVITY).intValue();
        this.objectCacheBlockSize = jopConfig.getOption(OBJECT_CACHE_BLOCK_SIZE).intValue();
        this.objectCacheFieldTag = false;
        this.objectCacheLineSize = jopConfig.getOption(OBJECT_CACHE_WORDS_PER_LINE).intValue();

        this.objectCacheHitCycles = jopConfig.getOption(OBJECT_CACHE_HIT_CYCLES);
        this.objectCacheLoadFieldCycles = jopConfig.getOption(OBJECT_CACHE_LOAD_FIELD_CYCLES);
        this.objectCacheLoadBlockCycles = jopConfig.getOption(OBJECT_CACHE_LOAD_BLOCK_CYCLES);
    }

    public int rws() {
        return rws;
    }

    public int wws() {
        return wws;
    }

    public boolean isCmp() {
        return cmp;
    }

    public int getCpus() {
        return cpus;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public File getAsmFile() {
        return asmFile;
    }

    public boolean isObjectCacheFieldTag() {
        return objectCacheFieldTag;
    }

    public long getObjectCacheHitCycles() {
        return objectCacheHitCycles;
    }

    public CacheImplementation getCacheName() {
        return objectCacheName;
    }

    /**
     * @return the associativity of the object cache
     */
    public long getObjectCacheAssociativity() {
        return this.objectCacheAssociativity;
    }

    public void setObjectCacheAssociativity(int assoc) {
        this.objectCacheAssociativity = assoc;
    }

    public boolean objectCacheSingleField() {
        return this.objectCacheFieldTag;
    }

    /**
     * @param b whether to use fields as tag (only for experiments)
     */
    public void setObjectCacheFieldTag(boolean b) {
        this.objectCacheFieldTag = b;
    }

    public int getObjectCacheLineSize() {
        return objectCacheLineSize;
    }

    public int getObjectCacheBlockSize() {
        return objectCacheBlockSize;
    }

    public int getObjectCacheMaxCachedFieldIndex() {
        if (this.objectCacheSingleField()) return Integer.MAX_VALUE;
        else return objectCacheLineSize - 1;
    }

    public void setObjectCacheLineSize(int lineSize) {
        this.objectCacheLineSize = lineSize;
    }

    public void setObjectCacheBlockSize(int blockSize) {
        this.objectCacheBlockSize = blockSize;
    }

    public long getObjectCacheLoadBlockCycles() {
        return this.objectCacheLoadBlockCycles;
    }

    public long getObjectCacheBypassTime() {
        return this.objectCacheLoadFieldCycles;
    }

    public void setObjectCacheHitCycles(long objectCacheHitCycles) {
        this.objectCacheHitCycles = objectCacheHitCycles;
    }

    public void setObjectCacheLoadFieldCycles(long objectCacheLoadFieldCycles) {
        this.objectCacheLoadFieldCycles = objectCacheLoadFieldCycles;
    }

    public void setObjectCacheLoadBlockCycles(long objectCacheLoadBlockCycles) {
        this.objectCacheLoadBlockCycles = objectCacheLoadBlockCycles;
    }

    /* Removed for now, as is not flexible enough */
//	public long getObjectCacheAccessTime(int words) {
//		int  burstLength = this.objectCacheMaxBurst;
//		long delay       = this.objectCacheAccessDelay;
//		long cyclesPerWord = this.objectCacheCyclesPerWord;
//		int fullBursts = words / burstLength;
//		int lastBurst  = words % burstLength;
//		long accessTime = delay + cyclesPerWord * lastBurst;
//		for(int i = 0; i < fullBursts; i++) {
//			accessTime += delay + cyclesPerWord * burstLength;
//		}
//		return accessTime;
//	}

}
