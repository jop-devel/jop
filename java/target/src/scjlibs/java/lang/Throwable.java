
package java.lang;

import java.io.PrintStream;

// public class Throwable implements Serializable
public class Throwable
{
  private final String detailMessage;

  private Throwable cause = this;

//  private StackTraceElement[] stackTrace;

  private int stack_pointer;

  public Throwable()
  {
    this((String) null);
  }

  public Throwable(String message)
  {
    fillInStackTrace();
    detailMessage = message;
  }

  public Throwable(String message, Throwable cause)
  {
    this(message);
    this.cause = cause;
  }

  public Throwable(Throwable cause)
  {
	  this(cause == null ? null : cause.toString(), cause);
  }

  public String getMessage()
  {
    return detailMessage;
  }


  public String getLocalizedMessage()
  {
    return getMessage();
  }


  public Throwable getCause()
  {
    return cause == this ? null : cause;
  }


  public Throwable initCause(Throwable cause)
  {
    if (cause == this)
      throw new IllegalArgumentException();
    if (this.cause != this)
      throw new IllegalStateException();
    this.cause = cause;
    return this;
  }


/*
  public String toString()
  {
    String msg = getLocalizedMessage();
    return getClass().getName() + (msg == null ? "" : ": " + msg);
  }
*/

  public void printStackTrace()
  {
	  printStackTrace(System.err);
  }


  public void printStackTrace(PrintStream s)
  {
//    s.print(stackTraceString());
  }


  public Throwable fillInStackTrace()
  {
	  // TODO: do something useful here
	  return this;
  }
}
