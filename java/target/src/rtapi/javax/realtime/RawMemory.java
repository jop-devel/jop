package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * This class is the hub of a system that constructs special-purpose objects
 * that access particular types and ranges of raw memory. This facility is
 * supported by the registerRawIntegralAccessFactory and the create methods.
 * Four rawintegral- access factories are supported: two for accessing memory
 * (called IO_PORT_MAPPED and IO_MEMORY_MAPPED), one for accessing memory that
 * can be used for DMA (called DMA_ACCESS) and the other for accesses to the
 * memory (called MEM_ACCESS).
 * 
 */
@SCJAllowed
public final class RawMemory {

	/**
	 * The name indicating an area of raw memory which is accessable for DMA
	 * transfer.
	 */
	@SCJAllowed(LEVEL_0)
	public static final RawMemoryName DMA_ACCESS = new RawMemoryName() {
	};

	/**
	 * The name indicating an area of raw memory which is used to access memory
	 * mapped IO registers.
	 */
	@SCJAllowed(LEVEL_0)
	public static final RawMemoryName IO_MEM_MAPPED = new RawMemoryName() {
	};

	/**
	 * The name indicating an area of raw memory which is used as port-mapped IO
	 * registers.
	 */
	@SCJAllowed(LEVEL_0)
	public static final RawMemoryName IO_PORT_MAPPED = new RawMemoryName() {
	};

	/**
	 * The name indicating an area of raw memory.
	 */
	@SCJAllowed(LEVEL_0)
	public static final RawMemoryName MEM_ACCESS = new RawMemoryName() {
	};

	/**
	 * An array to hold the registered factories with four possible types of factories:
	 * 	1. RawFactoryRegister[0] = DMA_ACCESS
	 * 	2. RawFactoryRegister[1] = IO_MEM_MAPPED
	 *  3. RawFactoryRegister[2] = IO_PORT_MAPPED
	 *	4. RawFactoryRegister[3] = MEM_ACCESS;
	 */
	private static RawIntegralAccessFactory[] RawFactoryRegister = new RawIntegralAccessFactory[4];

	@SCJAllowed(LEVEL_0)
	public RawMemory() {

	}

