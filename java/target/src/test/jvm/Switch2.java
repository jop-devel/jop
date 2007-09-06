package jvm;

public class Switch2  extends TestCase{

	public String getName() {
		return "Switch2";
	}
	
	public boolean test() {	
		boolean Ok=true;
		int i = 0;
	   
	   //table switch
	   for(i=-2147483648;i<2147483647;i++)
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
	   
	   //lookupswitch TAKE A LOOK HERE. .
	   
	   for(i=-2147483648;i<2147483647;i++)
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
	   		case 155 : Ok=Ok && i==155; break;
	   		default: Ok=Ok && (i>7 | i<-8); 
		   	}
	   }
	   
	   
	
	   return Ok;
	}
}
