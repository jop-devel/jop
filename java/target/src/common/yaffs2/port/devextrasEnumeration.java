package yaffs2.port;

public class devextrasEnumeration
{
	/**
	 * list_for_each	-	iterate over a list
	 * @pos:	the &struct list_head to use as a loop counter.
	 * @head:	the head for your list.
	 */
	// XXX #define list_for_each(pos, head) \
	//	for (pos = (head).next, prefetch(pos.next); pos != (head); \
	//        	pos = pos.next, prefetch(pos.next))
	public abstract class enumerate
	{
		public abstract void callback(list_head pos);
		
	public void list_for_each(list_head head)
	{
		list_head pos;
		for (pos = head.next/*, prefetch(pos.next)*/; pos != head;
			pos = pos.next/*, prefetch(pos.next)*/)
		{
			callback(pos);
		}
	}
	}
	
	/**
	 * list_for_each_safe	-	iterate over a list safe against removal
	 *                              of list entry
	 * @pos:	the &struct list_head to use as a loop counter.
	 * @n:		another &struct list_head to use as temporary storage
	 * @head:	the head for your list.
	 */
	// XXX #define list_for_each_safe(pos, n, head) \
	//	for (pos = (head).next, n = pos.next; pos != (head); \
	//		pos = n, n = pos.next)
}
