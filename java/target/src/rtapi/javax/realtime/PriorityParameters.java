package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed
public class PriorityParameters extends SchedulingParameters
{
  @BlockFree
  @SCJAllowed
  public PriorityParameters(int priority) {
  }
  
  @BlockFree
  @SCJAllowed
  public int getPriority() {
    return -1; // skeleton
  }

  // not scj allowed
  public void setPriority(int priority) throws IllegalArgumentException {
  }
}
