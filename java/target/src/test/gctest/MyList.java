/*
 * Created on 16.06.2005
 *
 */
package gctest;


/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class MyList {
	
	final static int CNT = 1000;
	
	private MyList next;
	private int a, b, c;
	
	
	static MyList list1, list2;
	
	
	static void test() {
		
		MyList list1, list2;
		
		MyList ptr = null;
		int i, j;
		list1 = list2 = null;
		
		for (int cnt=0; cnt<100;++cnt) {
			for (i=0; i<CNT; ++i) {
				ptr = list1;
//				System.out.println("new 1");
				list1 = new MyList();
				list1.a = i+cnt;
				list1.next = ptr;
				ptr = list2;
//				System.out.println("new 2");
				list2 = new MyList();
				list2.a = 1000+i+cnt;
				list2.next = ptr;
			}
			i = CNT;
			ptr = list1;
			j = 0;
			while (ptr!=null) {
				++j;
				--i;
				int val = ptr.a;
				if (val!=i+cnt) {
					System.out.println("Problem");
					System.exit(1);
				}
				ptr = ptr.next;
			}
			if (j!=CNT) {
				System.out.println("different size");
				System.exit(1);
			}
			i = CNT+1000+cnt;
			ptr = list2;
			j = 0;
			while (ptr!=null) {
				++j;
				--i;
				int val = ptr.a;
				if (val!=i) {
					System.out.println("Problem");
					System.exit(1);
				}
				ptr = ptr.next;
			}
			if (j!=CNT) {
				System.out.println("different size");
				System.exit(1);
			}
			System.out.print('*');
			// free both lists
			list1 = null;
			list2 = null;
		}
	}
	
	public static void main(String[] args) {
		test();
	}
}
