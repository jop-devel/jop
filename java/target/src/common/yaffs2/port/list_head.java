package yaffs2.port;

import yaffs2.utils.*;

public class list_head implements list_head_or_yaffs_Object
{
	
	/**
	 * PORT Since we can't do pointer arithmetic, we have to save the object the list_head belongs to.
	 * @param owner
	 */
	public list_head(Object owner)	// XXX check later if it should be reset somewhere (not in memset()!)
	{
		this.list_entry = owner;
	}
	
	/**
	 * PORT Since we can't do pointer arithmetic, we have to save the object it belongs to.
	 */
	public Object list_entry;
	
	//struct list_head {
		public list_head_or_yaffs_Object next, prev;
		public list_head next()	// PORT typecasts will always succeed(?)
		{
			return (list_head)next;
		}
		public list_head prev()	// PORT typecasts will always succeed(?)
		{
			return (list_head)prev;
		}
	//};
}
