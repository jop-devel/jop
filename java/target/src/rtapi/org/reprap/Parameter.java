package org.reprap;

public class Parameter 
{
	
	public int X;
	public int Y;
	public int Z;
	public int E;
	public int F;
	public int S;
	
	public Parameter(int X, int Y, int Z, int E, int F ,int S)
	{
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.E = E;
		this.F = F;
		this.S = S;
	}
	
	public Parameter()
	{
		this.X = 0;
		this.Y = 0;
		this.Z = 0;
		this.E = 0;
		this.F = 0;
		this.S = 0;
	}
	
}
