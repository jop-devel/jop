/*
 * BranchTest3:test branch on comparison with zero.
 * 
 */
package jvm;



public class BranchTest3  extends TestCase {

	
	public String getName() {
		return "BranchTest3";
	}
	
	public boolean test() {
		//temp: variable for avoiding Javac's static optimization when evaluating
		//boolean expressions
		boolean Ok=true,temp=false;
		//need variables, to avoid static optimizations
		int zero=0, negative=-1, two=2, three=3; 
				
		//test eq
		temp=!(zero==0);		//force Javac to insert the desired bytecode
		temp=!temp;
		Ok= Ok && temp;
		//test ne
		temp=!(two!=0);
		temp=!temp;
		Ok= Ok && temp;
		//test lt
		temp=!(negative<0);
		temp=!temp;
		Ok= Ok && temp;
		//test gt
		temp=!(two>0);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of "<=", providing both possible true conditions
		//test le
		temp=!(negative<=0);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(zero<=0);
		temp=!temp;
		Ok= Ok && temp;
		
		//we split in two the test of ">=", providing both possible true conditions
		//test ge
		temp=!(two>=0);
		temp=!temp;
		Ok= Ok && temp;
		
		temp=!(zero>=0);
		temp=!temp;
		Ok= Ok && temp;
		
		//test if_icmp<cond>, false conditions
		
		Ok= Ok && !(two==0);
		Ok= Ok && !(zero!=0);
		Ok= Ok && !(three<0);
		Ok= Ok && !(negative>0);
		//here we have just one possible false condition for each le,ge
		Ok= Ok && !(two<=0);
		Ok= Ok && !(negative>=0);
		return Ok;
		}

}