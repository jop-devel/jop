/**
 * 
 */
package wcet.framework.cfg;

import java.util.Collection;
import java.util.HashMap;

import wcet.framework.interfaces.cfg.IControlFlowGraph;
import wcet.framework.interfaces.cfg.IEdge;
import wcet.framework.interfaces.cfg.IVertex;
import wcet.framework.interfaces.cfg.IVertexData;

/**
 * @author Elena Axamitova
 * @version 0.2
 */
public class BasicControlFlowGraph<T extends IVertexData> implements
	IControlFlowGraph {
    protected HashMap<Integer, IVertex<T>> indexVertexMap;

    protected HashMap<Integer, IEdge> indexEdgeMap;

    public BasicControlFlowGraph() {
	this.indexVertexMap = new HashMap<Integer, IVertex<T>>();
	this.indexEdgeMap = new HashMap<Integer, IEdge>();
    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.cfg.IControlFlowGraph#addEdge(wcet.interfaces.cfg.IVertex,
         *      wcet.interfaces.cfg.IVertex)
         */
    @SuppressWarnings("unchecked")
    public int addEdge(int from, int to) {
	IEdge newEdge = new BasicEdge(from, to);
	int newEdgeId = newEdge.getIndex();
	this.indexEdgeMap.put(newEdgeId, newEdge);
	IVertex fromVertex = this.findVertexByIndex(from);
	IVertex toVertex = this.findVertexByIndex(to);
	fromVertex.addOutgoingEdge(newEdgeId);
	toVertex.addIngomingEdge(newEdgeId);
	return newEdgeId;

    }

    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.cfg.IControlFlowGraph#findVertexByIndex(int)
         */
    public IVertex<T> findVertexByIndex(int idx) {
	return this.indexVertexMap.get(Integer.valueOf(idx));
    }

    /* (non-Javadoc)
     * @see wcet.framework.interfaces.cfg.IControlFlowGraph#findEdgeByIndex(int)
     */
    public IEdge findEdgeByIndex(int idx) {
	return this.indexEdgeMap.get(Integer.valueOf(idx));
    }
    
    /*
         * (non-Javadoc)
         * 
         * @see wcet.interfaces.cfg.IControlFlowGraph#newVertex(wcet.interfaces.cfg.IVertexData)
         */
    @SuppressWarnings("unchecked")
    public int addVertex(IVertexData data) {
	IVertex<T> newVertex = new BasicVertex<T>((T) data);
	this.indexVertexMap.put(Integer.valueOf(newVertex.getIndex()),
		newVertex);
	return newVertex.getIndex();
    }

    public Collection<IVertex<T>> getAllVertices() {
	return this.indexVertexMap.values();
    }

    public Collection<IEdge> getAllEdges() {
	return this.indexEdgeMap.values();
    }

    public IVertex getRoot() {
	return this.findVertexByIndex(IControlFlowGraph.ROOT_ID);
    }

    public int getEdgeCount() {
	return this.indexEdgeMap.size();
    }

    public int getVeticesCount() {
	return this.indexVertexMap.size();
    }
}
