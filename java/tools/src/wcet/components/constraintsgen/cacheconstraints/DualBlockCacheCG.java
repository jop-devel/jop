/**
 * 
 */
package wcet.components.constraintsgen.cacheconstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.components.constraintsgen.graphtracer.IGraphTracerClient;
import wcet.components.graphbuilder.blocks.BasicBlock;
import wcet.components.graphbuilder.blocks.InvokeBlock;
import wcet.components.graphbuilder.blocks.InvokeReturnBlock;
import wcet.framework.exceptions.InitException;
import wcet.framework.hierarchy.MethodKey;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.solver.IConstraint;
import wcet.framework.solver.BasicConstraint;
import wcet.framework.solver.BasicConstraintTerm;

/**
 * @author Elena Axamitova
 * @version 0.2 29.05.2007
 * 
 * Constraints generator simulating double block cache. It works with
 * ControlFlowGraphTracer.
 */
public class DualBlockCacheCG implements IAnalyserComponent, IGraphTracerClient {

    /**
         * Next tracer client in chain
         */
    private IGraphTracerClient nextClient;

    /**
         * Shared data store.
         */
    private IDataStore dataStore;

    /**
         * Method key(owner, name and descriptor) of the last invoked method
         */
    private MethodKey lastInvokedMethod;

    /**
         * <code>true</code> if the last block containing method information
         * was an InvokeBlock, <code>false</code> if it was a ReturnBlock.
         */
    private boolean lastWasInvoke;

    /**
         * Saves mapping of node id to the last invoked method before that node
         * (used for fork, join and loop controlers).
         */
    private HashMap<Integer, MethodKey> nodeIdToLastInvokeMap;

    /**
         * All generated constraints.
         */
    private ArrayList<IConstraint> cacheConstraints;

    /**
         * Control flow graph.
         */
    private IControlFlowGraph<BasicBlock> cfg;

    /**
         * Id of the loop controler whose loop path is currently being visited
         */
    private int inLoopOfNode;

    /**
         * Mapping of child loop controler ids (inner loop) to father loop
         * controler ids (outer loop).
         */
    private HashMap<Integer, Integer> childToFatherLCIdMap;

    /**
         * Mapping of loop controler id to the first invoked method key in its
         * loop path.
         */
    private HashMap<Integer, IVertex<BasicBlock>> lcIdToFirstInvokeMap;

