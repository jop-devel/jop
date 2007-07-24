/**
 * 
 */
package wcet.components.constraintsgen.flowconstraints;

import wcet.components.constraintsgen.IConstraintsGeneratorConstants;
import wcet.components.constraintsgen.graphtracer.IGraphTracerClient;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;

/**
 * Writes messages to a standard error stream during the graph trace. Used for testing
 * only. 
 * @author Elena Axamitova
 * @version 0.1 22.05.2007
 */
public class TestTracerClient implements IGraphTracerClient, IAnalyserComponent {

    private IDataStore dataStore;

    private IGraphTracerClient nextClient;

    public TestTracerClient(IDataStore ds) {
	this.dataStore = ds;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endGraphVisit()
         */
    public void endGraphVisit() {
	System.err.println("End graph visit.");
	if (this.nextClient != null)
	    this.nextClient.endGraphVisit();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endLoopPath(int)
         */
    public void endLoopPath(int nodeId) {
	System.err.println("End loop path of node: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.endLoopPath(nodeId);
    }

    /* (non-Javadoc)
     * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#stillInLoop(int)
     */
    public void stillInLoop(int nodeId) {
	System.err.println("Still in loop: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.stillInLoop(nodeId);
    }

    
    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#endSimlePath(int)
         */
    public void endSimlePath(int nodeId) {
	System.err.println("End simple path of node: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.endSimlePath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startGraphVisit()
         */
    public void startGraphVisit() {
	System.err.println("Start graph visit.");
	if (this.nextClient != null)
	    this.nextClient.startGraphVisit();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startLoopPath(int)
         */
    public void startLoopPath(int nodeId) {
	System.err.println("Start loop path of node: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.startLoopPath(nodeId);
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.components.constraintsgen.graphtracer.IGraphTracerClient#startSimplePath(int)
         */
    public void startSimplePath(int nodeId) {
	System.err.println("Start simple path of node: " + nodeId);
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
	System.err.println("Visited node: " + nodeId + " flags: " + nodeFlags
		+ " with edge " + edgeId);
	if (this.nextClient != null)
	    this.nextClient.visitNode(edgeId, nodeId, nodeFlags);
    }

    public boolean getOnlyOne() {
	return false;
    }

    public int getOrder() {
	return IGlobalComponentOrder.NOT_EXECUTED;
    }

    public void init() throws InitException {
	this.nextClient = (IGraphTracerClient) this.dataStore
		.getObject(IConstraintsGeneratorConstants.LAST_TRACER_CLIENT);
	this.dataStore.storeObject(
		IConstraintsGeneratorConstants.LAST_TRACER_CLIENT, this);
    }

    public String call() throws Exception {
	// not executed, called from graph tracer
	return null;
    }

    public void endCatchPath(int nodeId) {
	System.err.println("End catch path of node: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.endCatchPath(nodeId);
    }

    public void startCatchPath(int nodeId, int startEdgeId, int endEdgeId) {
	System.err.println("Start catch path of node: " + nodeId+" start edge "+startEdgeId+" end edge "+endEdgeId);
	if (this.nextClient != null)
	    this.nextClient.startCatchPath(nodeId, startEdgeId, endEdgeId);
    }
    
    public void stillInCatch(int nodeId) {
	System.err.println("Still in catch of node: " + nodeId);
	if (this.nextClient != null)
	    this.nextClient.stillInCatch(nodeId);
    }
}
