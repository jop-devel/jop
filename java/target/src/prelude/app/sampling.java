package prelude.app;
public class sampling {
	
	static int i;
	static int input_i() {
		System.out.println("input_i() -> "+i);
		return i++;
	}
	
	static int id(int x) {
		System.out.println("id("+x+") -> "+x);
		return x;
	}

	static void swap(int x, int y, Preludesampling.swap_OutType out) {
		System.out.println("swap("+x+","+y+") -> "+y+","+x);
		out.o = y;
		out.p = x;
	}

	static void output_o(int o) {
		System.out.println("output_o("+o+")");
		System.out.print("o: ");
		System.out.print(o);
		System.out.println();
	}

}