    public DualBlockCacheCG(IDataStore ds) {
	this.dataStore = ds;
	this.cacheConstraints = new ArrayList<IConstraint>();
	this.nodeIdToLastInvokeMap = new HashMap<Integer, MethodKey>();
	this.childToFatherLCIdMap = new HashMap<Integer, Integer>();
	this.lcIdToFirstInvokeMap = new HashMap<Integer, IVertex<BasicBlock>>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return false;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	// called from graph tracer in chain
	return IGlobalComponentOrder.NOT_EXECUTED;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	this.nextClient = (IGraphTracerClient) this.dataStore
		.getObject(IConstraintsGeneratorConstants.LAST_TRACER_CLIENT);
	this.dataStore.storeObject(
		IConstraintsGeneratorConstants.LAST_TRACER_CLIENT, this);
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	// not executed, called from graph tracer
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endGraphVisit()
         */
    public void endGraphVisit() {
	this.createSpecialLoopConstraints();
	this.dataStore.addConstraints(this.cacheConstraints);
	if (this.nextClient != null) {
	    this.nextClient.endGraphVisit();
	}
    }

    /**
         * Creates special cache constraints if a single leaf method is invoked
         * in a loop - only in the first iteration the invoke results in a cache
         * miss, the rest are cache hits.
         */
    private void createSpecialLoopConstraints() {
	for (Iterator<Integer> iterator = this.lcIdToFirstInvokeMap.keySet()
		.iterator(); iterator.hasNext();) {
	    int lcId = iterator.next();
	    IVertex<BasicBlock> firstInvoke = this.lcIdToFirstInvokeMap
		    .get(lcId);
	    // if a loop controler id is not contained in the
                // lcIdToFirstInvokeMap,
	    // there are no invokes in its loop path(s). If the mapping is
	    // <code>null</null> either there is more than one invoke in any
                // of its
	    // loop paths, or the single invokes in its loop paths invoke
                // different
	    // methods.
	    if (firstInvoke != null) {
		// f<id> - invoke inflow - only one edge
		int inEdgeId = firstInvoke.getIncomingEdges().iterator().next();
		InvokeReturnBlock currBasicBlock = (InvokeReturnBlock) firstInvoke
			.getData();
		// create inflow divide constraint - f<id> = f<id>_hit +
		// f<id>_miss
		IConstraint inflowDivideConstraint = new BasicConstraint(
			IConstraint.EQUAL);
		inflowDivideConstraint.addLeftHandTerm(new BasicConstraintTerm(
			1, "f" + inEdgeId));
		inflowDivideConstraint
			.addRightHandTerm(new BasicConstraintTerm(
				1,
				"f"
					+ inEdgeId
					+ IConstraintsGeneratorConstants.CACHE_HIT_EDGE_SUFFIX));
		inflowDivideConstraint
			.addRightHandTerm(new BasicConstraintTerm(
				1,
				"f"
					+ inEdgeId
					+ IConstraintsGeneratorConstants.CACHE_MISS_EDGE_SUFFIX));
		this.cacheConstraints.add(inflowDivideConstraint);

		// create constraint for cache miss bound - f<id>_miss = loop
		// inflow
		IVertex<BasicBlock> loopControler = this.cfg
			.findVertexByIndex(lcId);
		IConstraint inflowLoopConstraint = new BasicConstraint(
			IConstraint.EQUAL);
		inflowLoopConstraint
			.addLeftHandTerm(new BasicConstraintTerm(
				1,
				"f"
					+ inEdgeId
					+ IConstraintsGeneratorConstants.CACHE_MISS_EDGE_SUFFIX));
		for (Iterator<Integer> i = loopControler.getInNotLoopEdges()
			.iterator(); i.hasNext();) {
		    inflowLoopConstraint
			    .addRightHandTerm(new BasicConstraintTerm(1, "f"
				    + i.next()));
		}
		this.cacheConstraints.add(inflowLoopConstraint);

		// create execution time constraint for the current block (with
		// incoming edges) - e.g. cache hit and cache miss edges
		IConstraint execTimeConstraint = new BasicConstraint(
			IConstraint.EQUAL);
		execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1,
			"t" + firstInvoke.getIndex()));
		execTimeConstraint
			.addRightHandTerm(new BasicConstraintTerm(
				currBasicBlock.getCacheHitExecTime(),
				"f"
					+ inEdgeId
					+ IConstraintsGeneratorConstants.CACHE_HIT_EDGE_SUFFIX));
		execTimeConstraint
			.addRightHandTerm(new BasicConstraintTerm(
				currBasicBlock.getCacheMissExecTime(),
				"f"
					+ inEdgeId
					+ IConstraintsGeneratorConstants.CACHE_MISS_EDGE_SUFFIX));
		this.cacheConstraints.add(execTimeConstraint);
	    }
	}

    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endLoopPath(int)
         */
    public void endLoopPath(int nodeId) {
	if (this.lcIdToFirstInvokeMap.containsKey(nodeId)) {
	    if (this.lcIdToFirstInvokeMap.get(nodeId) == null) {
		int fatherLCId = this.childToFatherLCIdMap.get(nodeId);
		IVertex<BasicBlock> firstFatherInvoke = this.lcIdToFirstInvokeMap
			.get(fatherLCId);
		if (firstFatherInvoke != null)
		    this.createCacheConstraints(firstFatherInvoke, false);
		this.lcIdToFirstInvokeMap.put(fatherLCId, null);
	    }
	}
	this.inLoopOfNode = this.childToFatherLCIdMap.get(nodeId);
	if (this.nextClient != null)
	    this.nextClient.endLoopPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endSimlePath(int)
         */
    public void endSimlePath(int nodeId) {
	if (this.nodeIdToLastInvokeMap.containsKey(nodeId)) {
	    MethodKey oldNodeInvoke = this.nodeIdToLastInvokeMap.get(nodeId);
	    if ((oldNodeInvoke != null)
		    && (!oldNodeInvoke.equals(this.lastInvokedMethod))) {
		this.nodeIdToLastInvokeMap.put(nodeId, null);
	    }
	} else {
	    this.nodeIdToLastInvokeMap.put(nodeId, this.lastInvokedMethod);
	}
	if (this.nextClient != null)
	    this.nextClient.endSimlePath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startGraphVisit()
         */
    @SuppressWarnings("unchecked")
    public void startGraphVisit() {
	this.cfg = (IControlFlowGraph<BasicBlock>) this.dataStore.getGraph();
	this.lastInvokedMethod = null;
	this.lastWasInvoke = true;
	this.inLoopOfNode = -1;
	this.childToFatherLCIdMap.clear();
	this.lcIdToFirstInvokeMap.clear();
	this.cacheConstraints.clear();
	this.nodeIdToLastInvokeMap.clear();
	if (this.nextClient != null)
	    this.nextClient.startGraphVisit();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startLoopPath(int)
         */
    public void startLoopPath(int nodeId) {
	if (nodeId != this.inLoopOfNode) {
	    this.childToFatherLCIdMap.put(nodeId, this.inLoopOfNode);
	    this.inLoopOfNode = nodeId;
	}
	if (this.nextClient != null)
	    this.nextClient.startLoopPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startSimplePath(int)
         */
    public void startSimplePath(int nodeId) {
	this.lastInvokedMethod = this.nodeIdToLastInvokeMap.get(nodeId);
	if (this.nextClient != null)
	    this.nextClient.startSimplePath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#visitNode(int,
         *      int, int)
         */
    public void visitNode(int edgeId, int nodeId, int nodeFlags) {
	IVertex<BasicBlock> currNode = this.cfg.findVertexByIndex(nodeId);
	if ((nodeFlags & IGraphTracerClient.INVOKE_NODE) != 0) {
	    InvokeBlock currMethInvoke = (InvokeBlock) currNode.getData();
	    MethodKey currInvokeKey = currMethInvoke.getMethodKey();
	    if (currInvokeKey.equals(this.lastInvokedMethod)) {
		this.createCacheConstraints(currNode, true);
	    } else {
		if (this.inLoopOfNode != -1) {
		    if (!this.lcIdToFirstInvokeMap
			    .containsKey(this.inLoopOfNode)) {
			this.lcIdToFirstInvokeMap.put(this.inLoopOfNode,
				currNode);
		    } else {
			IVertex<BasicBlock> oldInvokeNode = this.lcIdToFirstInvokeMap
				.get(this.inLoopOfNode);
			if (oldInvokeNode != null) {
			    this.createCacheConstraints(oldInvokeNode, false);
			    this.lcIdToFirstInvokeMap.put(this.inLoopOfNode,
				    null);
			}
			if ((nodeFlags & IGraphTracerClient.START_NODE) != 0)
			    this.createStartNodeCacheConstraint(currNode);
			else
			    this.createCacheConstraints(currNode, false);
		    }
		} else {
		    if ((nodeFlags & IGraphTracerClient.START_NODE) != 0)
			this.createStartNodeCacheConstraint(currNode);
		    else
			this.createCacheConstraints(currNode, false);
		}
		this.lastInvokedMethod = currInvokeKey;
		this.lastWasInvoke = true;
	    }
	} else if ((nodeFlags & IGraphTracerClient.RETURN_NODE) != 0) {
	    if (this.lastWasInvoke)
		this.createCacheConstraints(currNode, true);
	    else
		this.createCacheConstraints(currNode, false);
	    this.lastWasInvoke = false;
	}
	if ((nodeFlags & IGraphTracerClient.LOOP_CONTROLER_NODE) != 0) {
	    this.lastInvokedMethod = null;
	}
	if (this.nextClient != null)
	    this.nextClient.visitNode(edgeId, nodeId, nodeFlags);

    }

    /**
         * Creates execution time constraint for invoke and return nodes - cache
         * load time constraint. Uses incoming edges.
         * 
         * @param currNode -
         *                node object to create constraint for
         * @param cacheHit -
         *                <code>true</true> if guaranteed cache hit, <code>false</code>
     * otherwise
         */
    private void createCacheConstraints(IVertex<BasicBlock> currNode,
	    boolean cacheHit) {
	HashSet<Integer> inEdges = currNode.getIncomingEdges();
	InvokeReturnBlock currBasicBlock = (InvokeReturnBlock) currNode
		.getData();
	// compute method cache load penalty
	int cacheLoadPenalty;
	if (cacheHit)
	    cacheLoadPenalty = currBasicBlock.getCacheHitExecTime();
	else
	    cacheLoadPenalty = currBasicBlock.getCacheMissExecTime();
	// create execution time constraint for the current block (with
	// incoming edges)
	IConstraint execTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ currNode.getIndex()));
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    execTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		    cacheLoadPenalty, "f" + i.next()));
	}
	this.cacheConstraints.add(execTimeConstraint);
    }

    /**
         * Creates execution time constraint for a start node if it is invoke. A
         * start node is guaranteed miss. Requires special handling, since a
         * start node has no incoming edges.
         * 
         * @param currNode -
         *                start node object to create constraint for
         */
    private void createStartNodeCacheConstraint(IVertex<BasicBlock> currNode) {
	InvokeReturnBlock currBasicBlock = (InvokeReturnBlock) currNode
		.getData();
	IConstraint execTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ currNode.getIndex()));
	execTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		currBasicBlock.getCacheMissExecTime(),
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	this.cacheConstraints.add(execTimeConstraint);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#stillInLoop(int)
         */
    public void stillInLoop(int nodeId) {
	this.inLoopOfNode = nodeId;
	if (this.nextClient != null)
	    this.nextClient.stillInLoop(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endCatchPath(int)
         */
    public void endCatchPath(int nodeId) {
	if (this.nextClient != null)
	    this.nextClient.endCatchPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startCatchPath(int,
         *      int, int)
         */
    public void startCatchPath(int nodeId, int startEdgeId, int endEdgeId) {
	if (this.nextClient != null)
	    this.nextClient.startCatchPath(nodeId, startEdgeId, endEdgeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#stillInCatch(int)
         */
    public void stillInCatch(int nodeId) {
	if (this.nextClient != null)
	    this.nextClient.stillInCatch(nodeId);
    }

}
