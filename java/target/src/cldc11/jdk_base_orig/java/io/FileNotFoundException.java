package java.io;

/**
 * This exception is thrown to indicate that a file was not found
 */
public class FileNotFoundException extends IOException {

	/**
	 * Create an exception without a descriptive error message.
	 */
	public FileNotFoundException() {
	}

	/**
	 * Create an exception with a descriptive error message.
	 *
	 * @param message the descriptive error message
	 */
	public FileNotFoundException(String message) {
		super(message);
	}
}
