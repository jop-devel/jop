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
package com.jopdesign.wcet08.uppaal.translator;

import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.jop.BlockCache;
import com.jopdesign.wcet08.jop.VarBlockCache;
import com.jopdesign.wcet08.jop.CacheConfig;
import com.jopdesign.wcet08.jop.MethodCache;
import com.jopdesign.wcet08.jop.CacheConfig.CacheImplementation;
import com.jopdesign.wcet08.jop.CacheConfig.DynCacheApproximation;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet08.uppaal.model.NTASystem;
import com.jopdesign.wcet08.uppaal.model.Template;
import com.jopdesign.wcet08.uppaal.model.XmlSerializationException;

/**
 * Builder for the system modeling the java program
 */
public class SystemBuilder {
	
	public static final String CLOCK = "t";
	public static final String INVOKE_CHAN = "invoke";
	public static final String RETURN_CHAN = "ret";
	public static       String methodChannel(int i) { return "invoke_"+i; }
	public static final String MAX_CALL_STACK_DEPTH = "max_csd";
	public static final String NUM_METHODS = "num_methods";
	public static final String ACTIVE_METHOD = "active_method";
	public static final String CURRENT_CALL_STACK_DEPTH = "cur_csd";

	private NTASystem system;
	public NTASystem getNTASystem() { return system; }
	
	private Hashtable<Template,Integer> templates = new Hashtable<Template,Integer>();
	private Hashtable<MethodInfo,Integer> methodId = new Hashtable<MethodInfo, Integer>();
	private CacheSimBuilder cacheSim;
	private Project project;
	/**
	 * Create a new top-level UPPAAL System builder
	 * @param name               the name of the system
	 * @param maxCallStackDepth  the maximal call stack depth
	 * @param numMethods         the number of methods of the program
	 */
	public SystemBuilder(Project p, int maxCallStackDepth, List<MethodInfo> methods) {
		this.project = p;
		this.system = new NTASystem(p.getProjectName());
		int numMethods = methods.size();
		for(int i = 0; i < numMethods; i++) {
			methodId.put(methods.get(i), i);
		}
		Config config = project.getConfig();
		boolean assumeEmptyCache = config.getOption(UppAalConfig.UPPAAL_EMPTY_INITIAL_CACHE);
		MethodCache cache = project.getProcessorModel().getMethodCache();
		DynCacheApproximation cacheSim = Config.instance().getOption(CacheConfig.DYNAMIC_CACHE_APPROX);
		if(cache.getName() == CacheImplementation.NO_METHOD_CACHE
		  || cacheSim == DynCacheApproximation.ALWAYS_HIT) {
			this.cacheSim = new StaticCacheBuilder(false);
		} else if(cacheSim == DynCacheApproximation.ALWAYS_MISS) {
			this.cacheSim = new StaticCacheBuilder(true);
		} else {
			switch(cache.getName()) {
			case LRU_CACHE: 
				this.cacheSim = new LRUCacheBuilder((BlockCache)cache);
				break;
			case FIFO_CACHE:
				this.cacheSim = new FIFOCacheBuilder((BlockCache)cache, assumeEmptyCache);
				break;
			case FIFO_VARBLOCK_CACHE: 
				this.cacheSim = new VarBlockCacheBuilder(p,(VarBlockCache)cache, numMethods, assumeEmptyCache);
				break;
			default: throw new AssertionError("Unsupport cache implementation: "+cache.getName());
			}
		}
		initialize(maxCallStackDepth, methods);
	}
	/* -- Using global-local clock
	 * com.jopdesign.build.MethodInfo@cdbc83"wcet.StartLift.measure()V"
     * wcet: 9797
     * complex: 98
     * searchT: 4.58302
     * solverTmax: 0.265957
     * -- Using per method-local clock
     * com.jopdesign.build.MethodInfo@cdbc83"wcet.StartLift.measure()V"
     * wcet: 9797
     * complex: 98
     * searchT: 7.969566
     * solverTmax: 0.491255
     */
	private void initialize(int maxCallStackDepth, List<MethodInfo> methodInfos) {
		system.appendDeclaration("clock " + CLOCK +";");
		system.appendDeclaration(
				String.format("clock %s; ", TemplateBuilder.LOCAL_CLOCK));

		system.appendDeclaration("const int " + MAX_CALL_STACK_DEPTH + " = "+maxCallStackDepth+";"); 
		system.appendDeclaration("const int " + NUM_METHODS + " = "+methodInfos.size()+";"); 
		if(Config.instance().getOption(UppAalConfig.UPPAAL_ONE_CHANNEL_PER_METHOD)) {
			for(MethodInfo i : methodInfos) {
				int id = project.getFlowGraph(i).getId();
				system.appendDeclaration("chan "+methodChannel(id)+";");				
			}
		} else {
			system.appendDeclaration("chan "+INVOKE_CHAN+";");
			system.appendDeclaration("chan "+RETURN_CHAN+";");
			system.appendDeclaration("int[0,"+ NUM_METHODS+ "] "+ ACTIVE_METHOD  +";");
			system.appendDeclaration("int[0,"+ MAX_CALL_STACK_DEPTH +"] "+CURRENT_CALL_STACK_DEPTH+";");
		}
		cacheSim.appendDeclarations(system,NUM_METHODS);
	}
	
	public void buildSystem() {
		StringBuilder sys = new StringBuilder();
		/* Create processes */
		for(Entry<Template,Integer> tmpl : templates.entrySet()) {
			sys.append(String.format("M%s = %s() ;\n", 
					   tmpl.getValue(), tmpl.getKey().getId()));
		}
		/* Instantiate processes */
		sys.append("system ");
		
		/* Please change the first/else idiom by one similar to ruby's join  */
		boolean first = true;
		for(Entry<Template,Integer> tmpl : templates.entrySet()) {
			if(first) first = false;
			else      sys.append(", ");
			sys.append("M"+tmpl.getValue());
		}		
		sys.append(";\n");
		this.system.setSystem(sys.toString());
	}

	public void addTemplate(int procid, Template templ) throws DuplicateKeyException {
		this.templates.put(templ,procid);
		this.system.addTemplate(templ);
	}

	public Document toXML() throws XmlSerializationException {
		return this.system.toXML();
	}
	public Project getProject() {
		return project;
	}
	public CacheSimBuilder getCacheSim() {
		return this.cacheSim;
	}
	public int getMethodId(MethodInfo implementedMethod) {
		return this.methodId.get(implementedMethod);
	}
	public String accessCache(MethodInfo m) {
		return "access_cache("+getMethodId(m)+")";
	}
}
