/**
 * 
 */
package wcet.framework.interfaces.util;

import java.io.PrintStream;

import wcet.framework.exceptions.SurplusComponentException;
import wcet.framework.exceptions.TaskExecutionException;
import wcet.framework.interfaces.general.IAnalyserComponent;

/**
 * Iterface defining methods needed to register and run components 
 * in system. 
 * @author Elena Axamitova
 * @version 0.2
 */
public interface IComponentRunner {

	/**
	 * Registers the given component.
	 * @param componentToRegister -
	 *            the component to register
	 * @throws SurplusComponentException -
	 *             if there is a registred component with the same priority and
	 *             the <code>onlyOne</code> field of either the registred component or
	 *             the componentToRegister is <code>true</true>
	 */
	public void registerComponent(IAnalyserComponent componentToRegister)
			throws SurplusComponentException;
	
	/**
	 * Removes the component from execution key.
	 * @param componentToRemove - the component to register
	 * @return - <code>true</code> if the component was found and successfully removed
	 */
	public boolean removeComponent(IAnalyserComponent componentToRemove);
	
	/**
	 * Starts the analysis, e.g. runs all components.
	 * After calling this method, registration and removal of components is not possible.
	 * @throws TaskExecutionException when a problems in execution occurs.
	 */
	public void startComponents() throws TaskExecutionException;
	
	/**
	 * Sets the PrintStream to be used as output for components' messages. 
	 * If not set, any message received during the execution will be discarded.
	 * @param ps - the output PrintStream
	 */
	public void setOutput(PrintStream ps);

}
