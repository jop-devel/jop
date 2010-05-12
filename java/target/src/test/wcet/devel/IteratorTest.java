package wcet.devel;

public class IteratorTest {

	public static void main(String [] args) {
		List<Integer> list = new List<Integer>(1);
		list = list.cons(2);
		list = list.cons(3);
		list = list.cons(4);
		list = list.cons(5);
		list = list.cons(6);
		list = list.cons(7);
		list = list.cons(8);
		
		measure(list);
	}

	public static void measure(List<Integer> list) {
		iterate(list);
	}

	public static void iterate(List<Integer> list) {
		while (list != null) { //@WCA loop <= 8
			Integer t = list.car;
			// System.out.println(t.intValue());
			list = list.cdr;
		}
	}
}

class List<T> {
	public T car;
	public List<T> cdr;

	public List<T> cons(T head) {
		return new List(head, this);
	}

	public List(T head) {
		this(head, null);
	}

	private List(T head, List<T> tail) {
		this.car = head;
		this.cdr = tail;
	}
}