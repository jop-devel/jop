/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;
import java.util.Iterator;

import wcet.components.graphbuilder.blocks.BasicBlock;
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
    private StringBuffer buffer;

    /**
         * Analyser output
         */
    private PrintStream output;

    private ILpResult lpResult;

    public SpecialDotGraphOutputWriter(IDataStore ds) {
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
	return "+++Special Dot Graph Writer Completed Successfully.+++\n";
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
		    double outEdgeVar = 0, hitEdgeVar = 0;
		    if (this.lpResult != null) {
			outEdgeVar = this.lpResult.getVarValue("f"
				+ outEdge.getIndex());
			hitEdgeVar = this.lpResult.getVarValue("f"
				+ inEdge.getIndex() + "_ch");
		    }
		    if (hitEdgeVar > 0) {
			this.buffer
				.append("\t edge[color=red,labelfontcolor=red];\n");
			char inLabelChar = inEdge.isExceptionEdge() ? 'e' : 'f';
			this.buffer.append("\t\"" + pred.toString()
				+ "\" -> \"" + succ.toString() + "\" [label=\""
				+ inLabelChar + inEdge.getIndex() + "_h" + "="
				+ hitEdgeVar + "\"]" + "\n");
		    } else {
			this.buffer
				.append("\t edge[color=black,labelfontcolor=black];\n");
			char inLabelChar = inEdge.isExceptionEdge() ? 'e' : 'f';
			this.buffer.append("\t\"" + pred.toString()
				+ "\" -> \"" + succ.toString() + "\" [label=\""
				+ inLabelChar + inEdge.getIndex() + "_h"
				+ "\"]" + "\n");
		    }
		    if (outEdgeVar > 0) {
			this.buffer
				.append("\t edge[color=red,labelfontcolor=red];\n");
			char outLabelChar = inEdge.isExceptionEdge() ? 'e'
				: 'f';
			this.buffer.append("\t\"" + v.toString() + "\" -> \""
				+ succ.toString() + "\"[label=\""
				+ outLabelChar + outEdge.getIndex() + "="
				+ (outEdgeVar-hitEdgeVar) + "\"]\n");
		    } else {
			this.buffer
				.append("\t edge[color=black,labelfontcolor=black];\n");
			char outLabelChar = inEdge.isExceptionEdge() ? 'e'
				: 'f';
			this.buffer.append("\t\"" + v.toString() + "\" -> \""
				+ succ.toString() + "\"[label=\""
				+ outLabelChar + outEdge.getIndex() + "\"]\n");
		    }
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
		    double edgeVar = 0, missEdgeVar = 0;
		    if (this.lpResult != null) {
			edgeVar = this.lpResult.getVarValue("f"
				+ outEdge.getIndex());
			missEdgeVar = this.lpResult.getVarValue("f"
				+ outEdge.getIndex()+"_cm");
		    }
		    char labelChar = outEdge.isExceptionEdge() ? 'e' : 'f';
		    if ((sucBB.getType() == BasicBlock.INVOKE_BB)
			    || (sucBB.getType() == BasicBlock.RETURN_BB)) {
			if (missEdgeVar > 0) {
			    this.buffer
				    .append("\t edge[color=red,labelfontcolor=red];\n");

			    this.buffer.append("\t\"" + v.toString()
				    + "\" -> \"" + suc.toString()
				    + "\" [label=\"" + labelChar
				    + outEdge.getIndex() + "_m=" + missEdgeVar
				    + "\"]" + "\n");
			} else if (edgeVar>0){
			    this.buffer
				    .append("\t edge[color=red,labelfontcolor=red];\n");
			    this.buffer.append("\t\"" + v.toString()
				    + "\" -> \"" + suc.toString()
				    + "\" [label=\"" + labelChar
				    + outEdge.getIndex() + "_m=" + edgeVar
				    + "\"]" + "\n");
			}else {
			    this.buffer
			    .append("\t edge[color=black,labelfontcolor=black];\n");
		    this.buffer.append("\t\"" + v.toString()
			    + "\" -> \"" + suc.toString()
			    + "\" [label=\"" + labelChar
			    + outEdge.getIndex() + "_m\"]\n");
			}

		    } else {
			if (edgeVar > 0) {
			    this.buffer
				    .append("\t edge[color=red,labelfontcolor=red];\n");
			    this.buffer.append("\t\"" + v.toString()
				    + "\" -> \"" + suc.toString()
				    + "\" [label=\"" + labelChar
				    + outEdge.getIndex() + "=" + edgeVar
				    + "\"]" + "\n");
			} else {
			    this.buffer
				    .append("\t edge[color=black,labelfontcolor=black];\n");
			    this.buffer.append("\t\"" + v.toString()
				    + "\" -> \"" + suc.toString()
				    + "\" [label=\"" + labelChar
				    + outEdge.getIndex() + "\"]" + "\n");
			}
		    }
		}
	    }
	}

	this.buffer.append("}\n");
    }
}
