package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

/**
 * MS: Shall this class be here? It is not referenced at all.
 * Need to be checked with the spec...
 * @author martin
 *
 */
@SCJAllowed
public class MemoryParameters // implements Cloneable
{
	@SCJAllowed

  public static final long NO_MAX = -1;

	@SCJAllowed
  public MemoryParameters(long maxMemoryArea, long maxImmortal)
    throws IllegalArgumentException
  {
  }

}
