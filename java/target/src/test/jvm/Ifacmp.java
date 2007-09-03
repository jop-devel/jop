package jvm;

public class Ifacmp extends TestCase {

	static class A {}
	static class B {}
	
	public String getName() {
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