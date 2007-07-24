package wcet.framework.interfaces.general;

import java.util.Properties;

import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskException;
import wcet.framework.exceptions.TaskInitException;

/**
 * Interface to be implemented by all analysers using this framework.
 * @author Elena Axamitova
 * @version 0.2
 */
public interface IAnalyser {
	
	/**
	 * Initializes the analyser object, including all its components. This method should be called
	 * only once during the program execution.
	 * @param arguments - a properties object with arguments and settings. 
	 * @throws InitException - thrown when the initiation of the analyser fails.
	 */
	public void init(Properties arguments) throws InitException;
	
	/**
	 * Prepares the analyser for the execution of a concrete analysis. This method is called
	 * for every task. 
	 * @param mainClass - the name of the application entry point, e.g of the class containing
	 * 					the main method.
	 * @throws TaskInitException - when the execution preparation fails.
	 */
	public void setTask(String mainClass) throws TaskInitException; 
	
	/**
	 * Starts the analysis execution.
	 * @throws TaskException - when the execution failes.
	 */
	public void analyse() throws TaskException;
}
