/**
 * 
 */
package wcet.components.constraintsgen.graphtracer;

/**
 * @author Elena Axamitova
 * @version 0.1 06.05.2007
 */
public interface IGraphTracerClient {
    /**
         * A flag for a node with one incomming and one outgoing edge that is
         * not an invore nor return (e.g. nothing special)
         */
    public static final int SIMPLE_NODE = 0;

    /**
         * A flag for an invoke node
         */
    public static final int INVOKE_NODE = 1;

    /**
         * A flag for a return node
         */
    public static final int RETURN_NODE = 2;

    /**
         * A flag for a node with more than one outgoing edge. A loop controler
         * is a fork node if it has two (disjunctive) loop paths.
         */
    public static final int FORK_NODE = 4;

    /**
         * A flag for a node with more than one incomming edge. A loop controler
         * is a join node if it has more than one incomming edge from outside
         * the loop.
         */
    public static final int JOIN_NODE = 8;

    /**
         * A flag for a loop controler node
         */
    public static final int LOOP_CONTROLER_NODE = 16;

    /**
         * A flag for a node without outgoing edges (end of processing)
         */
    public static final int END_NODE = 32;

    /**
         * A flag for a node without incomming edges (start of processing)
         */
    public static final int START_NODE = 64;

    /**
         * A flag for a node that starts catch handling
         */
    public static final int CATCH_HANDLER_NODE = 128;

    /**
         * Called at the beginning of graph visit.
         */
    public void startGraphVisit();

    /**
         * Called at the end of graph visit.
         */
    public void endGraphVisit();

    /**
         * Visit a node of graph. Every node is visited only once.
         * 
         * @param edgeId -
         *                an incoming edge of the visited node that is on the
         *                currently visited path
         * @param nodeId - id of the visited node
         * @param nodeFlags - flags of the node
         */
    public void visitNode(int edgeId, int nodeId, int nodeFlags);

    /**
     * Start a simple path in the node. A simple path constains only simple
     * nodes, start and end nodes excluding.
         * @param nodeId
         */
    public void startSimplePath(int nodeId);

    /**
     * End a simple path in the node. A simple path constains only simple
     * nodes, start and end nodes excluding.
         * @param nodeId
         */
    public void endSimlePath(int nodeId);

    /**
     * Start a loop path of the loop controler with nodeId.
         * @param nodeId - id of the loop controler.
         */
    public void startLoopPath(int nodeId);

    /**
     * End a loop path of the loop controler with nodeId.
         * @param nodeId - id of the loop controler.
         */
    public void endLoopPath(int nodeId);

    /**
     * Called just after endLoopPath(nodeId), if the loop
     * path visit of the loop controler is still in progress (for example if the 
     * loop path forked and the second path is in loop).
         * @param nodeId
         */
    public void stillInLoop(int nodeId);

    /**
     * Visit start catch handler node.
         * @param nodeId - catch handler start node id
         * @param startEdgeId - start of catch handler scope (including)
         * @param endEdgeId - end of catch hadler scope (excluding)
         */
    public void startCatchPath(int nodeId, int startEdgeId, int endEdgeId);

    /**
     * Visit end of catch handler path.
         * @param nodeId - catch handler start node id
         */
    public void endCatchPath(int nodeId);

    /**
     * Called just after endCatchPath(nodeId), if the catch
     * handler path visit is still in progress (for example if the 
     * catch handler path forked).
         * @param nodeId - catch handler start node id
         */
    public void stillInCatch(int nodeId);
}
