package problem;

public class SuperA {

	{
		System.out.println("SuperA clinit");
	}

	public SuperA() {
		System.out.println("SuperA constructor");
	}

	public void m() {
		System.out.println("SuperA m()");
		p();
	}

	private void p() {
		System.out.println("SuperA p()");
	}

}
