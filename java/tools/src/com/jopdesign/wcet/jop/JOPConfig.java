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
package com.jopdesign.wcet.jop;

import java.io.File;

import com.jopdesign.timing.WCETInstruction;
import com.jopdesign.timing.jop.MicrocodeAnalysis;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.config.BooleanOption;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.EnumOption;
import com.jopdesign.wcet.config.IntegerOption;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.config.StringOption;

public class JOPConfig {
	private Config configData;
	public int rws;
	public int wws;
	public boolean cmp;
	public Long cpus;
	public Long timeslot;
	public File asmFile;

	public JOPConfig(Project p) {
		configData = p.getConfig();
		this.asmFile = new File(configData.getOption(ASM_FILE));
		this.rws = configData.getOption(READ_WAIT_STATES).intValue();
		this.wws = configData.getOption(WRITE_WAIT_STATES).intValue();
		this.cmp = configData.getOption(MULTIPROCESSOR);
		this.cpus = configData.getOption(CMP_CPUS);
		this.timeslot = configData.getOption(CMP_TIMESLOT);
	}
	// FIXME: default values are fetched from WCETInstruction until transition to
	// new timing system is complete
	public static final StringOption ASM_FILE =
		new StringOption("jop-asm-file","JOP assembler file",MicrocodeAnalysis.DEFAULT_ASM_FILE.getAbsolutePath());
	public static final IntegerOption READ_WAIT_STATES =
		new IntegerOption("jop-rws","JOP read wait states",WCETInstruction.r);
	public static final IntegerOption WRITE_WAIT_STATES =
		new IntegerOption("jop-wws","JOP write wait states",WCETInstruction.w);
	public static final BooleanOption MULTIPROCESSOR =
		new BooleanOption("jop-cmp","JOP multiprocessor configuration",WCETInstruction.CMP_WCET);
	public static final IntegerOption CMP_CPUS =
		new IntegerOption("jop-cmp-cpus", "JOP number of processors",WCETInstruction.CPUS);
	public static final IntegerOption CMP_TIMESLOT =
		new IntegerOption("jop-cmp-timeslot", "JOP arbiter timeslot cycles",WCETInstruction.TIMESLOT);
	
	/**
	 * Supported method cache implementations:
	 * <ul>
	 *  <li/> LRU_CACHE: N - Block LRU cache
	 *  <li/> FIFO_VARBLOCK_CACHE: Variable Block cache
	 *  <li/> NO_METHOD_CACHE: Assume there are no method cache misses
	 * </ul>
	 */
	public enum CacheImplementation {
		LRU_CACHE, FIFO_CACHE,
		LRU_VARBLOCK_CACHE, FIFO_VARBLOCK_CACHE,
		NO_METHOD_CACHE, 
	}
	public static final EnumOption<CacheImplementation> CACHE_IMPL =
		new EnumOption<CacheImplementation>(
				"cache-impl",
				"method cache implementation",
				CacheImplementation.FIFO_VARBLOCK_CACHE);
	public static final IntegerOption CACHE_BLOCKS =
		new IntegerOption("cache-blocks","number of cache blocks",16);

	public static final IntegerOption CACHE_SIZE_WORDS =
		new IntegerOption("cache-size-words",
						  "size of the cache in words",
						  1024);
			
	public static final Option<?>[] jopOptions = {
		ASM_FILE, READ_WAIT_STATES, WRITE_WAIT_STATES,
		MULTIPROCESSOR, CMP_CPUS, CMP_TIMESLOT,
		CACHE_IMPL, CACHE_BLOCKS, CACHE_SIZE_WORDS
	};
}
