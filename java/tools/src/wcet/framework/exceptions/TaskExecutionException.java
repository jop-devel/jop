package wcet.framework.exceptions;


public class TaskExecutionException extends TaskException {
	public TaskExecutionException(){
		super();
	}
	
	public TaskExecutionException(String message){
		super(message);
	}
	
	public TaskExecutionException(Throwable cause) {
		super(cause);
	}

	public TaskExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
