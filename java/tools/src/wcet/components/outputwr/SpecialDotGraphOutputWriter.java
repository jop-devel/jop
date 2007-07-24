/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;
import java.util.Iterator;

import wcet.components.graphbuilder.blocks.BasicBlock;
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
public class SpecialDotGraphOutputWriter implements IAnalyserComponent {
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

    public SpecialDotGraphOutputWriter(IDataStore ds) {
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
	return "+++Special Dot Graph Writer Completed Successfully.+++\n";
    }

    /**
         * Creates textual representation of the control flow graph in dot
         * format and saves it in the result buffer.
         */
    private void printGraph() {

	this.result.append("digraph G {\n");
	this.result.append("size = \"10,7.5\"\n");

	// System.out.println("******************");

	Iterator iter = this.cfg.getAllVertices().iterator();
	while (iter.hasNext()) {
	    IVertex v = (IVertex) iter.next();
	    BasicBlock bb = (BasicBlock) v.getData();
	    if ((bb.getType() == BasicBlock.INVOKE_BB)
		    || (bb.getType() == BasicBlock.RETURN_BB)) {
		if ((v.getIncomingEdges().size() != 0)
			&& (v.getOutgoingEdges().size() != 0)) {
		    IEdge inEdge = this.cfg.findEdgeByIndex((Integer) v
			    .getIncomingEdges().iterator().next());
		    Integer predId = inEdge.getFromVertex();
		    IVertex pred = this.cfg.findVertexByIndex(predId);
		    IEdge outEdge = this.cfg.findEdgeByIndex((Integer) v
			    .getOutgoingEdges().iterator().next());
		    Integer succId = (Integer) outEdge.getToVertex();
		    IVertex succ = this.cfg.findVertexByIndex(succId);
		    char labelChar = inEdge.isExceptionEdge() ? 'e' : 'f';
		    this.result.append("\t\"" + pred.toString() + "\" -> \""
			    + succ.toString() + "\" [label=\"" + labelChar
			    + inEdge.getIndex() + "_h" + "\"]" + "\n");
		    this.result.append("\t\"" + v.toString() + "\" -> \""
			    + succ.toString() + "\"\n");
		}
	    } else {
		// System.out.println(v);
		for (Iterator eit = v.getOutgoingEdges().iterator(); eit
			.hasNext();) {
		    IEdge outEdge = this.cfg.findEdgeByIndex((Integer) eit
			    .next());
		    Integer sucId = outEdge.getToVertex();
		    IVertex suc = this.cfg.findVertexByIndex(sucId);
		    BasicBlock sucBB = (BasicBlock) suc.getData();
		    char labelChar = outEdge.isExceptionEdge() ? 'e' : 'f';
		    if ((sucBB.getType() == BasicBlock.INVOKE_BB)
			    || (sucBB.getType() == BasicBlock.RETURN_BB)) {
			this.result.append("\t\"" + v.toString() + "\" -> \""
				+ suc.toString() + "\" [label=\"" + labelChar
				+ outEdge.getIndex() + "_m \"]" + "\n");

		    } else
			this.result.append("\t\"" + v.toString() + "\" -> \""
				+ suc.toString() + "\" [label=\"" + labelChar
				+ outEdge.getIndex() + "\"]" + "\n");
		}
	    }
	}

	this.result.append("}\n");
    }
}
