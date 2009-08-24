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

public class Switch2  extends TestCase{

	public String toString() {
		return "Switch2";
	}
	
	public boolean test() {	
		boolean Ok=true;
		int i = 0;
	   
		//table switch
		//-2147483648, avoid overflow
	   for(i=0;i<(2^10);i++)
	   {
		   	switch(i){
		   	case -8 : Ok=Ok && i==-8; break;
	   		case -7 : Ok=Ok && i==-7; break; 
	   		case -6 : Ok=Ok && i==-6; break; 
	   		case -5 : Ok=Ok && i==-5; break;
	   		case -4 : Ok=Ok && i==-4; break;
	   		case -3 : Ok=Ok && i==-3; break;
	   		case -2 : Ok=Ok && i==-2; break;
	   		case -1 : Ok=Ok && i==-1; break;
	   		case 0 : Ok=Ok && i==0; break;
	   		case 1 : Ok=Ok && i==1; break; 
	   		case 2 : Ok=Ok && i==2; break; 
	   		case 3 : Ok=Ok && i==3; break;
	   		case 4 : Ok=Ok && i==4; break;
	   		case 5 : Ok=Ok && i==5; break;
	   		case 6 : Ok=Ok && i==6; break;
	   		case 7 : Ok=Ok && i==7; break;
	   		default: Ok=Ok && (i>7 | i<-8); 
		   	}
	   }
	   //check that the loop executed completely
	   Ok=Ok && i==(2^10);
	   	   
	   //lookup switch
	   //-2147483648, avoid overflow
	   for(i=0;i<(2^10);i++)
	   {
		  
		   	switch(i){
		   	/*case -8 : Ok=Ok && i==-8; break;
	   		case -7 : Ok=Ok && i==-7; break; 
	   		case -6 : Ok=Ok && i==-6; break; 
	   		case -5 : Ok=Ok && i==-5; break;
	   		case -4 : Ok=Ok && i==-4; break;
	   		case -3 : Ok=Ok && i==-3; break;
	   		case -2 : Ok=Ok && i==-2; break;*/
	   		case -1 : Ok=Ok && i==-1; break;
	   		case 0 : Ok=Ok && i==0; break;
	   		case 1 : Ok=Ok && i==1; break; 
	   		case 2 : Ok=Ok && i==2; break; 
	   		case 3 : Ok=Ok && i==3; break;
	   		case 4 : Ok=Ok && i==4; break;
	   		case 5 : Ok=Ok && i==5; break;
	   		case 6 : Ok=Ok && i==6; break;
	   		case 7 : Ok=Ok && i==7; break;
	   		case 155 : Ok=Ok && i==155; break;
	   		//cuando el case para el 7 no está, no pasa por acá en JOP
	   		default: Ok=Ok && (i>7 | i<-8); 
		   	}
	   }
	   //check that the loop executed completely
	   Ok=Ok && i==(2^10);
	   return Ok;
	}
}
