/*
 * BranchTest2:
 * 
 */
package jvm;



public class BranchTest2  extends TestCase {

	
	public String getName() {
		return "BranchTest2";
	}
	
	public boolean test() {
		//temp: variable for avoiding Javac's static optimization when evaluating
		//boolean expressions
		boolean Ok=true,temp=false;
		//need variables, to avoid static optimizations
		int one=1, two=2, three=3; 
		
		//test if_icmp<cond>, true conditions
		//test eq
		temp=!(one==1);		//force Javac to insert the desired bytecode
		temp=!temp;
		Ok= Ok && temp;
		//test ne
		temp=!(one!=2);
		temp=!temp;
		Ok= Ok && temp;
		//test lt
		temp=!(one<2);
		temp=!temp;
		Ok= Ok && temp;
		//test gt
		temp=!(three>2);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of "<=", providing both possible true conditions
		//test le
		temp=!(one<=2);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(two<=2);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of ">=", providing both possible true conditions
		//test ge
		temp=!(two>=1);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(two>=2);
		temp=!temp;
		Ok= Ok && temp;
		
		//test if_icmp<cond>, false conditions
		
		Ok= Ok && !(one==2);
		Ok= Ok && !(one!=1);
		Ok= Ok && !(three<2);
		Ok= Ok && !(two>3);
		//here we have just one possible false condition for each le,ge
		Ok= Ok && !(two<=1);
		Ok= Ok && !(two>=3);
		return Ok;
		}

}