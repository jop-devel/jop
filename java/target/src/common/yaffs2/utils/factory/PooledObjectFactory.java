package yaffs2.utils.factory;

import yaffs2.utils.SerializableObject;


public abstract class PooledObjectFactory
{
	protected PooledObject first;

	public PooledObjectFactory()
	{
		this(0);
	}
	
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
			
			first = (PooledObject)first.next;

			// XXX simulation only: we might want to clear it if it is written to disk 
			// XXX but then we should also clear other objects
			if (result instanceof SerializableObject)
				yaffs2.utils.Unix.memset((SerializableObject)result, (byte)0);
			
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
