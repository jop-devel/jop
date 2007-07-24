/**
 * 
 */
package wcet.components.constraintsgen.graphtracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.components.graphbuilder.blocks.BasicBlock;
import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskExecutionException;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;

/**
 * @author Elena Axamitova
 * @version 0.1 28.04.2007
 * 
 * Simulates execution flow on control flow graph. Visit of any part of the control 
 * flow graph is started only when all part that can be executed before it have been 
 * already processed.
 */
//TODO wrong visiting order of catch handlers possible - change.
public class ControlFlowGraphTracer implements IAnalyserComponent {
    /**
     * Shared data store.
     */
    private IDataStore dataStore;

    /**
     * Control flow graph
     */
    private IControlFlowGraph<BasicBlock> cfg;

    /**
     * Stack containing edges that are start edges of simple paths waiting
     * to be procesed.
     */
    private Stack<Integer> simplePathStack;

    /**
     * List containing all visited edges. Used to check if all incomming 
     * edges of a join node or a loop controler were already processed.
     */
    private ArrayList<Integer> visitedEdges;

    /**
     * List containing all visited vertexes. Needed to ensure that visitNode(.)
     * is called only once for each vertex.
     */
    private ArrayList<Integer> visitedVertexes;

    /**
     * Map containing sets of all vertexes within a loop for loop controlers.
     * Since the identification of loop vertexes is a quite constly process, saving
     * the results trades memory for time.
     */
    private HashMap<Integer, HashSet<Integer>> lcIdloopVertexesMap;

    /**
     * Contains all already visited loop controlers. 
     */
    private HashSet<Integer> visitedLoopControlers;

    /**
     * Map containing mappings of loop controler ids to sets of all edges 
     * that leave its loop (regular out loop edges and all breaks) 
     */
    private HashMap<Integer, HashSet<Integer>> lcIdToOutEdgesMap;

    /**
     * Next tracer client in chain
     */
    private IGraphTracerClient client;

