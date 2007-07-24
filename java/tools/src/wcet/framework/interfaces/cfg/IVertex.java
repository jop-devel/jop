package wcet.framework.interfaces.cfg;

import java.util.HashSet;


public interface IVertex<T extends IVertexData> {
	
	public HashSet<Integer> getOutgoingEdges();
	
	public HashSet<Integer> getIncomingEdges();
	
	public void addIngomingEdge(int id);
	
	public void addOutgoingEdge(int id);
	
	public T getData();
	
	//public void setData(T data);
	
	public int getIndex();
	
	public String toString();
	
	public boolean isLoopControler();
	
	public int getLoopCount();
	
	public void setLoopCount(int lc);
	
	public HashSet<Integer> getEdgesToLoopBody();
	
	public void addEdgeToLoopBody(int eid);
	
	public HashSet<Integer> getInNotLoopEdges();
	
	public void addInNotLoopEdge(int eid);
	
	public boolean isCatchHandler();
	
	public void setCatchHandler();
}
