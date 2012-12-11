package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

/**
 * 
 * This class is used to define the maximum amount of memory that a schedulable
 * object requires in its default memory area (its per-release private scope
 * memory) and in immortal memory. The SCJ restricts this class relative to the
 * RTSJ such that values can be created but not queried or changed.
 * 
 * MS: Shall this class be here? It is not referenced at all. Need to be checked
 * with the spec...
 * 
 * @author martin
 * 
 */
@SCJAllowed
public class MemoryParameters // implements Cloneable
{
	/**
	 * Specifies no maximum limit.
	 */
	@SCJAllowed
	public static final long NO_MAX = -1;

	/**
	 * Create a MemoryParameters object with the given maximum values.
	 * 
	 * @param maxMemoryArea
	 *            is the maximum amount of memory in the per-release private
	 *            memory area.
	 * @param maxImmortal
	 *            is the maximum amount of memory in the immortal memory area
	 *            required by the associated schedulable object.
	 * @throws IllegalArgumentException
	 *             if any value other than positive. zero, or NO_MAX is passed
	 *             as the value of maxMemoryArea or maxImmortal.
	 */
	@SCJAllowed
	public MemoryParameters(long maxMemoryArea, long maxImmortal)
			throws IllegalArgumentException {
		if((maxMemoryArea < NO_MAX) | (maxImmortal < NO_MAX))
			throw new IllegalArgumentException();
	}

}
