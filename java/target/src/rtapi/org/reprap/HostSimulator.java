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

public class HostSimulator 
{
	//private SerialPort SP = IOFactory.getFactory().getSerialPort();
	
	private static final char[][] commands = {{'G','1',' ','Y','1','0','0',' ','Z','2','0',' ','E','1','0',' ','F','7','0','0','\n'},
											  {'G','1',' ','X','1','0','0',' ','Z','0',' ','E','2','0',' ','F','2','0','0','\n'},
											  {'G','1',' ','Y','0',' ','Z','2','0',' ','E','3','0',' ','F','7','0','0','\n'},
											  {'G','1',' ','X','0',' ','Z','0',' ','E','4','0',' ','F','2','0','0','\n'},
											  {'G','9','2',' ','E','0','\n'}};
	
	private static final char[] command = {'G','1',' ','Z','0','.','4','0','0',' ','F','7','2','0','.','0','0','0','\n',
		'G','1',' ','X','5','1','.','8','3','6',' ','Y','3','8','.','8','5','4','\n',
		'G','1',' ','F','1','8','0','0','.','0','0','0',' ','E','1','.','0','0','0','0','0','\n',
		'G','1',' ','X','4','0','.','4','3','1',' ','Y','3','8','.','8','6','7',' ','F','6','0','0','.','0','0','0',' ','E','1','.','3','3','5','3','5','\n',
		'G','1',' ','X','3','9','.','7','2','0',' ','Y','3','8','.','9','1','5',' ','E','1','.','3','5','6','2','9','\n',
		'G','1',' ','X','3','9','.','0','8','8',' ','Y','3','9','.','3','4','8',' ','E','1','.','3','7','8','8','4','\n',
		'G','1',' ','X','3','8','.','8','5','4',' ','Y','4','0','.','1','2','9',' ','E','1','.','4','0','2','8','2','\n',
		'G','1',' ','X','3','8','.','8','6','5',' ','Y','6','0','.','0','8','7',' ','E','1','.','9','8','9','6','9','\n',
		'G','1',' ','X','3','9','.','2','1','5',' ','Y','6','0','.','8','0','0',' ','E','2','.','0','1','3','0','5','\n',
		'G','1',' ','X','3','9','.','5','3','6',' ','Y','6','1','.','0','2','0',' ','E','2','.','0','2','4','4','9','\n',
		'G','1',' ','X','4','0','.','3','9','0',' ','Y','6','1','.','1','2','5',' ','E','2','.','0','4','9','7','7','\n',
		'G','1',' ','X','6','0','.','0','9','8',' ','Y','6','1','.','1','2','5',' ','E','2','.','6','2','9','3','2','\n',
		'G','1',' ','X','6','0','.','8','0','0',' ','Y','6','0','.','7','8','5',' ','E','2','.','6','5','2','2','5','\n',
		'G','1',' ','X','6','1','.','0','2','0',' ','Y','6','0','.','4','6','4',' ','E','2','.','6','6','3','7','0','\n',
		'G','1',' ','X','6','1','.','1','4','7',' ','Y','5','9','.','3','5','2',' ','E','2','.','6','9','6','6','0','\n',
		'G','1',' ','X','6','1','.','0','8','5',' ','Y','3','9','.','7','2','0',' ','E','3','.','2','7','3','8','7','\n',
		'G','1',' ','X','6','0','.','6','5','2',' ','Y','3','9','.','0','8','8',' ','E','3','.','2','9','6','4','2','\n',
		'G','1',' ','X','5','9','.','8','7','1',' ','Y','3','8','.','8','5','4',' ','E','3','.','3','2','0','4','0','\n',
		'G','1',' ','X','5','1','.','9','2','1',' ','Y','3','8','.','9','1','4',' ','E','3','.','5','5','4','1','7','\n',
		'G','1',' ','F','1','8','0','0','.','0','0','0',' ','E','2','.','5','5','4','1','7','\n',
		'G','9','2',' ','E','0',' ','Z','0','\n'};
	
	private int pointer1 = 0;
	private int pointer2 = 0;
	private int ok = 2;
	
	public boolean rxFull()
	{
		if(ok == 2)
		{
			return true;
		}
		return false;
	}
	
	public void write(char character)
	{
		if(ok == 0 && character == 'o')
		{
			ok++;
		}
		else if(ok == 1 && character == 'k')
		{
			ok++;
		}
		//System.out.print(character);
	}
	
	public char read()
	{
		/*if(pointer2 == commands[pointer1].length)
		{
			pointer2 = 0;
			pointer1++;
		}
		if(pointer1 == commands.length)
		{
			pointer1 = 0;
		}
		System.out.print(commands[pointer1][pointer2]);
		if(pointer2+1 == commands[pointer1].length)
		{
			ok = 0;
		}
		return commands[pointer1][pointer2++];*/
		
		if(pointer1 == command.length)
		{
			pointer1 = 0;
		}
		return command[pointer1++];
		
	}
	

}
