package gctest;


/**
 * Tests if GC collects the garbage object. The GC is explicitly invoked.
 */
public class GCTest1 {
	
	static Runtime rt = Runtime.getRuntime();

	public static void main(String s[]) {

		System.out.print("Total memory: ");
		System.out.println((int) rt.totalMemory());
		System.out.print("Free memory: ");
		System.out.println((int) rt.freeMemory());
		
		String allocStr = "Allocation problem!";
		String refStr = "Reference problem";

		Garbage garbage = new Garbage();
		int prevId = garbage.id;
		System.gc();
		int freeHeap = (int) rt.freeMemory();
		for (int i=0; i<10; ++i) {
			if (freeHeap != (int) rt.freeMemory()) {
				System.out.println(allocStr);
			}
			garbage = new Garbage();

			if ((garbage.id - prevId) != 1) {
				System.out.println(refStr);
				System.exit(-1);
			}
			prevId = garbage.id;

			System.gc(); //Remove the old garbage object

		}

		System.out.println("Test 1 OK");
	}
}

class Garbage {
	static int cnt = 0;

	public int id;

	public Garbage() {
		cnt++;
		id = cnt;
		System.out.print("Garbage object id:");
		System.out.println(id);
	}
}