    public ControlFlowGraphTracer(IDataStore ds) {
	this.dataStore = ds;
	this.simplePathStack = new Stack<Integer>();
	this.visitedEdges = new ArrayList<Integer>();
	this.visitedVertexes = new ArrayList<Integer>();
	this.lcIdloopVertexesMap = new HashMap<Integer, HashSet<Integer>>();
	this.lcIdToOutEdgesMap = new HashMap<Integer, HashSet<Integer>>();
	this.visitedLoopControlers = new HashSet<Integer>();
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
     */
    public boolean getOnlyOne() {
	return true;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
     */
    public int getOrder() {
	return IGlobalComponentOrder.CONSTRAINS_GENERATOR
		+ IConstraintsGeneratorConstants.CONTROL_FLOW_GRAPH_TRACER_ORDER;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
     */
    public void init() throws InitException {
	//empty
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    public String call() throws Exception {
	this.cfg = this.dataStore.getGraph();
	//retrieve the last client in chain
	this.client = (IGraphTracerClient) this.dataStore
		.getObject(IConstraintsGeneratorConstants.LAST_TRACER_CLIENT);
	if (this.client == null) {
	    //no reason to do such a hard work
	    throw new TaskExecutionException(
		    "GraphTracer: No client registred.");
	}
	this.client.startGraphVisit();

	this.visitGraph();

	this.client.endGraphVisit();
	
	//clear all
	this.visitedEdges.clear();
	this.visitedVertexes.clear();
	this.lcIdloopVertexesMap.clear();
	this.visitedLoopControlers.clear();

	return "### Graph tracer ended. ###";
    }

    /**
     * Visits graph.
     */
    private void visitGraph() {
	IVertex<BasicBlock> root = this.cfg.getRoot();
	this.client.visitNode(-1, root.getIndex(), this.getVertexFlags(root));
	this.visitedVertexes.add(root.getIndex());
	this.simplePathStack.addAll(root.getOutgoingEdges());
	IEdge currInEdge;
	while (!this.simplePathStack.isEmpty()) {
	    currInEdge = this.cfg.findEdgeByIndex(this.simplePathStack.pop());
	    this.handleEdge(currInEdge, this.simplePathStack);
	}
    }

    /**
     * Handle the current edge. If it is a start edge of a simple 
     * path follow it to its end and if all inflow to the end vertex has been 
     * handled, add all its outgoing edges to the given stack. 
     * If its a start edge of a not yet started loop path, call handleLoopPath(.).
     * If its an edge to a catch handler start, call handleExceptionEdge(.).
     * 
     * @param currInEdge - edge to handle
     * @param stack - stack to add edges that follow if the flow permits
     */
    private void handleEdge(IEdge currInEdge, Stack<Integer> stack) {
	IVertex<BasicBlock> currFromVertex = this.cfg
		.findVertexByIndex(currInEdge.getFromVertex());
	IVertex<BasicBlock> currToVertex = this.cfg
		.findVertexByIndex(currInEdge.getToVertex());
	//first edge to the loop body - start loop path
	if (currFromVertex.isLoopControler()
		//every edge from a loop controler to loop body is added twice
		//first time to start the loop path and second time to start 
		//the simple path - here only the first time processed
		&& !this.visitedEdges.contains(currInEdge.getIndex())
		&& (currFromVertex.getEdgesToLoopBody().contains(currInEdge
			.getIndex()))) {
	    this.handleLoopPath(currInEdge);
	    //if all (max 2) loop paths visited (loop inflow handled before), 
	    //add all loop outgoing edges to the stack
	    if (this.visitedEdges
		    .containsAll(currFromVertex.getIncomingEdges()))
		stack.addAll(this.lcIdToOutEdgesMap.get(currFromVertex
			.getIndex()));
	} else {
	    if (currToVertex.isCatchHandler()) {
		if (!this.visitedVertexes.contains(currToVertex.getIndex())) {
		    this.handleExceptionPath(currToVertex, stack);
		}
	    } else {
		this.visitedEdges.add(currInEdge.getIndex());
		this.client.startSimplePath(currInEdge.getFromVertex());
		HashSet<Integer> inEdges = currToVertex.getIncomingEdges();
		HashSet<Integer> outEdges = currToVertex.getOutgoingEdges();
		//visit all simple nodes (1 edge in and 1 edge out) in row
		while ((inEdges.size() == 1) && (outEdges.size() == 1)) {
		    this.client.visitNode(currInEdge.getIndex(), currToVertex
			    .getIndex(), this.getVertexFlags(currToVertex));
		    this.visitedVertexes.add(currToVertex.getIndex());
		    currFromVertex = currToVertex;
		    currInEdge = this.cfg.findEdgeByIndex(outEdges.iterator()
			    .next());
		    this.visitedEdges.add(currInEdge.getIndex());
		    //update data
		    currToVertex = this.cfg.findVertexByIndex(currInEdge
			    .getToVertex());
		    inEdges = currToVertex.getIncomingEdges();
		    outEdges = currToVertex.getOutgoingEdges();
		}
		if (!this.visitedVertexes.contains(currToVertex.getIndex())) {
		    this.client.visitNode(currInEdge.getIndex(), currToVertex
			    .getIndex(), this.getVertexFlags(currToVertex));
		    this.visitedVertexes.add(currToVertex.getIndex());
		}
		this.client.endSimlePath(currToVertex.getIndex());
		if (currInEdge.isExceptionEdge()
			&& (!this.onlyExceptionEdges(inEdges)))
		    this.client.endCatchPath(currToVertex.getIndex());
		if (currToVertex.isLoopControler()) {
		    //check if all inflow in a loop controller already handled
		    if (this.visitedEdges.containsAll(currToVertex
			    .getInNotLoopEdges()))
			//nested loop
			if (!this.visitedLoopControlers.contains(currToVertex
				.getIndex())) {
			    stack.addAll(currToVertex.getEdgesToLoopBody());
			    this.visitedLoopControlers.add(currToVertex
				    .getIndex());
			} else {
			    this.client.endLoopPath(currInEdge.getToVertex());
			}

		} else {
		    //check if all inflow in a join node handled
		    if (this.visitedEdges.containsAll(currToVertex
			    .getIncomingEdges())) {
			stack.addAll(currToVertex.getOutgoingEdges());
		    }
		}

	    }
	}
    }

    /**
     * Process loop path.
     * @param currInEdge - first edge to loop body
     */
    @SuppressWarnings("unchecked")
    private void handleLoopPath(IEdge currInEdge) {
	IVertex<BasicBlock> currLoopControler = this.cfg
		.findVertexByIndex(currInEdge.getFromVertex());

	HashSet<Integer> outLoopEdges = (HashSet<Integer>) currLoopControler
		.getOutgoingEdges().clone();
	outLoopEdges.removeAll(currLoopControler.getEdgesToLoopBody());
	
	//find all vertexes in this loop
	if (!this.lcIdloopVertexesMap.containsKey(currLoopControler.getIndex()))
	    this.identifyLoopVertexes(currLoopControler);
	HashSet<Integer> loopVertexes = this.lcIdloopVertexesMap
		.get(currLoopControler.getIndex());

	Stack<Integer> loopPathStack = new Stack<Integer>();
	loopPathStack.add(currInEdge.getIndex());
	this.client.startLoopPath(currLoopControler.getIndex());
	this.visitedEdges.add(currInEdge.getIndex());
	while (!loopPathStack.isEmpty()) {
	    currInEdge = this.cfg.findEdgeByIndex(loopPathStack.pop());
	    if (currLoopControler.getIndex() == currInEdge.getToVertex()) {
//		at loop end, but still in - loop end will be handled in handleEdge(.)
		this.client.stillInLoop(currLoopControler.getIndex());
		this.handleEdge(currInEdge, loopPathStack);
	    } else if (loopVertexes.contains(currInEdge.getToVertex())) {
		// in loop 
		this.client.stillInLoop(currLoopControler.getIndex());
		this.handleEdge(currInEdge, loopPathStack);
	    } else {
		//cur edge leaves the loop
		outLoopEdges.add(currInEdge.getIndex());
	    }
	}
	//remember all edges that leave the loop
	if (this.lcIdToOutEdgesMap.containsKey(currLoopControler.getIndex())) {
	    HashSet<Integer> oldOutEdges = this.lcIdToOutEdgesMap
		    .get(currLoopControler.getIndex());
	    outLoopEdges.addAll(oldOutEdges);
	}
	this.lcIdToOutEdgesMap.put(currLoopControler.getIndex(), outLoopEdges);
    }

    /**
     * Process a catch handle.
     * @param catchHandler - the catch handler vertex
     * @param fatherStack - stack in which to add normal(not exception handler) edges
     */
    @SuppressWarnings("unchecked")
    private void handleExceptionPath(IVertex catchHandler,
	    Stack<Integer> fatherStack) {
	//first find out which edge indicates start of the catch scope and which its end
	Iterator<Integer> inEdgesIterator;
	if (catchHandler.isLoopControler())
	    inEdgesIterator = catchHandler.getInNotLoopEdges().iterator();
	else
	    inEdgesIterator = catchHandler.getIncomingEdges().iterator();
	int firstCatchEdgeId = inEdgesIterator.next();
	int secondCatchEdgeId = inEdgesIterator.next();
	this.visitedEdges.add(firstCatchEdgeId);
	this.visitedEdges.add(secondCatchEdgeId);
	IEdge firstCatchEdge = cfg.findEdgeByIndex(firstCatchEdgeId);
	IEdge secondCatchEdge = cfg.findEdgeByIndex(secondCatchEdgeId);
	int firstFromVertexId = this.visitedEdges.indexOf(firstCatchEdge
		.getFromVertex());
	if (firstFromVertexId == -1)
	    firstFromVertexId = Integer.MAX_VALUE;
	int secondFromVertexId = this.visitedEdges.indexOf(secondCatchEdge
		.getFromVertex());
	if (secondFromVertexId == -1)
	    secondFromVertexId = Integer.MAX_VALUE;
	//now visit the catch start node if needed and call startCatchPath(.) 
	//on the client with correct arguments
	if (firstFromVertexId < secondFromVertexId) {
	    if (!this.visitedVertexes.contains(catchHandler.getIndex())) {
		this.client.visitNode(firstCatchEdgeId,
			catchHandler.getIndex(), this
				.getVertexFlags(catchHandler));
		this.visitedVertexes.add(catchHandler.getIndex());
	    }
	    this.client.startCatchPath(catchHandler.getIndex(),
		    firstCatchEdgeId, secondCatchEdgeId);
	} else {
	    if (!this.visitedVertexes.contains(catchHandler.getIndex())) {
		this.client.visitNode(secondCatchEdgeId, catchHandler
			.getIndex(), this.getVertexFlags(catchHandler));
		this.visitedVertexes.add(catchHandler.getIndex());
	    }
	    this.client.startCatchPath(catchHandler.getIndex(),
		    secondCatchEdgeId, firstCatchEdgeId);
	}
	//visit the rest of the catch path
	Stack<Integer> exceptionEdgesStack = new Stack<Integer>();
	if (catchHandler.isLoopControler())
	    exceptionEdgesStack.addAll(catchHandler.getEdgesToLoopBody());
	else
	    exceptionEdgesStack.addAll(catchHandler.getOutgoingEdges());
	while (!exceptionEdgesStack.isEmpty()) {
	    IEdge currInEdge = this.cfg.findEdgeByIndex(exceptionEdgesStack
		    .pop());
	    if (currInEdge.isExceptionEdge()) {
		//still in catch handler
		IVertex<BasicBlock> currToVertex = cfg
			.findVertexByIndex(currInEdge.getToVertex());
		if (this.onlyExceptionEdges(currToVertex.getIncomingEdges())) {
		    this.handleEdge(currInEdge, exceptionEdgesStack);
		} else {
		    this.handleEdge(currInEdge, exceptionEdgesStack);
		    // check if any of the edges in stack will be processed here
		    if (!this.noExceptionEdges(exceptionEdgesStack))
			this.client.stillInCatch(catchHandler.getIndex());
		}
	    } else {
		fatherStack.add(currInEdge.getIndex());
	    }
	}
    }

    /**
     * Find all vertexes that belong to the loop of this loop controler.
     * @param currLoopControler - a loop controler
     */
    @SuppressWarnings("unchecked")
    private void identifyLoopVertexes(IVertex<BasicBlock> currLoopControler) {
	HashSet<Integer> loopVertexes = new HashSet<Integer>();
	Queue<Integer> vertexesToVisit = new LinkedList<Integer>();
	HashSet<Integer> inLoopEdges = (HashSet<Integer>) currLoopControler
		.getIncomingEdges().clone();
	inLoopEdges.removeAll(currLoopControler.getInNotLoopEdges());
	//follow all incomming edges from within the loop backwards and remember
	//all found vertexes
	for (Iterator<Integer> iterator = inLoopEdges.iterator(); iterator
		.hasNext();) {
	    int currLCInEdgeId = iterator.next();
	    IEdge currLCInEdge = this.cfg.findEdgeByIndex(currLCInEdgeId);
	    int currLCInEdgeFromVertex = currLCInEdge.getFromVertex();
	    if (currLCInEdgeFromVertex != currLoopControler.getIndex())
		vertexesToVisit.add(currLCInEdge.getFromVertex());
	    while (!vertexesToVisit.isEmpty()) {
		int currVertexId = vertexesToVisit.poll();
		IVertex currVertex = cfg.findVertexByIndex(currVertexId);
		for (Iterator<Integer> iterator2 = currVertex
			.getIncomingEdges().iterator(); iterator2.hasNext();) {
		    IEdge currEdge = cfg.findEdgeByIndex(iterator2.next());
		    int currEdgeFromVertexId = currEdge.getFromVertex();
		    if (currEdgeFromVertexId != currLoopControler.getIndex()) {
			if (!loopVertexes.contains(currEdgeFromVertexId))
			    vertexesToVisit.add(currEdgeFromVertexId);
		    }
		}
		loopVertexes.add(currVertexId);
	    }

	}
	this.lcIdloopVertexesMap
		.put(currLoopControler.getIndex(), loopVertexes);
    }

    /**
     * Determine node flags.
     * @param currNode - the node which type(s) to compute
     * @return integer to be interpreted bitwise, each bit 
     * signifies one node type
     */
    private int getVertexFlags(IVertex<BasicBlock> currNode) {
	int result = IGraphTracerClient.SIMPLE_NODE;
	int CurrDataType = currNode.getData().getType();

	if (CurrDataType == BasicBlock.INVOKE_BB) {
	    result += IGraphTracerClient.INVOKE_NODE;
	} else if (CurrDataType == BasicBlock.RETURN_BB) {
	    result += IGraphTracerClient.RETURN_NODE;
	}
	if (currNode.getIncomingEdges().size() == 0) {
	    result += IGraphTracerClient.START_NODE;
	}
	if (currNode.getOutgoingEdges().size() == 0) {
	    result += IGraphTracerClient.END_NODE;
	}
	if (currNode.isLoopControler()) {
	    result += IGraphTracerClient.LOOP_CONTROLER_NODE;
	    if (currNode.getInNotLoopEdges().size() > 1) {
		result += IGraphTracerClient.JOIN_NODE;
	    }
	    if (currNode.getEdgesToLoopBody().size() > 1) {
		result += IGraphTracerClient.FORK_NODE;
	    }
	} else {
	    if (currNode.getIncomingEdges().size() > 1) {
		result += IGraphTracerClient.JOIN_NODE;
	    }
	    if (currNode.getOutgoingEdges().size() > 1) {
		result += IGraphTracerClient.FORK_NODE;
	    }
	}
	
          if (currNode.isCatchHandler()) { result +=
          IGraphTracerClient.CATCH_HANDLER_NODE; }
         

	return result;
    }

    /**
     * Checks if the collection containts only exception edges. Quite time 
     * consuming.
     * @param inEdges - collection of edge ids
     * @return <code>true</code> only exception edges in, <code>false</code>otherwise
     */
    private boolean onlyExceptionEdges(Collection<Integer> inEdges) {
	for (Iterator<Integer> iterator = inEdges.iterator(); iterator
		.hasNext();) {
	    int currEdgeId = iterator.next();
	    IEdge currEdge = this.cfg.findEdgeByIndex(currEdgeId);
	    if (!currEdge.isExceptionEdge()) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Checks if the collection containts only non-exception edges. Quite time 
     * consuming.
     * @param inEdges - collection of edge ids
     * @return <code>true</code> only exception edges in, <code>false</code>otherwise
     */
    private boolean noExceptionEdges(Collection<Integer> inEdges) {
	for (Iterator<Integer> iterator = inEdges.iterator(); iterator
		.hasNext();) {
	    int currEdgeId = iterator.next();
	    IEdge currEdge = this.cfg.findEdgeByIndex(currEdgeId);
	    if (currEdge.isExceptionEdge()) {
		return false;
	    }
	}
	return true;
    }
}
