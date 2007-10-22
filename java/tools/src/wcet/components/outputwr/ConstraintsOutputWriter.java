/**
 * 
 */
package wcet.components.outputwr;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import wcet.framework.exceptions.InitException;
import wcet.framework.interfaces.general.IAnalyserComponent;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IGlobalComponentOrder;
import wcet.framework.interfaces.solver.IConstraint;

/**
 * @author Elena Axamitova
 * @version 0.1 22.04.2007
 * 
 * Writes all constraints to the output. Used for testing and debugging.
 */
public class ConstraintsOutputWriter implements IAnalyserComponent {
    
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

    public ConstraintsOutputWriter(IDataStore ds) {
	this.dataStore = ds;
	this.result = new StringBuffer();
    }
    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOnlyOne()
     */
    public boolean getOnlyOne() {
	return false;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#getOrder()
     */
    public int getOrder() {
	return IGlobalComponentOrder.OUTPUT_WRITER;
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.general.IAnalyserComponent#init()
     */
    public void init() throws InitException {
	this.output = this.dataStore.getOutput();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public String call() throws Exception {
	List<IConstraint> constraints = this.dataStore.getAllConstraints();
	for(Iterator<IConstraint> iterator= constraints.iterator();iterator.hasNext();){
	    this.result.append(iterator.next().toString()+"\n");
	}

	this.output.print(this.result.toString());
	this.output.flush();
	return "+++Constraints Writer Completed Successfully.+++\n";
    }

}
