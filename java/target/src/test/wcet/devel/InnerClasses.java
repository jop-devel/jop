package wcet.devel;

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class InnerClasses {

	public static class A {
		int z;
		protected boolean setA(int v) {
			if(v > 30) return false;
			z = (1<<v);
			return true;
		}
		private B b;		
		public A(int v) {
			if(v<=0) return;
			z=v;
			b = new B(this) {
				protected boolean setB(int b) {
					return ! set2(b);
				}
				private boolean set2(int b) {
					y=z+b;
					System.out.println("Am I never used? (B)");
					return false;
				}
			};
		}
		public class B {
			private A outer;
			int y;
			public B(A o) {				
				outer = new A(o.z - 1) {
					protected boolean setA(int v) {
						z = v+y;
						System.out.println("Am I never used? (A)");
						return true;
					}
				};
			}
			protected boolean setB(int b) {
				if(b > 30) return false;
				y = 1<<b;
				return true;
			}
		}
	}
	public class C {
		int z;
		boolean setC(int b) {
			if(b > 10) return false;
			z = 1<<(b*3);
			return true;
		}
	}

	private A   a = new A(2);
	private A.B b = new A(3).b;
	private C c = new C();

	public void test() {
		a.b.outer.setA(x%10);
		b.outer.b.setB(x%30);
		c.setC(x%10);
	}

	/* should have roughly 10K cycles to simplify the evaluation */
	int x;
	void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<167;++i) // @WCA loop=160
			x = (x+1) * i;
	}

	static int ts, te, to;
	private static InnerClasses inst;

	public static void main(String[] args) {
		if(Config.MEASURE) {
			ts = Native.rdMem(Const.IO_CNT);
			te = Native.rdMem(Const.IO_CNT);
			to = te-ts;
		}
		inst = new InnerClasses();
		invoke();
		if (Config.MEASURE) {
			int dt = te-ts-to;
			System.out.print("wcet[AnnotLang1]:");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		inst.test();
	}

}
