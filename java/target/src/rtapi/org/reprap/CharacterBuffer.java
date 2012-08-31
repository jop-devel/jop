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
		if(amount == 0 || amount > count)
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
	
	synchronized public boolean add(char[] characters)
	{
		if(chars.length >= count+characters.length)
		{
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
			return true;
		}
		return false;
	}
}
