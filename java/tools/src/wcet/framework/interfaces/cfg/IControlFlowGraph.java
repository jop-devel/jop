package wcet.framework.interfaces.cfg;

import java.util.Collection;


public interface IControlFlowGraph<T extends IVertexData>{
	
    	public static final int ROOT_ID = 0;
    	
    	public static final int TAIL_ID = 1;
    
	public int addVertex(T data);
	
	public IVertex<T> getRoot();
	
	public IVertex<T> findVertexByIndex(int idx);
	
	public int addEdge(int v1, int v2);
	
	public IEdge findEdgeByIndex(int idx);
	
	public Collection<IVertex> getAllVertices();
	
	public int getVeticesCount();
	
	public Collection<IEdge> getAllEdges();
	
	public int getEdgeCount();
	
}
