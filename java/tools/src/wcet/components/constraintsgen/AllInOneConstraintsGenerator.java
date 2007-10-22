/**
 * 
 */
package wcet.components.constraintsgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.cfg.IVertexData;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.solver.IConstraint;
import wcet.framework.interfaces.solver.IConstraintTerm;
import wcet.framework.solver.BasicConstraint;
import wcet.framework.solver.BasicConstraintTerm;
import wcet.framework.solver.ObjectiveFunction;

/**
 * @author Elena Axamitova
 * @version 0.2 15.04.2007
 * 
 * Creates all constraints. Cache constraints simulate simple block cache.
 * It performs better that the combination ControlFlowGraphTracer + 
 * SimpleBlockCacheCG, since it implements much simple graph tracer strategy.
 */
public class AllInOneConstraintsGenerator implements IAnalyserComponent {
    /**
     * Shared data store.
     */
    private IDataStore dataStore;

    /**
     * Control flow graph
     */
    private IControlFlowGraph<IVertexData> graph;

    /**
     * All generated flow constraints (inflow==outflow).
     */
    private ArrayList<IConstraint> flowConstraints;

    /**
     * All generated loop constraints (inflow in loop controler * loop bound =
         * inflow in loop body)
     */
    private ArrayList<IConstraint> loopConstraints;

    /**
     * All generated execution time constraints (inflow * execution time of block = exec time)
     */
    private ArrayList<IConstraint> exectimeConstraints;

    /**
     * * Objective function of the problem - a sum of execution times of all
         * blocks to maximize.
     */
    private IConstraint objFunction;

    /**
     * Ids of vertexes to visit
     */
    private Queue<Integer> traverseQueue;

    /**
     * Already visited vertexes
     */
    private HashSet<Integer> processedVertices;

    /**
     * A set of all vertixes without outgoing edges in the graph - require
         * special handling.
     */
    private HashSet<Integer> lastVertices;

