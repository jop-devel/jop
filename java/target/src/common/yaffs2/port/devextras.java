package yaffs2.port;

public class devextras
{
	/*
	 * YAFFS: Yet another Flash File System . A NAND-flash specific file system. 
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU Lesser General Public License version 2.1 as
	 * published by the Free Software Foundation.
	 *
	 * Note: Only YAFFS headers are LGPL, YAFFS C code is covered by GPL.
	 */

	/*
	 * This file is just holds extra declarations used during development.
	 * Most of these are from kernel includes placed here so we can use them in 
	 * applications.
	 *
	 */

	/*#if defined WIN32
	#define __inline__ __inline
	#define new newHack
	#endif*/

	//#if !(defined __KERNEL__) || (defined WIN32)

	/* User space defines */

	/*typedef unsigned char __u8;
	typedef unsigned short __u16;
	typedef unsigned __u32;*/

	/*
	 * Simple doubly linked list implementation.
	 *
	 * Some of the internal functions ("__xxx") are useful when
	 * manipulating whole lists rather than single entries, as
	 * sometimes we already know the next/prev entries and we can
	 * generate better code by using them directly rather than
	 * using the generic single-entry routines.
	 */

	// #define prefetch(x) 1 // PORT Removed.
	
	// #define LIST_HEAD_INIT(name) { &(name), &(name) }
	static list_head LIST_HEAD_INIT(Object list_entry)
	{
		list_head result = new list_head(list_entry);
		INIT_LIST_HEAD(result);
		return result;
	}

	// #define LIST_HEAD(name) \
	//	struct list_head name = LIST_HEAD_INIT(name)
	// PORT list_head example = LIST_HEAD_INIT();

	static void INIT_LIST_HEAD(list_head ptr)
	{
		(ptr).next = (ptr); (ptr).prev = (ptr);
	}

	/*
	 * Insert a new entry between two known consecutive entries.
	 *
	 * This is only for internal list manipulation where we know
	 * the prev/next entries already!
	 */
	static /*__inline__*/ void __list_add(list_head _new,
					  list_head prev,
					  list_head next)
	{
		next.prev = _new;
		_new.next = next;
		_new.prev = prev;
		prev.next = _new;
	}

	/**
	 * list_add - add a new entry
	 * @new: new entry to be added
	 * @head: list head to add it after
	 *
	 * Insert a new entry after the specified head.
	 * This is good for implementing stacks.
	 */
	static /*__inline__*/ void list_add(list_head _new, list_head head)
	{
		__list_add(_new, head, head.next());
	}

	/**
	 * list_add_tail - add a new entry
	 * @new: new entry to be added
	 * @head: list head to add it before
	 *
	 * Insert a new entry before the specified head.
	 * This is useful for implementing queues.
	 */
	static /*__inline__*/ void list_add_tail(list_head _new,
					     list_head head)
	{
		__list_add(_new, head.prev(), head);
	}

	/*
	 * Delete a list entry by making the prev/next entries
	 * point to each other.
	 *
	 * This is only for internal list manipulation where we know
	 * the prev/next entries already!
	 */
	static /*__inline__*/ void __list_del(list_head prev,
					  list_head next)
	{
		next.prev = prev;
		prev.next = next;
	}

	/**
	 * list_del - deletes entry from list.
	 * @entry: the element to delete from the list.
	 * Note: list_empty on entry does not return true after this, the entry is
	 * in an undefined state.
	 */
	static /*__inline__*/ void list_del(list_head entry)
	{
		__list_del(entry.prev(), entry.next());
	}

	/**
	 * list_del_init - deletes entry from list and reinitialize it.
	 * @entry: the element to delete from the list.
	 */
	static /*__inline__*/ void list_del_init(list_head entry)
	{
		__list_del(entry.prev(), entry.next());
		INIT_LIST_HEAD(entry);
	}

	/**
	 * list_empty - tests whether a list is empty
	 * @head: the list to test.
	 */
	static /*__inline__*/ boolean list_empty(list_head head)
	{
		return head.next == head;
	}

	/**
	 * list_splice - join two lists
	 * @list: the new list to add.
	 * @head: the place to add it in the first list.
	 */
	static /*__inline__*/ void list_splice(list_head list,
					   list_head head)
	{
		list_head first = list.next();

		if (first != list) {
			list_head last = list.prev();
			list_head at = head.next();

			first.prev = head;
			head.next = first;

			last.next = at;
			at.prev = last;
		}
	}

	/**
	 * list_entry - get the struct for this entry
	 * @ptr:	the &struct list_head pointer.
	 * @type:	the type of the struct this is embedded in.
	 * @member:	the name of the list_struct within the struct.
	 */
	// #define list_entry(ptr, type, member) \
	//	((type *)((char *)(ptr)-(unsigned long)(&((type *)0).member)))
	// PORT Introduced list_entry field in list_head, which gives the same 
	// result as this macro.

	/**
	 * list_for_each	-	iterate over a list
	 * @pos:	the &struct list_head to use as a loop counter.
	 * @head:	the head for your list.
	 */
	// #define list_for_each(pos, head) \
	//	for (pos = (head).next, prefetch(pos.next); pos != (head); \
	//        	pos = pos.next, prefetch(pos.next))
	// PORT The corresponding code was inserted where this macro has been used. 
	// PORT prefetch() doesnt do anything, so it was omitted.

	
	/**
	 * list_for_each_safe	-	iterate over a list safe against removal
	 *                              of list entry
	 * @pos:	the &struct list_head to use as a loop counter.
	 * @n:		another &struct list_head to use as temporary storage
	 * @head:	the head for your list.
	 */
	// #define list_for_each_safe(pos, n, head) \
	//	for (pos = (head).next, n = pos.next; pos != (head); \
	//		pos = n, n = pos.next)
	// PORT The corresponding code was inserted where this macro has been used. 

	/*
	 * File types
	 */
	static final int DT_UNKNOWN	= 0;
	static final int DT_FIFO	=	1;
	static final int DT_CHR		= 2;
	static final int DT_DIR		= 4;
	static final int DT_BLK		= 6;
	static final int DT_REG		= 8;
	static final int DT_LNK		= 10;
	static final int DT_SOCK	= 12;
	static final int DT_WHT		= 14;

	/*#ifndef WIN32
	#include <sys/stat.h>
	#endif*/

	/*
	 * Attribute flags.  These should be or-ed together to figure out what
	 * has been changed!
	 */
	static final int ATTR_MODE =	1;
	static final int ATTR_UID =	2;
	static final int ATTR_GID =	4;
	static final int ATTR_SIZE =	8;
	static final int ATTR_ATIME =	16;
	static final int ATTR_MTIME	= 32;
	static final int ATTR_CTIME	= 64;
	static final int ATTR_ATIME_SET =	128;
	static final int ATTR_MTIME_SET	= 256;
	static final int ATTR_FORCE	= 512;	/* Not a change, but a change it */
	static final int ATTR_ATTR_FLAG	= 1024;


	/*#define KERN_DEBUG

	#else

	#ifndef WIN32
	#include <linux/types.h>
	#include <linux/list.h>
	#include <linux/fs.h>
	#include <linux/stat.h>
	#endif

	#endif

	#if defined WIN32
	#undef new
	#endif*/
}
