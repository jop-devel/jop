package jvm;

public class BranchTest1   extends TestCase {

	static class A {}
	static class B {}
	
	public String getName() {
		return "BranchTest1";
	}
	
	public boolean test() {
		A a,b,nullReference;
		
		boolean Ok=true;
		nullReference=new A();
		a=new A();
		b=new A();
		nullReference=null;
		
	
		Ok=Ok && !(a==b); //eq,false
		Ok=Ok &&  (a!=b); //eq,true
		a=b;
		Ok=Ok && (a==b); //ne,false
		Ok=Ok && !(a!=b);//ne,true
	
		//ifnull
		
		Ok=Ok && !(a==null);				//false cond
		Ok=Ok && (a!=null);					//true cond
		
		
		//ifnonull
		Ok=Ok && !(nullReference!=null); 	//false cond
		Ok=Ok && (nullReference==null); 	//true cond
		return Ok;
		}

}