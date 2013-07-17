package wcet.devel;
/* Automated Test Procedure:
 *
 * $test$> make jsim P1=test P2=wcet/devel P3=SuperGraph1
 * $grep$> wcet[r()]: ^ 2046503
 * $grep$> wcet[r()]: ^ 201 * c[=10165]
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=SuperGraph1 WCET_METHOD=compute10
 * $grep$> wcet: (cost: ^ 103210 $ )
 * ==> c has WCET of roughly 10000
 *
 * with callstring length 0, the example does not work yet
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=SuperGraph1 WCET_METHOD=r WCET_OPTIONS="-callstring-length 0 --use-dfa"
 * $grep$> wcet: (cost: ^ 3300000 $ , execution
 * ==> ~ 330 c
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=SuperGraph1 WCET_METHOD=r WCET_OPTIONS="-callstring-length 1 --use-dfa"
 * $grep$> wcet: (cost: ^ 2523947 $ , execution
 * ==> ~ 250 c
 *
 * $test$> make wcet P1=test P2=wcet/devel P3=SuperGraph1 WCET_METHOD=r WCET_OPTIONS="-callstring-length 2 --use-dfa"
 * $grep$> wcet: (cost: ^ 2079848 $ , execution
 * ==> ~ 210 c
 */
import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * <p>Test call-string sensitive analyses based on supergraph model</p>
 * <p>Below let c be the cost of executing compute(). Then
 *    the expected supergraph with CS=2 is</p>
 * <pre>
 *   r -> f(r1 ~ false) -> g(r1+f1 ~ 2) -> h()
 *                      -> g(r1+f2 ~ 5) -> j()
 *                      -> g(r1+f3 ~ 2) -> h()
 *   ... (cost = 12+55+12 + 12+55+55 = 201 c [measured])
 * </pre>
 * <p>Expected Callgraph with CS=1</p>
 * <pre>
 *   r -> f(r1 ~ false) -> g(f1 ~ 10) -> h()
 *                      -> g(f2 ~ 20) -> j()
 *                      -> g(f3 ~ ?)  -> h() or j()
 *   ... (cost = 12+55+55 + 12+55+55 = 244 c)
 * </pre>
 * <p>Expected Callgraph with CS=0</p>
 * <pre>
 *   r -> f(?) -> g(?) -> h() or j()
 *             -> g(?) -> h() or j()
 *             -> g(?) -> h() or j()
 *   ... (cost = 6 * 55 = 330  c)
 * </pre>
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SuperGraph1 {
	/* fields */
	int x,y,z;
	/* call-context sensitive super graph */
	void r() {
		f(false);
		f(true);
	}
	void f(boolean b) {
		g(2);
		g(5);
		g(b ? 2 : 5);
		z = x*y;
	}
	void g(int n) {
	    /* DFA should compute a global loop bound of 5 */
		for(int i = 0; i < n; ++i) { 
			compute1();
		}

		/* Advanced loop bound annotations */
		// @ANNOTATION loop = floor(ld($1)) + 1
//		for(int j = n; j > 0; j>>=1) {
//		    x = x+1;
//		}

		if(n < 4) {
			h();
		} else {
			j();
		}
	}
	void h() {
		compute10();
		y=2;		
	}
	void j() {
		compute50();
		y=3;
	}
	/* should have roughly n * 10K cycles to simplify the evaluation */
	void compute1()  { 
		for(int j=0;j<1;++j)    // @WCA loop=1
		for(int i= 7;i<173;++i) // @WCA loop=166
			x = (x+1) * i;
	}
	void compute10()  { 
		for(int j=0;j<10;++j)    // @WCA loop=10
		for(int i= 7;i<173;++i) // @WCA loop=166
			x = (x+1) * i;
	}
	void compute50()  { 
		for(int j=0;j<50;++j)    // @WCA loop=50
		for(int i= 7;i<173;++i) // @WCA loop=166
			x = (x+1) * i;
	}
	
	static int ts, te, to;
	static SuperGraph1 test;

	public static void main(String[] args) {
		int dt;
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		test = new SuperGraph1();
		invoke();
		if (Config.MEASURE) {
			dt = te-ts-to;
			System.out.print("max: ");
            System.out.println(dt);
        }
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test.compute10();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (Config.MEASURE) {
			int dtc = te-ts-to;
			System.out.print("wcet[r()]: ");
            System.out.print(dt*10/dtc);
            System.out.print(" * c[=");
            System.out.print(dtc/10);
            System.out.println("]");
        }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		test.r();
	}
	

}
