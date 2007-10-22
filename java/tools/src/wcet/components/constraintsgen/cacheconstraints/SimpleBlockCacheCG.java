/**
 * 
 */
package wcet.components.constraintsgen.cacheconstraints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.components.constraintsgen.graphtracer.IGraphTracerClient;
import wcet.components.graphbuilder.blocks.BasicBlock;
import wcet.components.graphbuilder.blocks.InvokeReturnBlock;
import wcet.framework.exceptions.InitException;
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
 * @version 0.1 11.05.2007
 * 
 * Constraints generator simulating single block cache, that works with
 * ControlFlowGraphTracer. Every method cache access is a miss.
 */
public class SimpleBlockCacheCG implements IAnalyserComponent,
	IGraphTracerClient {
    /**
         * Shared data store.
         */
    private IDataStore dataStore;

    /**
         * Next tracer client in chain
         */
    private IGraphTracerClient nextClient;

    /**
         * All generated constraints.
         */
    private ArrayList<IConstraint> cacheConstraints;

    /**
         * Control flow graph.
         */
    private IControlFlowGraph<BasicBlock> cfg;

    public SimpleBlockCacheCG(IDataStore ds) {
	this.dataStore = ds;
	this.cacheConstraints = new ArrayList<IConstraint>();
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
	this.dataStore.addConstraints(this.cacheConstraints);
	if (this.nextClient != null) {
	    this.nextClient.endGraphVisit();
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endLoopPath(int)
         */
    public void endLoopPath(int nodeId) {
	if (this.nextClient != null)
	    this.nextClient.endLoopPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endSimlePath(int)
         */
    public void endSimlePath(int nodeId) {
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
	this.cacheConstraints.clear();
	this.cfg = (IControlFlowGraph<BasicBlock>) this.dataStore.getGraph();
	if (this.nextClient != null)
	    this.nextClient.startGraphVisit();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startLoopPath(int)
         */
    public void startLoopPath(int nodeId) {
	if (this.nextClient != null)
	    this.nextClient.startLoopPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startSimplePath(int)
         */
    public void startSimplePath(int nodeId) {
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
	if (((nodeFlags & IGraphTracerClient.INVOKE_NODE) != 0)
		|| ((nodeFlags & IGraphTracerClient.RETURN_NODE) != 0)) {
	    // if the current node accesses method cache, create a cache
                // miss
	    // constraint for it.
	    if ((nodeFlags & IGraphTracerClient.START_NODE) != 0)
		this.createStartNodeCacheConstraint(currNode);
	    else
		this.createCacheConstraints(currNode);
	}
	if (this.nextClient != null)
	    this.nextClient.visitNode(edgeId, nodeId, nodeFlags);
    }

    /**
         * Creates a cache constraint for a node with incoming edges.
         * 
         * @param currNode -
         *                node to create cnstraints for
         */
    private void createCacheConstraints(IVertex<BasicBlock> currNode) {
	HashSet<Integer> inEdges = currNode.getIncomingEdges();
	// create execution time constraint for the current block (with
	// incoming edges)
	InvokeReturnBlock currBasicBlock = (InvokeReturnBlock) currNode
		.getData();
	IConstraint execTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ currNode.getIndex()));
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    execTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		    currBasicBlock.getCacheMissExecTime(), "f" + i.next()));
	}
	this.cacheConstraints.add(execTimeConstraint);
    }

    /**
         * Creates a cache constraint for a start node (special handling since
         * it has no incoming edges).
         * 
         * @param currNode -
         *                start node to create cnstraints for
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
