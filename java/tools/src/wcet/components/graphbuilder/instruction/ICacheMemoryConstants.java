/**
 * 
 */
package wcet.components.graphbuilder.instruction;

/**
 * @author Elena Axamitova
 * @version 0.1 23.03.2007
 */
public interface ICacheMemoryConstants {
    
    /**
     * number of wait states (cycles) for a read ram access
     */
    public static final int rws = 1;
    
    /**
     * number of wait states(cycles) for a write ram access
     */
    public static final int wws = 1;
    
    /**
     * method cache load wait states
     */
    public static final int cws = 0;
}
