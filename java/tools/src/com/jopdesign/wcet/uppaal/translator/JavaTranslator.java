package com.jopdesign.wcet.uppaal.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.CallGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.graphutils.TopOrder.BadGraphException;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.translator.cache.CacheSimBuilder;

/** Translate Java threads, either using one process per methods or 
 *  a single process
 */
public abstract class JavaTranslator {
	/* map methods to node automatons */
	protected Map<MethodInfo,SubAutomaton> methodAutomata;
	protected List<MethodInfo> methodInfos;
	protected UppAalConfig config;
	protected Project project;
	protected MethodInfo root;
	protected SystemBuilder systemBuilder;
	protected CacheSimBuilder cacheSim;
	protected HashMap<MethodInfo, Integer> methodIDs;
	
	public Project getProject() {
		return project;
	}
	public UppAalConfig getConfig() {
		return config;
	}
	public List<MethodInfo> getMethodInfos() {
		return methodInfos;
	}
	public int getMethodID(MethodInfo m) {
		return methodIDs.get(m);
	}
	public CacheSimBuilder getCacheSim() {
		return cacheSim;
	}
	public void addMethodAutomaton(MethodInfo mi, SubAutomaton auto) {
		this.methodAutomata.put(mi,auto);
	}
	public SubAutomaton getMethodAutomaton(MethodInfo mi) {
		return methodAutomata.get(mi);
	}
	public JavaTranslator(UppAalConfig c, Project p, MethodInfo root) {
		this.config = c;
		this.project = p;
		this.root = root;
		/* Get callgraph */
		CallGraph callGraph = project.getCallGraph();
		// logger.info("Call stack depth: "+callGraph.getMaxHeight());
		this.methodInfos = callGraph.getReachableImplementations(root);
		if(! methodInfos.get(0).equals(root)) {
			throw new AssertionError("Bad callgraph: reachable implementations needs to return root as first element");
		}
		/* Create system builder */
		systemBuilder = new SystemBuilder(config,project,callGraph.getMaxHeight() + 1,methodInfos);
		this.methodIDs = new HashMap<MethodInfo, Integer>();
		for(int i = 0; i < methodInfos.size(); i++) {
			methodIDs.put(methodInfos.get(i), i);
		}
		this.methodAutomata = new HashMap<MethodInfo, SubAutomaton>();
		/* Cache sim */
		this.cacheSim = systemBuilder.getCacheSim();
	}
	protected abstract void translate();
	protected void translateMethod(TemplateBuilder tb,
								   SubAutomaton methodAutomaton,
								   int mId,
								   MethodInfo mi,
								   InvokeBuilder invokeBuilder) {
		/* get flow graph */
		ControlFlowGraph cfg = project.getFlowGraph(mi);
		/* insert summary nodes if request */
		if(config.collapseLeaves) {
			try {
				cfg.insertSummaryNodes();
			} catch (BadGraphException e) {
				throw new AssertionError("Faild to insert summary nodes: "+e);
			}			
		}
		MethodBuilder mBuilder = 
			new MethodBuilder(config, project, tb, invokeBuilder, cacheSim, 
				                               methodAutomaton, mId, cfg);
		mBuilder.build();
	}
	public SystemBuilder getSystem() {
		translate();
		return this.systemBuilder;
	}
	
}
