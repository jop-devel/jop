/**
 * 
 */
package wcet.components.constraintsgen;

/**
 * @author Elena Axamitova
 * @version 0.1 15.04.2007
 * 
 * Constant values used in the constraintgen package.
 */
public interface IConstraintsGeneratorConstants {
    /**
     * Order of the control flow graph tracer
     */
    public static final int CONTROL_FLOW_GRAPH_TRACER_ORDER = 3;
    
    /**
     * Order of the cache constraints generator
     */
    public static final int CACHE_CONSTRAINTS_GENERATOR_ORDER = 5;
    
    /**
     * Order of the flow constraints generator 
     */
    public static final int FLOW_CONSTRAINTS_GENERATOR_ORDER = 7;
 
    /**
     * Key of the objective function key in the data store
     */
    public static final String OBJ_FUNCTION_KEY = "ConstraintsGenerator.ObjectiveFunction";
    
    /**
     * Key of the last tracer client in the data store
     */
    public static final String LAST_TRACER_CLIENT = "ConstraintsGenerator.LastTracerClient";
    
    /**
     * Name of the first edge - imaginary, without start vertex, inflow of the root
     */
    public static final String FIRST_EDGE_NAME = "fs";

    /**
     * * Name of the last edge - imaginary, without end vertex, outflow of a last vertex
     */
    public static final String LAST_EDGE_NAME = "ft";
    
    /**
     * Suffix added to edge name when dividing flow between cache hit and cache miss,
     * cache hit part
     */
    public static final String CACHE_HIT_EDGE_SUFFIX = "_ch";

    /**
     * Suffix added to edge name when dividing flow between cache hit and cache miss,
     * cache miss part
     */
    public static final String CACHE_MISS_EDGE_SUFFIX = "_cm";
}
