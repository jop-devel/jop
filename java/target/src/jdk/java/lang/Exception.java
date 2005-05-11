
package java.lang;
public class Exception extends Throwable
{
  public Exception()
  {
  }

  public Exception(String s)
  {
    super(s);
  }

  public Exception(String s, Throwable cause)
  {
    super(s, cause);
  }

  public Exception(Throwable cause)
  {
    super(cause);
  }
}
