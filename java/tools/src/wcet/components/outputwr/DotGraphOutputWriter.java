/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;
import java.util.Iterator;

import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;

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
    private StringBuffer result;

    /**
     * Analyser output
     */
    private PrintStream output;

    public DotGraphOutputWriter(IDataStore ds) {
	this.dataStore = ds;
	this.result = new StringBuffer();
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
	this.printGraph();
	this.output.print(this.result.toString());
	this.output.flush();
	return "+++Dot Graph Writer Completed Successfully.+++\n";
    }

    /**
     * Creates textual representation of the control flow graph in dot format
     * and saves it in the result buffer.
     */
    private void printGraph() {

	this.result.append("digraph G {\n");
	this.result.append("size = \"10,7.5\"\n");

	//System.out.println("******************");

	Iterator iter = this.cfg.getAllVertices().iterator();
	while (iter.hasNext()) {
	    IVertex v = (IVertex) iter.next();
	    //System.out.println(v);
	    for (Iterator eit = v.getOutgoingEdges().iterator(); eit.hasNext();) {
		IEdge outEdge = this.cfg.findEdgeByIndex((Integer)eit.next());
		Integer sucId = outEdge.getToVertex();
		IVertex suc = this.cfg.findVertexByIndex(sucId);
		char labelChar = outEdge.isExceptionEdge()?'e':'f';
		this.result.append("\t\"" + v.toString() + "\" -> \""
			+ suc.toString() + "\" [label=\""+labelChar+outEdge.getIndex()+"\"]"+"\n");
	    }
	}

	this.result.append("}\n");
    }
}
