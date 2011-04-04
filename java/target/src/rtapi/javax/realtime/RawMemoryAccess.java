package javax.realtime;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_0;

@SCJAllowed(LEVEL_0)
public class RawMemoryAccess implements RawIntegralAccess {

  @SCJAllowed(LEVEL_0)
  public static final RawMemoryName IO_ACCESS = null;
  
  @SCJAllowed(LEVEL_0)
  public static final RawMemoryName MEM_ACCESS = null;
  
  @SCJAllowed(LEVEL_0)
  public RawMemoryAccess(PhysicalMemoryName type, long size)
  /*       throws java.lang.SecurityException,
                javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException,
                javax.realtime.UnsupportedPhysicalMemoryException */
                {
                };
       
  @SCJAllowed(LEVEL_0)         
  public RawMemoryAccess(PhysicalMemoryName type, long base, long size)
   /*      throws java.lang.SecurityException,
                javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException,
                javax.realtime.UnsupportedPhysicalMemoryException */
                {
                };
    
  @SCJAllowed(LEVEL_0)            
  public static RawIntegralAccess createRmaInstance(RawMemoryName type,
                long base, long size)
   /*      throws java.lang.InstantiationException,
                java.lang.IllegalAccessException,
                java.lang.reflect.InvocationTargetException */
                { return null; }
   
  @SCJAllowed(LEVEL_0)             
  public byte getByte(long offset)
    /*     throws javax.realtime.OffsetOutOfBoundsException,
               javax.realtime.SizeOutOfBoundsException*/
                { return 0; };
  
  @SCJAllowed(LEVEL_0)
  public void getBytes(long offset, byte[] bytes, int low, int number)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public int getInt(long offset)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException*/
                { return 1; };
                
  @SCJAllowed(LEVEL_0)
  public void getInts(long offset, int[] ints, int low, int number)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public long getLong(long offset)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException*/
                { return 0L; };
  
  @SCJAllowed(LEVEL_0)
  public void getLongs(long offset, long[] longs, int low, int number)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};

  @SCJAllowed(LEVEL_0)
  public short getShort(long offset)
 /*        throws javax.realtime.OffsetOutOfBoundsException,
                javax.realtime.SizeOutOfBoundsException*/
                { return 0; };
  
  @SCJAllowed(LEVEL_0)
  public void getShorts(long offset, short[] shorts, int low, int number)
/*         throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)   
  public void setByte(long offset, byte value)
/*         throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public void setBytes(long offset, byte[] bytes, int low, int number)
/*         throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};

  @SCJAllowed(LEVEL_0)
  public void setInt(long offset, int value)
/*         throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
   
  @SCJAllowed(LEVEL_0)      
  public void setInts(long offset, int[] its, int low, int number)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public void setByte(long offset, long value)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
  
  @SCJAllowed(LEVEL_0)   
  public void setLongs(long offset, long[] longs, int low, int number)
 /*        throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public void setShort(long offset, short value)
  /*       throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException*/
         {};
         
  @SCJAllowed(LEVEL_0)
  public void setShorts(long offset, short[] shorts, int low, int number)
 /*        throws javax.realtime.OffsetOutOfBoundsException,
         javax.realtime.SizeOutOfBoundsException */
         {};
}
