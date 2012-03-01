/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
	Author: Tórur Biskopstø Strøm (torur.strom@gmail.com)
*/

package org.reprap;

public class CharacterBuffer
{
	private static final int POOL_SIZE = 10;
	public static final int BUFFER_WIDTH = 32;
	private static CharacterBuffer firstReady;
	private static CharacterBuffer lastReady;
	private static CharacterBuffer firstEmpty;
	private static CharacterBuffer lastEmpty;
	private static Object lock1 = new Object();
	private static Object lock2 = new Object();
	private static boolean initialized = initialize(); //Ensures that pool is created in immortal memory so that all PEH have access
	
	private CharacterBuffer next;
	private boolean empty;
	public char[] chars;
	public int length;
	
	private CharacterBuffer()
	{
		chars = new char[BUFFER_WIDTH];
		length = 0;
		empty = true;
	}
	
	private static boolean initialize()
	{
		CharacterBuffer current = new CharacterBuffer();
		firstEmpty = current;
		for(int i = 0; i < POOL_SIZE-1; i++)
		{
			CharacterBuffer temp = new CharacterBuffer();
			current.next = temp;
			current = temp;
		}
		lastEmpty = current;
		initialized = true;
		return true;
	}
	
	public void returnToPool()
	{
		if(empty)
		{
			synchronized (lock1) 
			{
				if(lastEmpty == null)
				{
					//Empty pool
					firstEmpty = this;
				}
				else
				{
					lastEmpty.next = this;
				}
				lastEmpty = this;
				next = null;
			}
			return;
		}
		synchronized (lock2) 
		{
			if(lastReady == null)
			{
				//Empty pool
				firstReady = this;
			}
			else
			{
				lastReady.next = this;
			}
			lastReady = this;
			next = null;
		}
	}
	
	public static CharacterBuffer getEmptyBuffer()
	{
		CharacterBuffer temp;
		synchronized (lock1) 
		{
			if(firstEmpty == null)
			{
				//Empty pool. This should not happen 
				System.out.println("No character buffers left");
				return null;
			}
			temp = firstEmpty;
			firstEmpty = temp.next;
			if(firstEmpty == null)
			{
				lastEmpty = null;
			}
		}
		temp.empty = false;
		temp.length = 0;
		return temp;
	}
	
	public static CharacterBuffer getReadyBuffer()
	{
		CharacterBuffer temp;
		synchronized (lock2) 
		{
			if(firstReady == null)
			{
				return null;
			}
			temp = firstReady;
			firstReady = temp.next;
			if(firstReady == null)
			{
				lastReady = null;
			}
		}
		temp.empty = true;
		return temp;
	}
}