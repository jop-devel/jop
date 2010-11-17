package javax.realtime;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_1;

@SCJAllowed(LEVEL_1)
public class AsyncLongEvent extends AbstractAsyncEvent
{

  /**
   * fire this event, i.e., releases the execution of all handlers that
   * were added to this event.
   * 
   * @memory Does not allocate memory. Does not allow this to escape local variables.
   */
  @SCJAllowed(LEVEL_1)
  public void fire(long value)
  {
  }
}
