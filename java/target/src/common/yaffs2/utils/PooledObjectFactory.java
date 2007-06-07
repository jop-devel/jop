package yaffs2.utils;

public abstract class PooledObjectFactory
{
	protected static PooledObject first;

	public PooledObjectFactory(int initialCapacity)
	{
		PooledObject last = null;
		for (int i = 0; i < initialCapacity; i++)
		{
			 PooledObject o = createInstance();			 

			 o.next = last;

			 last = o;
		}
	}
		
	protected abstract PooledObject createInstance();
	
	public PooledObject get()
	{ 
		if (first == null)
			return createInstance();
		else
		{
			PooledObject result = first;
			first = first.next;
			return result;
		}
	}

	/**
	 * 
	 * @param o Might be null.
	 */
	public void put(PooledObject o)
	{
		if (o != null)
		{
			o.next = first;
			first = o;
		}
	}
}
