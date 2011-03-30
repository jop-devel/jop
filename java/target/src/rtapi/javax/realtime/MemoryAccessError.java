package javax.realtime;

// import java.io.Serializable;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

@SCJAllowed
public class MemoryAccessError
  extends RuntimeException // implements Serializable
{
  @Allocate({CURRENT})
  @BlockFree
  @SCJAllowed
  public MemoryAccessError() {}

  @Allocate({CURRENT})
  @BlockFree
  @MemoryAreaEncloses(inner = {"this"}, outer = {"msg"})
  @SCJAllowed
  public MemoryAccessError(String description)
  {
    super(description);
  }
}
