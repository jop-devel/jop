package gcinc;
import com.jopdesign.sys.GC;
public class SimpGC6  {

	private SimpGC6 reference;
	public void setReference(SimpGC6 simp){
	reference=simp;
	
	}
	
	
	public void createChainedObjects(){
		SimpGC6 ref1,ref2;
		ref1=new SimpGC6();
		ref2=new SimpGC6();
		ref1.setReference(ref2);
		ref2.setReference(ref1);
	}
	
	
	public static void main(String[] args) {
		
		SimpGC6 sgc=new SimpGC6();
		
		//Start our experiment with a clean heap
		System.out.println("call GC1");
		GC.gc();
		
		//Measure how many objects fit into memory-make GC trigger
		for(int i=0; i<9730; i++){
			new myObject();
			System.out.print("nc:");
			System.out.print(i);
			System.out.print(" ");
		}
		//Clean objects remaining from last for
		System.out.println("call GC2");
		GC.gc();		
		
		//Create chained objects
		for(int i=0; i<5000; i++){
		sgc.createChainedObjects();
		}
		//Clean them
		System.out.println("call GC3");
		GC.gc();	
		
		//If the objects were cleaned GC should trigger at the same point as
		//in the first loop
		for(int i=0; i<9730; i++){
			new myObject();
			System.out.print("sc:");
			System.out.print(i);
		}
		
		
	}

}