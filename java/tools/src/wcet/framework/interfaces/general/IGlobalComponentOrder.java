/*
 * IGlobalComponentOrder.java, WCETA tool
 */
package wcet.framework.interfaces.general;

/**
 * Defines constants for priorities reserved by first level
 * components. Can be used for validity check of dynamicaly assembled
 * analysers. 
 * @author Elena Axamitova
 * @version 0.3
 */
public interface IGlobalComponentOrder {
	
	/**
	 * default order value
	 */
	public static final int UNKNOWN_ORDER = 0;
	
	/**
	 * not executed component
	 */
	public static final int NOT_EXECUTED = -1;
	
	/**
	 * order of the annotations checker component
	 */
	public static final int ANNOTATIONS_CHECHER = 10;
	
	/**
	 * order of the graph builder component
	 */
	public static final int GRAPH_BUILDER = 20;
	
	//30 free
	/**
	 * order of the constraints generator
	 */
	public static final int CONSTRAINS_GENERATOR = 40;
	
	
	//60 free
	
	/**
	 * order of the ILP solver components
	 */
	public static final int ILP_SOLVER = 70;
	
	//80 free
	
	/**
	 * order of the output writer
	 */
	public static final int OUTPUT_WRITER = 90;
	
	
}
