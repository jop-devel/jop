/**
 * 
 */
package wcet.framework.general;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.general.IDataStore;
import wcet.framework.interfaces.general.IDataStoreKeys;
import wcet.framework.interfaces.solver.IConstraint;

/**
 * @author Elena
 * @version 0.2
 */
public abstract class AbstractDataStore implements IDataStore {
	PrintStream output;
	Vector<IConstraint> constraints;
	IControlFlowGraph graph;
	Hashtable<String, Object> otherObjects;
	
	public boolean setClasspath(String cp){
	    if(cp==null)return false;
		if (this.otherObjects.containsKey(IDataStoreKeys.CLASSPATH_KEY)){
			return false;
		} else {
			this.otherObjects.put(IDataStoreKeys.CLASSPATH_KEY, cp);
			return true;
		}
	}
	
	public boolean setSourcepath(String sp){
	    if(sp==null)return false;
		if (this.otherObjects.containsKey(IDataStoreKeys.SOURCEPATH_KEY)){
			return false;
		} else {
			this.otherObjects.put(IDataStoreKeys.SOURCEPATH_KEY, sp);
			return true;
		}
	}
	
	public boolean setOutput(PrintStream os){
	    if(os==null)return false;
		if (this.output == null){
			this.output = os;
			return true;
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#addConstraint(wcet.framework.interfaces.constraints.IConstraint)
	 */
	public void addConstraint(IConstraint constraint) {
		this.constraints.add(constraint);
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#addConstraints(java.util.List)
	 */
	public void addConstraints(List<IConstraint> constraints) {
		this.constraints.addAll(constraints);

	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getAllConstraints()
	 */
	public List<IConstraint> getAllConstraints() {
		return this.constraints;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getClasspath()
	 */
	public String getClasspath() {
		return (String)this.otherObjects.get(IDataStoreKeys.CLASSPATH_KEY);
	}
	
	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getSourcepath()
	 */
	public String getSourcepath() {
		return (String)this.otherObjects.get(IDataStoreKeys.SOURCEPATH_KEY);
	}
	public String getMainMethodDescriptor() {
		return (String)this.otherObjects.get(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY);
	}

	public String getMainMethodName() {
		return (String)this.otherObjects.get(IDataStoreKeys.MAINMETHOD_NAME_KEY);
	}

	public boolean setMainMethodDescriptor(String descriptor) {
	    if(descriptor==null)return false;
		if (this.otherObjects.containsKey(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY)){
			return false;
		} else {
			this.otherObjects.put(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY, descriptor);
			return true;
		}
	}

	public boolean setMainMethodName(String name) {
	    if(name==null)return false;
		if (this.otherObjects.containsKey(IDataStoreKeys.MAINMETHOD_NAME_KEY)){
			return false;
		} else {
			this.otherObjects.put(IDataStoreKeys.MAINMETHOD_NAME_KEY, name);
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getGraph()
	 */
	public IControlFlowGraph getGraph() {
		return this.graph;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getObject(java.lang.String)
	 */
	public Object getObject(String key) {
		return this.otherObjects.get(key);
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#getOutput()
	 */
	public PrintStream getOutput() {
		return this.output;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#removeObject(java.lang.String)
	 */
	public Object removeObject(String key) {
		return this.removeObject(key);
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#setGraph(wcet.interfaces.graph.IFlowGraph)
	 */
	public void setGraph(IControlFlowGraph graph) {
	    
		this.graph = graph;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#storeObject(java.lang.String, java.lang.Object)
	 */
	public boolean storeObject(String key, Object obj) {
	    if(key==null) return false;
		this.otherObjects.put(key, obj);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see wcet.interfaces.general.IDataStore#isGraphSet()
	 */
	public boolean isGraphSet() {
		return (this.graph==null);
	}

	public String getTask() {
		return (String)this.otherObjects.get(IDataStoreKeys.TASK_KEY);
	}

	public boolean setTask(String ts) {
	    if(ts==null) return false;
		this.otherObjects.put(IDataStoreKeys.TASK_KEY, ts);
		return true;
	}

}
