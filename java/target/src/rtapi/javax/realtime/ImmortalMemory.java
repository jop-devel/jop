package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

@SCJAllowed
public final class ImmortalMemory extends MemoryArea
{

	private static ImmortalMemory instance;
	private static long IMMORTAL_MEMORY_SIZE = 200;

	private ImmortalMemory(long size) 
	{
		super(size);
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public static ImmortalMemory instance()
	{
		if(instance == null)
		{
			instance = new ImmortalMemory(IMMORTAL_MEMORY_SIZE);
		}
		return instance;
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public void enter(Runnable logic)
	{

	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryConsumed()
	{
		return 0L; // dummy return
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long memoryRemaining()
	{
		return 0L; // dummy return
	}

	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public long size()
	{
		return 0L; // dummy return
	}
}
