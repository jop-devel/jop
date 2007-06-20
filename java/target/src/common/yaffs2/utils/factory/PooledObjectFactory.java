package yaffs2.utils.factory;

import yaffs2.utils.SerializableObject;


public abstract class PooledObjectFactory<PO extends PooledObject>
{
	protected PO first;

	public PooledObjectFactory()
	{
		this(0);
	}
	
	public PooledObjectFactory(int initialCapacity)
	{
		PO last = null;
		for (int i = 0; i < initialCapacity; i++)
		{
			 PO o = createInstance();			 

			 o.next = last;

			 last = o;
		}
	}
		
	protected abstract PO createInstance();
	
	@SuppressWarnings("unchecked")
	public PO get()
	{ 
		if (first == null)
			return createInstance();
		else
		{
			PO result = first;
			
			first = (PO)first.next;

			// XXX simulation only: we might want to clear it if it is written to disk 
			if (result instanceof SerializableObject)
				yaffs2.utils.Unix.memset((SerializableObject)result, (byte)0);
			
			return result;
		}
	}

	/**
	 * 
	 * @param o Might be null.
	 */
	public void put(PO o)
	{
		if (o != null)
		{
			o.next = first;
			first = o;
		}
	}
}
