package java.util;

public class NoPoolElementException extends RuntimeException{
	
	  /**
	   * Create an exception without a message.
	   */
	  public NoPoolElementException()
	  {
		  super("NoPoolElementException");
	  }

	  /**
	   * Create an exception with a message.
	   *
	   * @param s the message
	   */
	  public NoPoolElementException(String s)
	  {
	    super(s);
	  }

	  /**
	   * Create an exception indicating the illegal index.
	   *
	   * @param index the invalid index
	   */
	  public NoPoolElementException(int i, int j)
	  {
	    super("Not enough pool elements: " + i +" < " +j);
	  }

}
