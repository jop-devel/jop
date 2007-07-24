/**
 * 
 */
package wcet.framework.exceptions;


/**
 * @author Elena
 *
 */
public class TaskInitException extends TaskException {


	public TaskInitException() {
	}


	public TaskInitException(String message) {
		super(message);
	}

	public TaskInitException(Throwable cause) {
		super(cause);
	}

	public TaskInitException(String message, Throwable cause) {
		super(message, cause);
	}

}
