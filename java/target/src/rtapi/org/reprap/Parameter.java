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
