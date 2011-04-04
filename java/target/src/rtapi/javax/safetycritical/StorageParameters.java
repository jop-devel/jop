package javax.safetycritical;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * StorageParameters
 *
 */
@SCJAllowed
public class StorageParameters
{
  /**
   * Get the amount of backing store set aside for the
   * current schedulable object.
   * 
   * @return the total size
   */
  public static long backingStoreSize() { return 0; }

  /**
   * Get the amount of backing store consumed by the
   * current schedulable object.
   *
   * @return the amount consumed.
   */
  public static long backingStoreConsumed() { return 0; }

  /**
   * Get the amount of backing store available for use
   * by the current schedulable object.
   *
   * @return the amount remaining.
   */
  public static long backingStoreRemaining() { return 0; }
  /**
   * Get the amount of native stack size set asside for the
   * current schedulable object.
   * 
   * @return the total size
   */
  public static long nativeStackSize() { return 0; }

  /**
   * Get the amount of native stack size consumed by the
   * current schedulable object.
   *
   * @return the amount consumed.
   */
  public static long nativeStackConsumed() { return 0; }

  /**
   * Get the amount of native stack size available for use
   * by the current schedulable object.
   *
   * @return the amount remaining.
   */
  public static long nativeStackRemaining() { return 0; }
  /**
   * Get the amount of stack space for Java objects set aside for the
   * current schedulable object.
   * 
   * @return the total size
   */
  public static long javaStackSize() { return 0; }

  /**
   * Get the amount of stack space for Java objects consumed by the
   * current schedulable object.
   *
   * @return the amount consumed.
   */
  public static long javaStackConsumed() { return 0; }

  /**
   * Get the amount of stack space for Java objects available for use
   * by the current schedulable object.
   *
   * @return the amount remaining.
   */
  public static long javaStackRemaining() { return 0; }

  /**
   * Stack sizes for schedulable objects and sequencers. Passed as
   * parameter to the constructor of mission sequencers and
   * schedulable objects.
   *
   * TBD: kelvin changed nativeStack and javaStack to long.  Note that
   * getJavaStackSize() and getNativeStackSize() methods were already
   * declared to return long.  It seems that we have an implicit
   * assumption that memory sizes are represented by long.  do others
   * agree with this change?
   *
   * @param totalBackingStore size of the backing store reservation
   *        for worst-case scope usage in bytes
   * @param nativeStack size of native stack in bytes (vendor specific)
   * @param javaStack size of Java execution stack in bytes (vendor specific)
   */
  @SCJAllowed
  public StorageParameters(long totalBackingStore,
                           long nativeStack, long javaStack) {}
  
  /**
   * Stack sizes for schedulable objects and sequencers. Passed as
   * parameter to the constructor of mission sequencers and
   * schedulable objects. 
   *
   * TBD: kelvin changed nativeStack and javaStack to long.  Note that
   * getJavaStackSize() and getNativeStackSize() methods were already
   * declared to return long.  It seems that we have an implicit
   * assumption that memory sizes are represented by long.  do others
   * agree with this change?
   *
   * @param totalBackingStore size of the backing store reservation
   *        for worst-case scope usage in bytes 
   *
   * @param nativeStack size of native stack in bytes (vendor specific)
   *
   * @param javaStack size of Java execution stack in bytes (vendor specific)
   *
   * @param messageLength length of the space in bytes dedicated to
   *        message associated with this Schedulable object's
   *        ThrowBoundaryError exception plus all the method  
   *        names/identifiers in the stack backtrace
   *
   * @param stackTraceLength the number of byte for the
   *        StackTraceElement array dedicated to stack backtrace associated
   *        with this Schedulable object's ThrowBoundaryError exception. 
   */
  @SCJAllowed
  public StorageParameters(long totalBackingStore,
                           long nativeStackSize, long javaStackSize,
                           int messageLength,
                           int stackTraceLength) { }

  /**
   *
   * @return the size of the total backing store available for scoped
   *         memory areas created by the assocated SO.
   */
  @SCJAllowed
  public long getTotalBackingStoreSize() { return 0L; }
    
  /**
   *
   * @return the size of the native method stack available to the assocated SO.
   */
  @SCJAllowed
  public long getNativeStackSize() { return 0L; }
    
  /**
   *
   * @return the size of the Java stack available to the assocated SO.
   */ 
  @SCJAllowed
  public long getJavaStackSize() { return 0L; }
    
  /**
   * 
   * return the length of the message buffer
   */
  @SCJAllowed
  public int getMessageLength(){return 0; }

  /**
   * 
   * return the length of the stack trace buffer
   */
  @SCJAllowed
  public int getStackTraceLength() {return 0; }
}
