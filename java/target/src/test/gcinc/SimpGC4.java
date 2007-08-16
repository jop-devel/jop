package gcinc;
import com.jopdesign.sys.GC;
public class SimpGC4 implements Runnable {

	public myList list;
	public boolean test;
	private typeA myReference;
	int length,nr;
	public SimpGC4(int i) {
		length = i;
		}
	public void run() {
		
			if(test)
				{
				if (!myReference.testYourself(length)){
				throw new Error("Object is in wrong state");}
				synchronized(monitor)
				{shared=myReference;}
				
				myReference=null;
				test=false;
				}
			
			//System.out.println("running");
			if (SimpGC4.shared!=null){
			synchronized(monitor){
			myReference=SimpGC4.shared;
			SimpGC4.shared=null;
				}
			test=true;
			}
		}
		
	

	static typeA shared;
	static SimpGC4 a,b,c;
	static Object monitor;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpGC4(22);
		b = new SimpGC4(22);
		c = new SimpGC4(22);
		monitor=new Object();
		SimpGC4.shared=new typeA(22);
		
		for (;;) {
			a.run();
			GC.gc();
			b.run();
			GC.gc();
			c.run();
			GC.gc();
		}
	}

}	