package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.safetycritical.annotate.SCJAllowed;


  /**
   * Construct a new object within the current memory area.
   *
   * @parameter deadline is an offset from the release time by which the release should finish. 
   *            A null deadline indicates that there is no deadline.
   * @parameter handler is the async event handler to be release if the associated schedulable object
   *            misses its deadline. A null parameter indicates that no handler should be release.
   */
@SCJAllowed(LEVEL_1)
public class AperiodicParameters extends ReleaseParameters
{
  @SCJAllowed(LEVEL_1)
  public AperiodicParameters(RelativeTime deadline, AsyncEventHandler missHandler)
  {
    super(null, null, null, null);
  }
}
