/*
  Copyright (C) 2012, Tórur Biskopstø Strøm (torur.strom@gmail.com)

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
package org.reprap;

public class CharacterBuffer 
{
	private char[] chars;
	private int count = 0;
	private int position = 0;
	
	public CharacterBuffer(int size) 
	{
		chars = new char[size];
	}
	
	//Creates new array but is only used by CommandParser.handleAsyncEvent
	synchronized public char[] getChars(int amount)
	{
		if(amount <= 0 || amount > count)
		{
			amount = count;
		}
		char[] newChars = new char[amount];
		int length = chars.length;
		int start;
		if(count > position)
		{
			start = length-(count-position);
		}
		else 
		{
			start = position-count;
		}
		count -= amount;
		int j = 0;
		if((start+amount) > length)
		{
			for (int i = start; i < length; i++) //@WCA loop=1
			{
				newChars[j++] = chars[i];
			}
			amount -= (length-start);
			start = 0;
		}
		for (int i = start; i < amount+start; i++) //@WCA loop=64
		{
			newChars[j++] = chars[i];
		}
		return newChars;
	}
	
	synchronized public boolean add(char character)
	{
		if(chars.length > count)
		{
			chars[position] = character;
			count++;
			position++;
			if(position == chars.length)
			{
				position = 0;
			}
			return true;
		}
		return false;
	}
	
	synchronized public void add(char[] characters)
	{
		if(characters == null || chars.length < count+characters.length)
		{
			return;
		}
		for(int i = 0; i < characters.length; i++) //@WCA loop=64
		{
			chars[position] = characters[i];
			position++;
			if(position == chars.length)
			{
				position = 0;
			}
		}
		count += characters.length;
	}
	
	synchronized public void add(char[] characters1,char[] characters2)
	{
		addUnSafe(characters1);
		addUnSafe(characters2);
	}
	
	synchronized public void add(char[] characters1,char[] characters2,char[] characters3)
	{
		addUnSafe(characters1);
		addUnSafe(characters2);
		addUnSafe(characters3);
	}
	
	synchronized public void add(char[] characters1,char[] characters2,char[] characters3, char[] characters4)
	{
		addUnSafe(characters1);
		addUnSafe(characters2);
		addUnSafe(characters3);
		addUnSafe(characters4);
	}
	
	synchronized public void add(char[] characters1,char[] characters2,char[] characters3, char[] characters4, char[] characters5)
	{
		addUnSafe(characters1);
		addUnSafe(characters2);
		addUnSafe(characters3);
		addUnSafe(characters4);
		addUnSafe(characters5);
	}
	
	private void addUnSafe(char[] characters)
	{
		if(characters == null || chars.length < count+characters.length)
		{
			return;
		}
		int length = characters.length;
		for(int i = 0; i < length; i++) //@WCA loop=64
		{
			chars[position] = characters[i];
			position++;
			if(position == chars.length)
			{
				position = 0;
			}
		}
		count += characters.length;
	}
}