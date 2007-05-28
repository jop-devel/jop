package gcinc;

import java.util.Vector;

public class SimpVector implements Runnable {

	Vector v;
	int nr;
	int size; 
	
	public SimpVector(int i) {
		size = i;
	}
	public void run() {
		if (nr==0) {
			if (size==0) throw new Error("Size is 0");
			// create the vector
			v = new Vector();
			for (int i=0; i<size; ++i) {
				v.addElement(new Integer(i));
			}
			nr = 1;
		} else if (nr==1) {
			// check the vector
			if (v.size()!=size) throw new Error("Size changed");
			for (int i=0; i<size; ++i) {
				Object o = v.elementAt(i);
				if (o==null) throw new Error("Null pointer to element");
				Integer it = (Integer) o;
				int iv = it.intValue();
				if (iv!=i) throw new Error("Value is wrong");
			}
			nr = 0;
		}
	}

	static SimpVector a,b,c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpVector(100);
		b = new SimpVector(25);
		c = new SimpVector(999);

		for (;;) {
			a.run();
			b.run();
			c.run();
		}
	}

}
