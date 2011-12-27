package wcet.devel;
/* Automated Test Procedure:
 *
 * $test$> make P1=test P2=wcet/devel P3=ResolveVirtuals CALLSTRING_LENGTH=3 USE_DFA=yes wcet
 * $grep$> ^wcet.always-hit:.*cost: 4341
 *
 */
import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Testcase for Bug#11:
 * --------------------
 * 
 * When using callstrings, resolving virtual method invocations in the control flow graph is
 * <ol><li/> imprecise
 *     <li/> might lead to cyclic callgraphs
 *     <li/> problematic, as we have to deal with infeasible receivers
 * </ol>
 * Concerning (2), we must ensure that no method which is not reachable from the analyzed method is inserted into a
 * control flow graph (unreachable methods are usually not analyzed).
 *
 * For example, suppose we have
 * <pre>
 * interface I { f();          }
 * class A < I { f() { ok;   } }
 * class B < I { f() { loop; } } // assume not reachable from WCET target
 * a()         { g(new A());   }
 * b()         { g(new B());   } // assume not reachable from WCET target
 * g(I i)      { i.f();        }
 * </pre>
 * When analyzing a() in this example, if we know that B#f() is not reachable, we SHOULD NOT consider B as a feasible receiver type
 * for i in g.
 *
 * Ideally, we should not resolve virtual invokes statically at all. As a short time fix, unreachable methods should be
 * excluded when resolving virtual invokes in control flow graphs. 
 * --------------------
 * In this test case, we test two unreachable interface implementations, and one callstring sensitive performance difference.
 */
public class ResolveVirtuals
{
	public static interface I {
		public void f();
	}
	/* invoking f() is ok */
	public static class A implements I {
		protected int a;
		@Override
		public void f() {
			a = (a+7);
		}		
	}
	/* invoking f() is ok, but expensive */
	public static class A1 extends A {
		int b;
		@Override
		public void f() {
			for(int i = 0; i<16;i++) { //@WCA loop = 16
				super.f();
				b += 7 * a;
			}
		}				
	}
	/* invoking f() leads to cycle in call graph */
	public static class B implements I {
		I target;
		@Override
		public void f() {
			target.f();
		}		
	}	
	/* invoking f() is impossible, because there is no implementation of J */
	public static interface J {
		public void f1();
	}
	public static class C implements I {
		private J j;
		public C(J j) {
			this.j = j;
		}
		@Override
		public void f() {
			j.f1();
		}
	}

	private  A  a  = new A();
	private  A1 a1 = new A1();
	private  B b = new B();
	private  C c = null;
	
	/* test */
	public static void g(I i) {
		i.f();
	}
	public static void a(A a) {
		g(a);
	}
	public static void b(B b) {
		g(b);
	}
	public static void c(C c) {
		g(c);
	}
	
	static int ts, te, to;
	static ResolveVirtuals test;

	public static void main(String[] args) {
		int dt;
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		test = new ResolveVirtuals();
		test.b.target = test.b; /* cycle */
		invoke();
		if (Config.MEASURE) {
			dt = te-ts-to;
			System.out.print("max: ");
            System.out.println(dt);
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		a(test.a);
		a(test.a1);
		a(test.a);
	}
	

}
