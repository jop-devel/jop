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
	
	public int X;
	public int Y;
	public int Z;
	public int E;
	public int F;
	public int S;
	
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
		this.X = Integer.MIN_VALUE;
		this.Y = Integer.MIN_VALUE;
		this.Z = Integer.MIN_VALUE;
		this.E = Integer.MIN_VALUE;
		this.F = Integer.MIN_VALUE;
		this.S = Integer.MIN_VALUE;
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
}
