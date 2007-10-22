/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;
import java.util.Iterator;

import wcet.components.lpsolver.ILpSolverConstants;
import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.solver.ILpResult;

/**
 * @author Elena Axamitova
 * @version 0.1 05.02.2007
 * 
 * Writes the control flow graph to the output in the dot format.
 */
public class DotGraphOutputWriter implements IAnalyserComponent {
    /**
         * Generated control flow graph
         */
    private IControlFlowGraph cfg;

    /**
         * Shared data store.
         */
    private IDataStore dataStore;

    /**
         * All ouput messages buffered
         */
    private StringBuffer buffer;

    /**
         * Analyser output
         */
    private PrintStream output;

    /**
         * LP solver result
         */
    private ILpResult lpResult;

    public DotGraphOutputWriter(IDataStore ds) {
	this.dataStore = ds;
	this.buffer = new StringBuffer();
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
	return IGlobalComponentOrder.OUTPUT_WRITER;
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
         */
    public void init() throws InitException {
	this.output = this.dataStore.getOutput();
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
    public String call() throws Exception {
	this.cfg = this.dataStore.getGraph();
	this.lpResult = (ILpResult) this.dataStore
		.getObject(ILpSolverConstants.LPSOLVE_RESULT_KEY);
	this.printGraph();
	this.output.print(this.buffer.toString());
	this.output.flush();
	return "+++Dot Graph Writer Completed Successfully.+++\n";
    }

    /**
         * Creates textual representation of the control flow graph in dot
         * format and saves it in the result buffer.
         */
    private void printGraph() {

	this.buffer.append("digraph G {\n");
	this.buffer.append("size = \"10,7.5\"\n");

	// System.out.println("******************");

	Iterator iter = this.cfg.getAllVertices().iterator();
	while (iter.hasNext()) {
	    IVertex v = (IVertex) iter.next();
	    // System.out.println(v);
	    for (Iterator eit = v.getOutgoingEdges().iterator(); eit.hasNext();) {
		IEdge outEdge = this.cfg.findEdgeByIndex((Integer) eit.next());
		Integer sucId = outEdge.getToVertex();
		IVertex suc = this.cfg.findVertexByIndex(sucId);
		char labelChar = outEdge.isExceptionEdge() ? 'e' : 'f';
		int edgeId = outEdge.getIndex();
		double edgeVar = 0;
		if(this.lpResult!=null){
		    edgeVar = this.lpResult.getVarValue("f" + edgeId);
		}
		if (edgeVar > 0) {
		    this.buffer.append("\t edge[color=red,labelfontcolor=red];\n");
		    this.buffer.append("\t\"" + v.toString() + "\" -> \""
			    + suc.toString() + "\" [label=\"" + labelChar
			    + edgeId + "=" + edgeVar + "\"];" + "\n");
		} else {
		    this.buffer.append("\t edge[color=black,labelfontcolor=black];\n");
		    this.buffer.append("\t\"" + v.toString() + "\" -> \""
			    + suc.toString() + "\" [label=\"" + labelChar
			    + edgeId + "\"];" + "\n");
		}

	    }
	}

	this.buffer.append("}\n");
    }
}
