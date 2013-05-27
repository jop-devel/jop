package libcsp.csp.util;

/**
 * This interface should be implemented by "disposable objects" or objects that
 * should be returned into a resource pool.
 * 
 * @author Mikkel Todberg, Jeppe Lund Andersen
 * 
 */
public interface IDispose {
	
	/**
	 * Returns an object into its corresponding resource pool.
	 */
	public void dispose();
}
