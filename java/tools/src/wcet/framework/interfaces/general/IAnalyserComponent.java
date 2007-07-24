/*
 * IAnalyserComponent.java, WCETA tool 
 */
package wcet.framework.interfaces.general;

import java.util.concurrent.Callable;

import wcet.framework.exceptions.InitException;

/**
 * Interface of an analyser component. An analyser component performs
 * a well specified part of the analysis.
 * @author Elena Axamitova
 * @version 0.4
 */
public interface IAnalyserComponent extends Callable<String>{
	
	/**
	 * Initializes the component. 
	 * @throws InitException
	 */
	public void init() throws InitException;
	
	/**
	 * Returns the component's order in the execution sequence.
	 * @see IGlobalComponentOrder
	 * @return - the execution order of the component
	 */
	public int getOrder();
	
	/**
	 * <code>true</code> if there is to be only one component of this order in the system,
	 * <code>false</code> otherwise.
	 * @return - the onlyOne property of the component
	 */
	public boolean getOnlyOne();
	
	/*
	 * Sets the data store object to be used by the component. The component can refuse this
	 * data store.
	 * @param ds - the data store object
	 * @return <code>true</code> when this data store will be used.
	 */
	//public boolean setDataStore(IDataStore ds);

}
