package jvm;

public class Logic3  extends TestCase{

	public String getName() {
		return "Logic3";
	}
	
	public boolean test() {	
		boolean Ok=true;
		int i,j; 
		i= 1073741828;
		j= -1073741824;
		
		//test branch on integer comparison
		//issue when  i-j>=2^31
		Ok= Ok && i>j;
		
		//The following should be useful to verify that the issue was corrected
		i++;
		Ok= Ok && i>j;
		
		i++;
		Ok= Ok && i>j;
		
		i++;
		Ok= Ok && i>j;
		
	   return Ok;
	}
}