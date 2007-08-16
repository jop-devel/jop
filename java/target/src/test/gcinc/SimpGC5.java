
package gcinc;
import com.jopdesign.sys.GC;
public class SimpGC5 {

	public static void main(String[] args) {
		
		//Warning: required space grows with sum(2expn), n=0,1,2 ...
		
		SimpleTree st= new SimpleTree(8);
		
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		
		//Make this a couple of times 
		st=st.getLeftSubtree();
		st=st.getRightSubtree();
		st=st.getLeftSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		st=st.getRightSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		st=st.getLeftSubtree();
		GC.gc();
		if(!st.verify()){
			throw new Error("Something wrong");
		}
		
		for(;;){
			
		System.out.println("Everything is fine");
		}
	}

}
