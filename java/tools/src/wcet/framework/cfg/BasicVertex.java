/**
 * 
 */
package wcet.framework.cfg;

import java.util.HashSet;

import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.cfg.IVertexData;

/**
 * @author Elena Axamitova
 * @version 0.2
 */
public class BasicVertex<T extends IVertexData> implements IVertex {
	
	private static int LAST_INDEX = 0;
	
	protected HashSet<Integer> outgoingEdges;
	protected HashSet<Integer> incomingEdges;

	private T data;
	private int index;

	private int loopCount;
	private HashSet<Integer> edgesToLoopBodyIds;
	private HashSet<Integer> inNotLoopEdges;
	
	private boolean isCatchHandler = false;
	
	public BasicVertex(T data){
		this.data = data;
		this.index = getNextIndex();
		this.outgoingEdges = new HashSet<Integer>();
		this.incomingEdges = new HashSet<Integer>();
		this.loopCount = -1;
	}
	
	protected synchronized static int getNextIndex(){
	    return LAST_INDEX++;
	}
	
	/* (non-Javadoc)
	 * @see wcet.interfaces.cfg.IVertex#getData()
	 */
	public T getData() {
		return this.data;
	}

	/* (non-Javadoc)
	 * @see wcet.interfaces.cfg.IVertex#getIndex()
	 */
	public int getIndex() {
		return this.index;
	}

	@Override
	public String toString(){
	    return "B"+Integer.valueOf(this.index).toString()+" " + this.data.toString();
	}

	public void addIngomingEdge(int id) {
	   this.incomingEdges.add(id);
	}

	public void addOutgoingEdge(int id) {
	   this.outgoingEdges.add(id);
	}

	public HashSet<Integer> getIncomingEdges() {
	    return this.incomingEdges;
	}

	public HashSet<Integer> getOutgoingEdges() {
	   return this.outgoingEdges;
	}

	public void addInNotLoopEdge(int eid) {
	    if(this.inNotLoopEdges == null){
		this.inNotLoopEdges = new HashSet<Integer>();
	    }
	    this.inNotLoopEdges.add(eid);
	}

	public void addEdgeToLoopBody(int eid) {
	    if(this.edgesToLoopBodyIds == null){
		this.edgesToLoopBodyIds = new HashSet<Integer>();
	    }
	    this.edgesToLoopBodyIds.add(eid);
	}
	
	public HashSet<Integer> getEdgesToLoopBody() {
	    return this.edgesToLoopBodyIds;
	}

	public HashSet<Integer> getInNotLoopEdges() {
	    return this.inNotLoopEdges;
	}

	public int getLoopCount() {
	    return this.loopCount;
	}

	public void setLoopCount(int lc) {
	    this.loopCount = lc;
	}

	public boolean isLoopControler() {
	    return this.loopCount!=-1;
	}

	public boolean isCatchHandler() {
	    return this.isCatchHandler;
	}

	public void setCatchHandler() {
	    this.isCatchHandler=true;
	}

}
