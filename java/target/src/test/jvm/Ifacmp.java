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

package jvm;

public class Ifacmp extends TestCase {

	static class A {}
	static class B {}
	
	public String toString() {
		return "Ifacmp";
	}
	
	public boolean test() {
		boolean Ok=true;
		A a,b;
		//B c,d;
		a=new A();
		b=new A();
		//c=new B();
		//d=new B();
		
		//test equal and nonequal with false,true
		if(a==b){
			Ok=false;
			}
		else
			{
			/*Ok stays the same*/			
			}
				
		if(a!=b){
			/*Ok stays the same*/
			}
		else
			{
			
			Ok=false;
			}
		//test equal and nonequal with true,false
		a=b;
		
		if(a==b){
			/*Ok stays the same*/
			}
		else
			{
			Ok=false;			
			}
				
		if(a!=b){
			Ok=false;
			}
		else
			{
			/*Ok stays the same*/
			
			}
		
		return Ok;
		
		
		
		}

}