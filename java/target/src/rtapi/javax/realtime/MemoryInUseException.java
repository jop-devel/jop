package javax.realtime;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Allocate.Area.IMMORTAL;

@SCJAllowed
public class MemoryInUseException extends RuntimeException
{
  @BlockFree
  @SCJAllowed
  public MemoryInUseException() {}

  @Allocate({IMMORTAL})
  @BlockFree
  @SCJAllowed
  public MemoryInUseException(String description) { super(description); }
}