    public AllInOneConstraintsGenerator(IDataStore ds) {
	this.dataStore = ds;
	this.flowConstraints = new ArrayList<IConstraint>();
	this.loopConstraints = new ArrayList<IConstraint>();
	this.exectimeConstraints = new ArrayList<IConstraint>();
	this.traverseQueue = new LinkedList<Integer>();
	this.processedVertices = new HashSet<Integer>();
	this.lastVertices = new HashSet<Integer>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
         */
    public boolean getOnlyOne() {
	return true;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
         */
    public int getOrder() {
	return IGlobalComponentOrder.CONSTRAINS_GENERATOR
		+ IConstraintsGeneratorConstants.FLOW_CONSTRAINTS_GENERATOR_ORDER;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {

    }

    /**
     * Clear all previously generated constraints.
     */
    private void resetConstraintLists() {
	this.flowConstraints.clear();
	this.loopConstraints.clear();
	this.exectimeConstraints.clear();
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    @SuppressWarnings("unchecked")
    public String call() throws Exception {
	this.objFunction = (IConstraint) this.dataStore
		.getObject(IConstraintsGeneratorConstants.OBJ_FUNCTION_KEY);
	if (this.objFunction == null)
	    this.objFunction = new ObjectiveFunction(IConstraint.MAXIMIZE);
	// clear all
	this.resetConstraintLists();
	this.processedVertices.clear();
	this.lastVertices.clear();

	graph = this.dataStore.getGraph();
	//create root constraints, add all root's children to process queue
	this.startProcessing();
	while (!this.traverseQueue.isEmpty()) {
	    Integer currVertex = this.traverseQueue.poll();
	    //create constraints for the current node, add node's children to 
	    //the queue if nnot already processed
	    this.processVertex(currVertex);
	}
	//create constraints for all last vertexes (no outgoing edges)
	this.endProcessing();
	
	//handle catch parts of the graph
	this.cleanUpGraph();

	//save all generated constraints in data store
	this.dataStore.addConstraint(this.objFunction);
	this.dataStore.addConstraints(this.flowConstraints);
	this.dataStore.addConstraints(this.loopConstraints);
	this.dataStore.addConstraints(this.exectimeConstraints);
	return "+++Flow constraints generated.+++\n";
    }

    /**
     * Handle catch parts of the graph - set flow of an
     * exception handler edge to 0.
     */
    @SuppressWarnings("unchecked")
    private void cleanUpGraph() {
	// in current impementation it cleans catch-parts of a graph
	for (Iterator<IEdge> edgeIterator = this.graph.getAllEdges()
		.iterator(); edgeIterator.hasNext();) {
	    IEdge currEdge = edgeIterator.next();
	    // since the current catch implementation just stops the engine,
	    // no exception edges are followed
	    if(currEdge.isExceptionEdge()){
		IConstraint nullConstraint = new BasicConstraint(
			    IConstraint.EQUAL);
		    nullConstraint.addLeftHandTerm(new BasicConstraintTerm("f"
			    + currEdge.getIndex()));
		    nullConstraint.addRightHandTerm(new BasicConstraintTerm(0,
			    null));
		    this.flowConstraints.add(nullConstraint);
	    }
	    
	}

    }

    /**
     * Generates root constraints and adds root's children t the process queue.
     */
    private void startProcessing() {
	// add first constraint (analysied code is executed once)
	IConstraint firstConstraint = new BasicConstraint(IConstraint.EQUAL);
	firstConstraint.addLeftHandTerm(new BasicConstraintTerm(
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	firstConstraint.addRightHandTerm(new BasicConstraintTerm(1, null));
	this.flowConstraints.add(firstConstraint);
	// create root flow constraint
	IVertex<IVertexData> root = graph.getRoot();
	IConstraint rootFlowConstraint = new BasicConstraint(IConstraint.EQUAL);
	rootFlowConstraint.addLeftHandTerm(new BasicConstraintTerm(1,
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	for (Iterator<Integer> i = root.getOutgoingEdges().iterator(); i
		.hasNext();) {
	    rootFlowConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	this.flowConstraints.add(rootFlowConstraint);

	// create exec time rootConstraint
	IVertexData rootBasicBlock = root.getData();
	IConstraint rootTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	rootTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ root.getIndex()));
	rootTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		rootBasicBlock.getValue(),
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	this.exectimeConstraints.add(rootTimeConstraint);
	this.objFunction.addRightHandTerm(new BasicConstraintTerm(1, "t"
		+ root.getIndex()));

	// create loop constraint for root if needed
	if (root.isLoopControler()) {
	    IConstraint rootLoopConstraint = new BasicConstraint(
		    IConstraint.GREATEREQUAL);
	    rootLoopConstraint.addRightHandTerm(new BasicConstraintTerm(root
		    .getLoopCount(), IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	    for (Iterator<Integer> i = root.getEdgesToLoopBody().iterator(); i
		    .hasNext();)
		rootLoopConstraint.addLeftHandTerm(new BasicConstraintTerm(1,
			"f" + i.next()));
	    this.loopConstraints.add(rootLoopConstraint);
	}
	// add all root's children to the queue
	for (Iterator<Integer> i = root.getOutgoingEdges().iterator(); i
		.hasNext();) {
	    IEdge currEdge = this.graph.findEdgeByIndex(i.next());
	    this.traverseQueue.add(currEdge.getToVertex());
	}

	this.processedVertices.add(root.getIndex());

    }

    /**
     * Creates constraints for end vertexes - without outgoing edges.
     */
    private void endProcessing() {
	Iterator<Integer> iterator = this.lastVertices.iterator();
	int idx = 0;
	// since the analysied code is executed once, only one of the last
	// vertices is executed.
	IConstraint lastConstraint = new BasicConstraint(IConstraint.EQUAL);
	lastConstraint.addRightHandTerm(new BasicConstraintTerm(1, null));

	while (iterator.hasNext()) {
	    IVertex<IVertexData> currVertex = this.graph
		    .findVertexByIndex(iterator.next());
	    // create flow constraint
	    HashSet<Integer> inEdges = currVertex.getIncomingEdges();
	    IConstraint flowConstraint = new BasicConstraint(IConstraint.EQUAL);
	    IConstraintTerm lastEdgeTerm = new BasicConstraintTerm(1,
		    IConstraintsGeneratorConstants.LAST_EDGE_NAME + (idx++));
	    flowConstraint.addLeftHandTerm(lastEdgeTerm);
	    for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
		flowConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
			+ i.next()));
	    }
	    // add the last edge to the last term
	    lastConstraint.addLeftHandTerm(lastEdgeTerm);
	    this.flowConstraints.add(flowConstraint);
	    // create execution time constraint for the current last block
	    // (with incoming edges)
	    IVertexData currBasicBlock = currVertex.getData();

	    IConstraint execTimeConstraint = new BasicConstraint(
		    IConstraint.EQUAL);
	    execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		    + currVertex.getIndex()));
	    for (Iterator<Integer> i = currVertex.getIncomingEdges().iterator(); i
		    .hasNext();) {
		execTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
			currBasicBlock.getValue(), "f" + i.next()));
	    }

	    this.exectimeConstraints.add(execTimeConstraint);
	    this.objFunction.addRightHandTerm(new BasicConstraintTerm(1, "t"
		    + currVertex.getIndex()));

	    // last block cannot be a loop controler, has no outgoing edges
	}

	this.flowConstraints.add(lastConstraint);
    }

    /**
     * Triggers constraints generation for the vertex with id currVertexId and adds
     * all its children that have not yet been handled to the process queue.
     * @param currVertexId - id of the vertex
     */
    private void processVertex(int currVertexId) {
	IVertex<IVertexData> currVertex = this.graph
		.findVertexByIndex(currVertexId);
	if (!this.processedVertices.contains(currVertexId)) {
	    if (currVertex.getOutgoingEdges().size() == 0) {
		this.lastVertices.add(currVertex.getIndex());
	    } else {
		this.createVertexConstraints(currVertex);
		// add all reachable, not yet processed vertices to the queue
		Iterator<Integer> outEdges = currVertex.getOutgoingEdges()
			.iterator();
		while (outEdges.hasNext()) {
		    int currOutEdgeId = outEdges.next();
		    int nextVertex = this.graph.findEdgeByIndex(currOutEdgeId)
			    .getToVertex();
		    if (!this.processedVertices.contains(nextVertex)) {
			this.traverseQueue.add(nextVertex);
		    }
		}
	    }
	}
	this.processedVertices.add(currVertexId);
    }

    /**
     * Creates all constraints for a given vertex.
     * @param currVertex - the id of the vertex
     */
    private void createVertexConstraints(IVertex<IVertexData> currVertex) {
	HashSet<Integer> outEdges = currVertex.getOutgoingEdges();

	// create flow constraint for the current block
	HashSet<Integer> inEdges = currVertex.getIncomingEdges();
	IConstraint flowConstraint = new BasicConstraint(IConstraint.EQUAL);
	for (Iterator<Integer> i = outEdges.iterator(); i.hasNext();) {
	    flowConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    flowConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	this.flowConstraints.add(flowConstraint);

	// create execution time constraint for the current block (with
	// incoming edges)
	IVertexData currBasicBlock = currVertex.getData();

	IConstraint execTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	execTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ currVertex.getIndex()));
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    execTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		    currBasicBlock.getValue(), "f" + i.next()));
	}
	this.exectimeConstraints.add(execTimeConstraint);
	this.objFunction.addRightHandTerm(new BasicConstraintTerm(1, "t"
		+ currVertex.getIndex()));

	// create loop constraint if currBasicBlock is a loop controler
	if (currVertex.isLoopControler()) {
	    IConstraint loopConstraint = new BasicConstraint(
		    IConstraint.GREATEREQUAL);
	    for (Iterator<Integer> i = currVertex.getEdgesToLoopBody()
		    .iterator(); i.hasNext();)
		loopConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
			+ i.next()));
	    int loopCount = currVertex.getLoopCount();
	    for (Iterator<Integer> i = currVertex.getInNotLoopEdges()
		    .iterator(); i.hasNext();) {
		loopConstraint.addLeftHandTerm(new BasicConstraintTerm(
			loopCount, "f" + i.next()));

	    }
	    this.loopConstraints.add(loopConstraint);
	}

    }

}
