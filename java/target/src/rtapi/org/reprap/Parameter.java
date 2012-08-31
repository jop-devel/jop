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

public class Parameter 
{
	
	int X;
	int Y;
	int Z;
	int E;
	int F;
	int S;
	
	public Parameter(int x, int y, int z, int e, int f ,int s)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.E = e;
		this.F = f;
		this.S = s;
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
	
	synchronized public Parameter clone()
	{
		return new Parameter(this.X,this.Y,this.Z,this.E,this.F,this.S);
	}
	
	synchronized public void copy(Parameter source)
	{
		X = source.X;
		Y = source.Y;
		Z = source.Z;
		E = source.E;
		F = source.F;
		S = source.S;
	}
	
	synchronized public void set(int x, int y, int z, int e, int f ,int s)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.E = e;
		this.F = f;
		this.S = s;
	}
}
