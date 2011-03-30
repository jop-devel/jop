package javax.realtime;


import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Allocate.Area.IMMORTAL;

@SCJAllowed
public class MemoryScopeException extends RuntimeException
{
  @BlockFree
  @SCJAllowed
  public MemoryScopeException() {}

  @Allocate({IMMORTAL})
  @BlockFree
  @SCJAllowed
  public MemoryScopeException(String description) { super(description); }
}