	/**
	 * Creates or finds an accessor object for accessing a byte array in raw
	 * memory.
	 * 
	 * Throws AlignmentError if the base is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the byte array falls in an invalid
	 * address range. Throws MemoryTypeConflictException if base does not point
	 * to memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the base is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param type
	 *            The required type of memory.
	 * @param base
	 *            The offset of the required array.
	 * @param size
	 *            The length of the array.
	 * 
	 * @return An accessor object from the raw memory access.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByteArray createRawByteArrayInstance(RawMemoryName type,
			long base, long size) {
			
		return RawFactoryRegister[getPosition(type)].newRawByteArray(base, (int) size);
		
	}

	/**
	 * Creates or finds an accessor object for accessing a byte array in raw
	 * memory.
	 * 
	 * Throws AlignmentError if the base is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the byte array falls in an invalid
	 * address range. Throws MemoryTypeConflictException if base does not point
	 * to memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the base is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param type
	 *            is the required type of memory.
	 * @param base
	 *            is the offset of the required array.
	 * @param size
	 *            is the length of the array.
	 * @return an accessor object from the raw memory access.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByteArrayRead createRawByteArrayReadInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	/**
	 * Creates or finds an accessor object for accessing a byte array in raw
	 * memory.
	 * 
	 * Throws AlignmentError if the base is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the byte array falls in an invalid
	 * address range. Throws MemoryTypeConflictException if base does not point
	 * to memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the base is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param type
	 *            is the required type of memory.
	 * @param base
	 *            is the offset of the required array.
	 * @param size
	 *            is the length of the array.
	 * @return an accessor object from the raw memory access.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByteArrayWrite createRawByteArrayWriteInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	/**
	 * Creates or finds an accessor object for accessing a byte of raw memory.
	 * 
	 * Throws AlignmentError if the base is not on the appropriate boundary.
	 * Throws SizeOutOfBoundsException if the long array falls in an invalid
	 * address range. Throws MemoryTypeConflictException if base does not point
	 * to memory that matches the type served by this factory. Throws
	 * OffsetOutOfBoundsException if the base is negative or greater than the
	 * size of the raw memory area.
	 * 
	 * @param type
	 *            is the required type of memory.
	 * @param base
	 *            is the offset of the required byte.
	 * @return an accessor object from the raw memory access.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByte createRawByteInstance(RawMemoryName type, long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByteRead createRawByteReadInstance(RawMemoryName type,
			long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawByteWrite createRawByteWriteInstance(RawMemoryName type,
			long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawIntArray createRawIntArrayInstance(RawMemoryName type,
			long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawIntArrayRead createRawIntArrayReadInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawIntArrayWrite createRawIntArrayWriteInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawInt createRawIntInstance(RawMemoryName type, long base) {

		return RawFactoryRegister[getPosition(type)].newRawInt(base);

	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawIntRead createRawIntReadInstance(RawMemoryName type,
			long base) {

		return RawFactoryRegister[getPosition(type)].newRawIntRead(base);

	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawIntWrite createRawIntWriteInstance(RawMemoryName type,
			long base) {

		int position = getPosition(type);
		return RawFactoryRegister[position].newRawIntWrite(base);

	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLongArray createRawLongArrayInstance(RawMemoryName type,
			long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLongArrayRead createRawLongArrayReadInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLongArrayWrite createRawLongArrayWriteInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLong createRawLongInstance(RawMemoryName type, long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLongRead createRawLongReadInstance(RawMemoryName type,
			long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawLongWrite createRawLongWriteInstance(RawMemoryName type,
			long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShortArray createRawShortArrayInstance(RawMemoryName type,
			long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShortArrayRead createRawShortArrayReadInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShortArrayWrite createRawShortArrayWriteInstance(
			RawMemoryName type, long base, long size) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShort createRawShortInstance(RawMemoryName type, long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShortRead createRawShortReadInstance(RawMemoryName type,
			long base) {
		return null;
	}

	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static RawShortWrite createRawShortWriteInstance(RawMemoryName type,
			long base) {
		return null;
	}

	/**
	 * Registers a factory for accessing raw memory. Throws
	 * IllegalArgumentException if factory is null or its name is served by a
	 * factory that has already been registered.
	 * 
	 * @param factory
	 *            is the factory being registered.
	 * @throws IllegalArgumentException
	 *             if factory is null or its name is served by a factory that
	 *             has already been registered.
	 */
	@SCJAllowed(LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public static void registerAccessFactory(RawIntegralAccessFactory factory)
			throws IllegalArgumentException {

		if (factory == null) {
			throw new IllegalArgumentException();
		}

		int position = getPosition(factory);

		if (RawFactoryRegister[position] == null) {
			RawFactoryRegister[position] = factory;
		} else {
			throw new IllegalArgumentException();
		}

	}

	/**
	 * Not part of spec, implementation specific
	 */
	static int getPosition(RawIntegralAccessFactory factory) {
		if (factory.getName() == RawMemory.DMA_ACCESS) {
			return 0;
		}

		if (factory.getName() == RawMemory.IO_MEM_MAPPED) {
			return 1;
		}

		if (factory.getName() == RawMemory.IO_PORT_MAPPED) {
			return 2;
		}

		if (factory.getName() == RawMemory.MEM_ACCESS) {
			return 3;
		}

		return 0;

	}

	static int getPosition(RawMemoryName name) {
		if (name == RawMemory.DMA_ACCESS) {
			return 0;
		}

		if (name == RawMemory.IO_MEM_MAPPED) {
			return 1;
		}

		if (name == RawMemory.IO_PORT_MAPPED) {
			return 2;
		}

		if (name == RawMemory.MEM_ACCESS) {
			return 3;
		}

		return 0;

	}
	

	// @SCJAllowed(LEVEL_0)
	// public static RawIntegralAccess createRawIntegralInstance(
	// RawMemoryName type, long base, long size) {
	// return null;
	// }

}