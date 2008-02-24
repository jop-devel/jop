/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package gcinc;
public class typeB extends testObject{
	
	
	public boolean testYourself(int i){
		boolean isOk;	
		isOk= (myInt1/i)/i==i && (myInt2-i)/4==i && (((myInt3-6)/i)-5)/(4)==i;  
		return isOk;	
	}
	
	public typeB(int i){
		myInt1=i*i*i;
		myInt2=4*i+i;
		myInt3=4*i*i+5*i+6;
		
	}
		
}
