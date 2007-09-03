/* ArrayTest2: test arrays of references.
 * Bytecodes exercised: 
 * 
 * 
 */
package jvm;
public class ArrayTest2  extends TestCase {

	
	static interface X extends Y {}
	static interface Y {public int getInt();
							}
	
	static class A implements X{	private int i;
									public int getInt(){
									return i;
										}
									}
	static class B extends A {}
									/*implements X {
									private int i;
									public int getInt(){
									return i;
										}
									}*/
	static class C implements X {	private int i;
									public int getInt(){
									return i;
										}
									}
	
	
	public String getName() {
		return "ArrayTest2";
	}
	
	public boolean test() {
		boolean Ok=true;
		A[] a = new A[10]; //create an array of classes
		B[] b = new B[10];
		C[] c = new C[10];
		X[] x = new X[10]; //create an array of interfaces
		Y[] y = new Y[10];
		
		Object[] o= new Object[10];
		//Check Correct Array Initialization
		for(int i=0; i<10;i++)
			{
				Ok=Ok && a[i]==null;
				Ok=Ok && b[i]==null;
				Ok=Ok && c[i]==null;
				Ok=Ok && x[i]==null;
				Ok=Ok && y[i]==null;
				Ok=Ok && o[i]==null;
				
			}
		//Check for length (bytecode:arraylength)
		Ok=Ok&& a.length==10;
		//Exercise all possible cases of aastore 
		a[0]=new A(); //same class
		a[1]=new B(); //subclass
		
		x[0]=new C(); //an implementor of the interface
		y[0]=new C(); //an implementor of a subinterface
		o[0]=new A();
		//for the checking
		b[0]=new B();
		
		
		
		Object p[]=new Object[10];
		p[0]=a; 	//an array reference
		
		a[0]=a[1];	//same class
		a[1]=b[0];	//subclass
		x[1]=a[2];	//an implementor
		o[1]=x[3];
		x[2]=c[2];
		y[1]=c[1];
		
		//Check
		
		Ok= Ok && a[0].getInt()==0;
		Ok= Ok && a[1].getInt()==0;
		Ok= Ok && x[0].getInt()==0;
		Ok= Ok && y[0].getInt()==0;
		Ok= Ok && ((A)o[0]).getInt()==0;
		//possible issue related to checkcast
		//Ok= Ok && ((A[])p[0]).length==10;
		
				
		return Ok;
	}

}
