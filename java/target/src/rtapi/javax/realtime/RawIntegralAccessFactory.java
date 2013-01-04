package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * An interface that describes factory classes that create the accessor objects
 * for raw memory access.
 * 
 */
@SCJAllowed(LEVEL_0)
public interface RawIntegralAccessFactory {

	/**
	 * 
	 * @return a reference to an object that implements the RawMemoryName
	 *         interface. This “name” is associated with this factory and
	 *         indirectly with all the objects created by this factory
	 */
	@SCJAllowed(LEVEL_0)
	public RawMemoryName getName();

	/**
	 * Creates an accessor object for accessing a byte in raw memory.
	 * 
	 * Throws AlignmentError if the offset is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the byte falls in an invalid address
	 * range. Throws MemoryTypeConflictException if offset does not point to
	 * memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the offset is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param offset
	 * @return an object implementing the RawByte interface.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByte newRawByte(long offset);

	/**
	 * Creates an accessor object for accessing a byte array in raw memory.
	 * 
	 * Throws AlignmentError if the base is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the byte array falls in an invalid
	 * address range. Throws MemoryTypeConflictException if base does not point
	 * to memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the base is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param base
	 * @param entries
	 * @return an object implementing the RawByteArray interface.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteArray newRawByteArray(long base, int entries);
	
	/**
	 * Creates an accessor object for read accessing a byte array in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteArrayRead newRawByteArrayRead(long base, int entries);
	
	/**
	 * Creates an accessor object for write accessing a byte array in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawByteArrayWrite newRawByteArrayWrite(long base,int entries);
	
	/**
	 * Creates an accessor object for read accessing a byte in raw memory.
	 */
	@SCJAllowed(javax.safetycritical.annotate.Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawByteRead newRawByteRead(long offset);
	
	/**
	 * Creates an accessor object for write accessing a byte in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawByteWrite newRawByteWrite(long offset);
	
	/**
	 * Creates an accessor object for accessing an int in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawInt newRawInt(long offset);
	
	/**
	 * Creates an accessor object for accessing an int in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntRead newRawIntRead(long offset);

	/**
	 * Creates an accessor object for accessing an int in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntWrite newRawIntWrite(long offset);

	/**
	 * Creates an accessor object for accessing a int array in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawIntArray newRawIntArray(long base, int entries);
	
	/**
	 * Creates an accessor object for read accessing a int array in raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawIntArrayRead newRawIntArrayRead(long base, int entries);
	
	/**
	 * Creates an accessor object for write accessing a int array in raw memory.
	 */
	@SCJAllowed(javax.safetycritical.annotate.Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public javax.realtime.RawIntArrayWrite newRawIntArrayWrite(long base,	int entries);
	
//	@SCJAllowed(LEVEL_0)
//	public RawIntegralAccess newIntegralAccess(long base, long size);
	/*
	 * throws java.lang.SecurityException,
	 * javax.realtime.OffsetOutOfBoundsException,
	 * javax.realtime.SizeOutOfBoundsException,
	 * javax.realtime.MemoryTypeConflictException, java.lang.OutOfMemoryError;
	 */

}
