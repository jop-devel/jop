
package javax.realtime;


public interface RawRealAccessFactory {

  public RawMemoryName getName();

  public RawRealAccess newRawFloatAccess(long base, long size);
  /*throws java.lang.SecurityException,
         javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException,
         javax.realtime.MemoryTypeConflictException,
         java.lang.OutOfMemoryError;*/

}
