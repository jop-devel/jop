package problem;

public class SuperB extends SuperA {

	{
		System.out.println("SuperB clinit");
	}

	public SuperB() {
		System.out.println("SuperB constructor");
	}

	public void m() {
		System.out.println("SuperB m()");
	}

	private void p() {
		System.out.println("SuperB p()");
	}

	public void foo() {
		m();
		super.m();
		p();
	}

	public static void main(String[] args) {

		SuperB s = new SuperB();
		s.foo();
	}
}
