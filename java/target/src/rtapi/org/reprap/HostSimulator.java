package org.reprap;

public class HostSimulator 
{
	//private SerialPort SP = IOFactory.getFactory().getSerialPort();
	
	private static final char[][] commands = {{'G','1',' ','Y','1','0','0',' ','Z','2','0',' ','E','1','0',' ','F','7','0','0','\n'},
											  {'G','1',' ','X','1','0','0',' ','Z','0',' ','E','2','0',' ','F','2','0','0','\n'},
											  {'G','1',' ','Y','0',' ','Z','2','0',' ','E','3','0',' ','F','7','0','0','\n'},
											  {'G','1',' ','X','0',' ','Z','0',' ','E','4','0',' ','F','2','0','0','\n'},
											  {'G','9','2',' ','E','0','\n'}};
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
		System.out.print(character);
	}
	
	public char read()
	{
		if(pointer2 == commands[pointer1].length)
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
		return commands[pointer1][pointer2++];
	}
	

}
