/*
 * IDataStore.java, WCETA tool
 */
package wcet.framework.interfaces.general;

import java.io.PrintStream;
import java.util.List;

import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.solver.IConstraint;

/**
 * DataStore object is used to store information needed across component boundaries.
 * The keys used to object store and retrieval should be defined in an IDataStoreKeys 
 * interface.
 * @see IDataStoreKeys
 * @author Elena Axamitova
 * @version 0.3
 */
public interface IDataStore {
	/**
	 * Store the task to be executed.
	 * @param ts - the name of the currently analysed application entry point.
	 * @return - <code>true</code>if the task was accepted</code>
	 */
	public boolean setTask(String ts);
	/**
	 * Get the current task.
	 * @return - the name of the currently analysed application entry point.
	 */
	public String getTask();
	/**
	 * Store the classpath.
	 */
	public boolean setClasspath(String cp);
	/**
	 * The classpath set.
	 * @return - the complete path to the analysed application class files.
	 */
	public String getClasspath();
	
	/**
	 * Store the output destination.
	 * @param os - the output stream to be used for messages and results.
	 * @return
	 */
	public boolean setOutput(PrintStream os);
	
	/**
	 * Get the output destination.
	 * @return - the output stream to be used for messages and results.
	 */
	public PrintStream getOutput();
	
	/**
	 * Get the current flow graph.
	 * @return - the current representation of the application's programm flow.
	 */
	public IControlFlowGraph getGraph();
	
	/**
	 * Set the flow graph. If the graph is allready set, it should be modified, not
	 * replaced.
	 * @param graph  - current representation of the application's programm flow.
	 */
	public void setGraph(IControlFlowGraph graph);
	
	/**
	 * Get the state of the graph stored.
	 * @return  - <code>true</code> if the graph is set.
	 */
	public boolean isGraphSet();
	/**
	 * Add a constraint to the current set.
	 * @param constraint - a constraint to be added.
	 */
	public void addConstraint(IConstraint constraint);
	
	/**
	 * Add all constraints in the list to the current constraints.
	 * @param constraints - constraints to be added.
	 */
	public void addConstraints(List<IConstraint> constraints);
	
	/**
	 * Get all constraints.
	 * @return - all constraints stored.
	 */
	public List<IConstraint> getAllConstraints();
	
	/**
	 * Store an unspecified object with the provided key.
	 * @param key - the key for the object retrieval
	 * @param obj - the onject to be stored.
	 */
	public boolean storeObject(String key, Object obj);
	
	/**
	 * Get an object stored under the provided key.
	 * @return - the stored object.
	 */
	public Object getObject(String key);
	
	/**
	 * Remove the object stored under this key form dataStore.
	 * @return
	 */
	public Object removeObject(String key);
	
	/**
	 * 
	 * @param sourcepath - the complete path to the analysed application java files.
	 */
	public boolean setSourcepath(String sourcepath);
	
	/**
	 * The classpath set.
	 * @return - the complete path to the analysed application java files.
	 */
	public String getSourcepath();
	
	
	/**
	 * 
	 * @param sourcepath - the complete path to the analysed application java files.
	 */
	public boolean setMainMethodName(String name);
	
	/**
	 * The classpath set.
	 * @return - the complete path to the analysed application java files.
	 */
	public String getMainMethodName();
	/**
	 * 
	 * @param sourcepath - the complete path to the analysed application java files.
	 */
	public boolean setMainMethodDescriptor(String descriptor);
	
	/**
	 * The classpath set.
	 * @return - the complete path to the analysed application java files.
	 */
	public String getMainMethodDescriptor();
	
}
