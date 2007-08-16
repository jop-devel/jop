package gcinc;
public class SimpGC2 {

	
	public void createObjects() {
		int i;
		for(i=1;i<3*9730;i++) //make GC trigger 3 times (3*9730)
		{		
		allocateObject();
		}
		System.out.println(i);

	}

	public void allocateObject() {
		myObject mo;
		mo=new myObject();
	}

	public static void main(String[] args) {
		SimpGC2 sgc= new SimpGC2();
		sgc.createObjects();
		for(;;){}
	}

}
