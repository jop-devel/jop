/**
 * 
 */
package wcet.framework.cfg;

import wcet.framework.interfaces.cfg.IEdge;

/**
 * @author Elena Axamitova
 * @version 0.1
 */
public class BasicEdge implements IEdge {
	private static int LAST_INDEX = 0;
	
	protected int fromVertex;
	protected int toVertex;
	protected int index;
	protected int frequency;
	protected boolean exceptionEdge = false;
	
	public BasicEdge(int v1, int v2){
		this.fromVertex = v1;
		this.toVertex = v2;
		this.frequency = 1;
		this.index = getNextIndex();
	}
	
	public BasicEdge(int v1, int v2, boolean exc){
		this.fromVertex = v1;
		this.toVertex = v2;
		this.frequency = 1;
		this.index = getNextIndex();
		this.exceptionEdge = exc;
	}
	
	protected synchronized static int getNextIndex(){
	    return LAST_INDEX++;
	}
	
	/* (non-Javadoc)
	 * @see wcet.interfaces.cfg.IEdge#getFromVertex()
	 */
	public int getFromVertex() {
		return this.fromVertex;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.cfg.IEdge#getIndex()
	 */
	public int getIndex() {
		return this.index;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.cfg.IEdge#getToVertex()
	 */
	public int getToVertex() {
		return this.toVertex;
	}
	/* (non-Javadoc)
	 * @see wcet.framework.interfaces.cfg.IEdge#getFrequency()
	 */
	public int getFrequency() {
	    return this.frequency;
	}
	/* (non-Javadoc)
	 * @see wcet.framework.interfaces.cfg.IEdge#setFrequency(int)
	 */
	public void setFrequency(int f) {
	    this.frequency = f;;
	}
	
	public String toString(){
	    return "f"+this.index;
	}
	
	public void setExceptionEdge(){
	    this.exceptionEdge = true;
	}
	
	public boolean isExceptionEdge(){
	    return this.exceptionEdge;
	}
}
