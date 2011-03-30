package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed
public final class RawMemory
{

  @SCJAllowed
  public static final RawMemoryName DMA_ACCESS = new RawMemoryName(){};

  @SCJAllowed
  public static final RawMemoryName MEM_ACCESS = new RawMemoryName(){};

  @SCJAllowed
  public static final RawMemoryName IO_PORT_MAPPED = new RawMemoryName(){};

  @SCJAllowed
  public static final RawMemoryName IO_MEM_MAPPED = new RawMemoryName(){};

  @SCJAllowed
  public static RawIntegralAccess createRawIntegralInstance(RawMemoryName type,
                                                            long base,
                                                            long size)
  {
    return null;
  }
  

}