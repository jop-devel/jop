/**
 * 
 */
package wcet.components.constraintsgen.flowconstraints;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.components.constraintsgen.graphtracer.IGraphTracerClient;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
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
 * A graph tracer client that creates flow constraints for all nodes.
 * 
 * @author Elena Axamitova
 * @version 0.1 11.05.2007
 */

public class FlowConstraintsGenerator implements IGraphTracerClient,
	IAnalyserComponent {
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
         * All generated execution time constraints - no invoke and return nodes
         * (inflow * execution time of block = exec time)
         */
    private ArrayList<IConstraint> exectimeConstraints;

    /**
         * Objective function of the problem - a sum of execution times of all
         * blocks to maximize.
         */
    private IConstraint objFunction;

    /**
         * A set of all vertixes without outgoing edges in the graph - require
         * special handling.
         */
    private HashSet<Integer> lastVertices;

    /**
         * Next tracer client in chain
         */
    private IGraphTracerClient nextClient;

    public FlowConstraintsGenerator(IDataStore ds) {
	this.dataStore = ds;
	this.flowConstraints = new ArrayList<IConstraint>();
	this.loopConstraints = new ArrayList<IConstraint>();
	this.exectimeConstraints = new ArrayList<IConstraint>();
	this.lastVertices = new HashSet<Integer>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endGraphVisit()
         */
    public void endGraphVisit() {
	this.endProcessing();
	// store all generated vertexes
	this.dataStore.addConstraints(this.flowConstraints);
	this.dataStore.addConstraints(this.loopConstraints);
	this.dataStore.addConstraints(this.exectimeConstraints);
	this.dataStore.storeObject(
		IConstraintsGeneratorConstants.OBJ_FUNCTION_KEY,
		this.objFunction);
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
	this.objFunction = new ObjectiveFunction(IConstraint.MAXIMIZE);
	// clear all
	this.resetConstraintLists();
	this.lastVertices.clear();
	this.graph = (IControlFlowGraph<IVertexData>) this.dataStore.getGraph();
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
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#stillInLoop(int)
         */
    public void stillInLoop(int nodeId) {
	if (this.nextClient != null)
	    this.nextClient.stillInLoop(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#visitNode(int,
         *      int, int)
         */
    public void visitNode(int edgeId, int nodeId, int nodeFlags) {
	IVertex<IVertexData> currNode = this.graph.findVertexByIndex(nodeId);
	if (currNode.isCatchHandler())
	    this.createCatchHandlerConstraints(currNode);

	// create flow constraint, special handling of start and end nodes
	if ((nodeFlags & IGraphTracerClient.START_NODE) != 0) {
	    this.createStartNodeFlowConstraint(currNode);
	    if ((nodeFlags & IGraphTracerClient.LOOP_CONTROLER_NODE) != 0)
		this.createStartNodeLoopConstraint(currNode);
	} else if ((nodeFlags & IGraphTracerClient.END_NODE) != 0) {
	    this.lastVertices.add(nodeId);
	} else {
	    this.createFlowConstraint(currNode);
	    // create loop bound constraint if needed.
	    if ((nodeFlags & IGraphTracerClient.LOOP_CONTROLER_NODE) != 0)
		this.createLoopConstraint(currNode);
	}
	// create exec time with inflow edges, special handling of invoke,
	// return and start nodes.
	if (!((nodeFlags & IGraphTracerClient.INVOKE_NODE) != 0)
		&& !((nodeFlags & IGraphTracerClient.RETURN_NODE) != 0)) {
	    if ((nodeFlags & IGraphTracerClient.START_NODE) != 0) {
		this.createStartNodeExecConstraint(currNode);
	    } else {
		this.createExecTimeConstraint(currNode);
	    }
	}
	// create the corresponing term in the objective function - for all
        // nodes
	this.createObjfunctionTerm(currNode);

	if (this.nextClient != null)
	    this.nextClient.visitNode(edgeId, nodeId, nodeFlags);
    }

    /**
         * Creates null constraints for catch handler inflow edges. In the
         * current implementation of jop, any exception thrown just stops the
         * engine. So any catch handler inflow edges have the execution
         * frequency 0.
         * 
         * @param currNode -
         *                a catch handler start node
         */
    protected void createCatchHandlerConstraints(IVertex<IVertexData> currNode) {
	// QUESTION should be enough - e.g catch in edges have frequency 0 -
	// since any
	// throw just stops the engine
	HashSet<Integer> inEdges = currNode.getIncomingEdges();
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    IConstraint catchConstraint = new BasicConstraint(IConstraint.EQUAL);
	    catchConstraint.addLeftHandTerm(new BasicConstraintTerm("f"
		    + i.next()));
	    catchConstraint.addRightHandTerm(new BasicConstraintTerm(0, null));
	    this.flowConstraints.add(catchConstraint);
	}
    }

    /**
         * Creates an objective function term for a given node (1*exec time).
         * 
         * @param currNode -
         *                a graph node
         */
    private void createObjfunctionTerm(IVertex<IVertexData> currNode) {
	this.objFunction.addRightHandTerm(new BasicConstraintTerm(1, "t"
		+ currNode.getIndex()));
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

    /**
         * Clear all previously generated constraints.
         */
    private void resetConstraintLists() {
	this.flowConstraints.clear();
	this.loopConstraints.clear();
	this.exectimeConstraints.clear();
    }

    /**
         * Creates a flow constraint for the given node (inflow = outflow).
         * 
         * @param currVertex -
         *                a node to generate constraint for
         */
    private void createFlowConstraint(IVertex<IVertexData> currVertex) {
	HashSet<Integer> outEdges = currVertex.getOutgoingEdges();

	// create flow constraint for the current block
	HashSet<Integer> inEdges = currVertex.getIncomingEdges();
	IConstraint flowConstraint = new BasicConstraint(IConstraint.EQUAL, "B"
		+ currVertex.getIndex());
	for (Iterator<Integer> i = outEdges.iterator(); i.hasNext();) {
	    flowConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	for (Iterator<Integer> i = inEdges.iterator(); i.hasNext();) {
	    flowConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	this.flowConstraints.add(flowConstraint);
    }

    /**
         * Creates a execution time constraint with inflow edges(inflow * exec
         * time block = exec time node). Not called for invoke and return nodes.
         * 
         * @param currVertex -
         *                a node to generate constraint for
         */
    private void createExecTimeConstraint(IVertex<IVertexData> currVertex) {
	HashSet<Integer> inEdges = currVertex.getIncomingEdges();
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
	// this.objFunction.addRightHandTerm(new BasicConstraintTerm(1, "t"
	// + currVertex.getIndex()));
    }

    /**
         * Creates a loop bound constraint for a given loop controler. ( inflow
         * loop controler* loop bound = inflow loop body)
         * 
         * @param loopControler
         */
    private void createLoopConstraint(IVertex<IVertexData> loopControler) {
	// create loop constraint if currBasicBlock is a loop controler
	IConstraint loopConstraint = new BasicConstraint(
		IConstraint.GREATEREQUAL);
	for (Iterator<Integer> i = loopControler.getEdgesToLoopBody()
		.iterator(); i.hasNext();)
	    loopConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	int loopCount = loopControler.getLoopCount();
	for (Iterator<Integer> i = loopControler.getInNotLoopEdges().iterator(); i
		.hasNext();) {
	    loopConstraint.addLeftHandTerm(new BasicConstraintTerm(loopCount,
		    "f" + i.next()));

	}
	this.loopConstraints.add(loopConstraint);

    }

    /**
         * Creates constraint for vertexes without outgoing edges - only one of
         * them is executed.
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
	}
	this.flowConstraints.add(lastConstraint);
    }

    /**
         * Special handling of a start node (no incomming edges) when creating a
         * flow constraint.
         * 
         * @param startNode -
         *                a start node
         */
    private void createStartNodeFlowConstraint(IVertex<IVertexData> startNode) {
	// add first constraint (analysied code is executed once), special since
	// one if the inflow
	// edges is the first edge
	IConstraint firstConstraint = new BasicConstraint(IConstraint.EQUAL);
	firstConstraint.addLeftHandTerm(new BasicConstraintTerm(
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	firstConstraint.addRightHandTerm(new BasicConstraintTerm(1, null));
	this.flowConstraints.add(firstConstraint);
	// create strart node flow constraint
	IConstraint rootFlowConstraint = new BasicConstraint(IConstraint.EQUAL);
	rootFlowConstraint.addLeftHandTerm(new BasicConstraintTerm(1,
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	for (Iterator<Integer> i = startNode.getOutgoingEdges().iterator(); i
		.hasNext();) {
	    rootFlowConstraint.addRightHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	}
	this.flowConstraints.add(rootFlowConstraint);
    }

    /**
         * Special handling of a start node (no incomming edges) when creating
         * an execution time constraint.
         * 
         * @param startNode -
         *                a start node
         */
    private void createStartNodeExecConstraint(IVertex<IVertexData> startNode) {
	// create start node exec time constraint, special since one if the
	// inflow
	// edges is the first edge
	IVertexData rootBasicBlock = startNode.getData();
	IConstraint rootTimeConstraint = new BasicConstraint(IConstraint.EQUAL);
	rootTimeConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "t"
		+ startNode.getIndex()));
	rootTimeConstraint.addRightHandTerm(new BasicConstraintTerm(
		rootBasicBlock.getValue(),
		IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	this.exectimeConstraints.add(rootTimeConstraint);
    }

    /**
         * Special handling of a start loop constroler node (no incomming edges)
         * when creating a loop constraint.
         * 
         * @param startNode -
         *                a start node
         */
    private void createStartNodeLoopConstraint(IVertex<IVertexData> startNode) {
	// create start node loop constraint, special since one if the inflow
	// edges is the first edge
	IConstraint rootLoopConstraint = new BasicConstraint(
		IConstraint.GREATEREQUAL);
	rootLoopConstraint
		.addRightHandTerm(new BasicConstraintTerm(startNode
			.getLoopCount(),
			IConstraintsGeneratorConstants.FIRST_EDGE_NAME));
	for (Iterator<Integer> i = startNode.getEdgesToLoopBody().iterator(); i
		.hasNext();)
	    rootLoopConstraint.addLeftHandTerm(new BasicConstraintTerm(1, "f"
		    + i.next()));
	this.loopConstraints.add(rootLoopConstraint);

    }

}
