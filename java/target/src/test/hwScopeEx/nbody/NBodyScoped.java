package hwScopeEx.nbody;

import java.util.Random;

import com.jopdesign.sys.Memory;

/*
 * Scoped version of the N-Body simulation from classical mechanics. It is a "brute-force" 
 * simulation in the sense that the algorithm to calculate the resulting force of each body is 
 * not optimized (anyway that is not the goal of this example). Of course, drawback is that 
 * it is a slow simulation, proportional to the N*N.
 * 
 * Number of aastore references are in the order of 6*N*steps
 */
public class NBodyScoped {
	
	/*
	 * Number of bodies in simulation
	 */
	final static int N = 2;
	final static int steps = 100;
	final static int DIMENSIONS = 2;
	
	final static int ITERATIONS = 5;
	
	public static double time;
	
	/*
	 * Constants to count reference access
	 */
//	public static boolean COUNT_REF = false;
//	public static int PUTFIELD_COUNT = 0;
//	public static int PUTSTATIC_COUNT = 0;
//	public static int AASTORE_COUNT = 0;

	
	final static boolean SHW_DETAILS = false;
	
	/*
	 * Gravitational constant
	 */
	final static float G = (float) 6.67e-11;
	
	/*
	 * An array of N bodies. It will be used to make extensive
	 * references to the array from a nested scope region.
	 */
	//BodyScoped[] ab;
	BodyScoped_2[] ab;
	
	static final int M_SIZE = 4096;
	static final int S_SIZE = 512;
	
	NBodyScoped(){
		
		init();
		
	}
	
	public void init(){
		
		/*
		 * Initialize the array of bodies
		 */
		
//		double[][] p = {{-0.5, 0}, {0.5, 0}}; 
//		double[][] s = {{0, 0}, {0, 0}};
//		double[][] a = {{0, 0}, {0, 0}};
//		
//		double[][] p = new double[N][2]; 
//		double[][] s = new double[N][2];
//		double[][] a = new double[N][2];

		float[][] p = new float[N][2]; 
		float[][] s = new float[N][2];
		float[][] a = new float[N][2];

		/*
		 * i = body number
		 * j = dimension (x,y,z,....)
		 */
		Random rand = new Random();
		for(int i = 0; i < N; i++){
			for(int j=0; j<DIMENSIONS; j++){
				p[i][j] = rand.nextFloat();
				s[i][j] = 0;
				a[i][j] = 0;
			}
		}
		
		//ab = new BodyScoped[N];
		ab = new BodyScoped_2[N];
		for(int i = 0; i < N; i++){
			ab[i] = new BodyScoped_2(p[i], s[i], a[i],10000000);
			//ab[i] = new BodyScoped(p[i], s[i], a[i],10000000);
		}
	};
	
	public static void main(String args[]){
		
		NBodyScoped nBodySc = new NBodyScoped();
		
		//NBodyCalc nBodyCalc = new NBodyCalc(nBodySc.ab);
		NBodyCalc_2 nBodyCalc = new NBodyCalc_2(nBodySc.ab);
		
		Memory m = Memory.getCurrentMemory();
		
		//nBodyCalc.run();
		
//		COUNT_REF = true;
	
		m.enterPrivateMemory(512, nBodyCalc);
		
		double total_time = time/ITERATIONS;

		System.out.println("---------- Simulation results after t = "+steps+" steps ----------");
		System.out.println("---------- Number of bodies: "+N+"----------");
		System.out.println("---------- Elapsed time: " +  total_time + "----------");
//		System.out.println("---------- Static refs: " + PUTSTATIC_COUNT + "----------");
//		System.out.println("---------- Field refs: " + PUTFIELD_COUNT + "----------");
//		System.out.println("---------- Array refs: " + AASTORE_COUNT + "----------");

//		if(SHW_DETAILS){
//			
//			for(int p=0; p<N;p++){
//				
//				System.out.println();
//				System.out.println("---------- Body " +p+ "----------");
//				System.out.println("Force in x : "+nBodySc.ab[p].Force[1]);
//				System.out.println("Force in y : "+nBodySc.ab[p].Force[2]);
//				
//				System.out.println("Speed in x : "+nBodySc.ab[p].speed[0]);
//				System.out.println("Speed in y : "+nBodySc.ab[p].speed[1]);
//				
//				System.out.println("Position in x : "+nBodySc.ab[p].position[0]);
//				System.out.println("Position in y : "+nBodySc.ab[p].position[1]);
				
//			}
//		}
		
	}
}
