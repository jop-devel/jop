package gcinc;
public class SimpGC3 implements Runnable {

	public myList list;
	int length,nr;
	public SimpGC3(int i) {
		length = i;
		
	}
	public void run() {
		if (nr==0) {
			if (length==0) throw new Error("Length is 0");
			// create the List
			list= new myList();
			for (int i=1; i<length/2 + 1; ++i) {
				list.add(new typeA(i));
				list.add(new typeB(i));
			}
			nr = 1;
		} else if (nr==1) {
			System.out.println(length);
			// check the list
			if (list.size()!=length) throw new Error("Size changed");
			testObject to;
			to=(testObject)list.next();
			for (int i=1; i<length/2+1; ++i) {
				if (to==null) throw new Error("Null pointer to element");
				if (!to.testYourself(i)) throw new Error("Value is wrong");
				to=(testObject)list.next();
			
				if (to==null) throw new Error("Null pointer to element");
				if (!to.testYourself(i)) throw new Error("Value is wrong");
				to=(testObject)list.next();
				}	
			nr = 0;
		
	}
	}

	static SimpGC3 a,b,c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpGC3(10);
		b = new SimpGC3(26);
		c = new SimpGC3(10);
		//GC.setConcurrent();	
		for (;;) {
			a.run();
			b.run();
			c.run();
			System.out.println("I'm running");
		}
	}

}		
