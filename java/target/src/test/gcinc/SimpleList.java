package gcinc;

public class SimpleList {
	
	class Element {
		Object element;
		Element next;
	}
	
	Element first, last;

	public void append(Object o) {
		Element e = new Element();
		e.element = o;
		synchronized (this) {
			if (last!=null) {
				last.next = e;
			} else {
				first = e;
			}
			last = e;
		}
	}
	
	public Object remove() {
		
		Object o = null;
		synchronized (this) {
			if (first!=null) {
				Element e = first;
				o = e.element;
				first = e.next;
				if (first==null) {
					last = null;
				}
			}
		}
		return o;
	}
}
