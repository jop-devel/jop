package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Level.LEVEL_0;

@SCJAllowed(LEVEL_0)
public interface RawIntegralAccessFactory {

  @SCJAllowed(LEVEL_0)
  public RawMemoryName getName();

  @SCJAllowed(LEVEL_0)
  public RawIntegralAccess newIntegralAccess(long base, long size);
   /*      throws java.lang.SecurityException,
                javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException,
                javax.realtime.MemoryTypeConflictException,
                java.lang.OutOfMemoryError;*/

}
