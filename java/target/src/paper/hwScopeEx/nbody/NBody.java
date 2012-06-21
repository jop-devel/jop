package hwScopeEx.nbody;

/*
 * N-Body simulation from classical mechanics. It is a "brute-force" simulation in the sense
 * that the algorithm to calculate the resulting force of each body is not optimized (anyway 
 * that is not the goal of this example). Of course, drawback is that it is a slow simulation,
 * proportional to the N*N
 * 
 * -------------- NON-SCOPED VERSION --------------------------
 */

public class NBody {
	
	/*
	 * Number of bodies and discrete steps for the 
	 * simulation
	 */
	final static int N = 2;
	final static int steps = 200;
	
	/*
	 * An array of N bodies.
	 */
	Body[] ab;
	
	NBody(){
		
		/*
		 * Initialize the array of bodies
		 */
		ab = new Body[N];
		for(int i = 0; i < N; i++){
			ab[i] = new Body(i-0.5, 0, 0, 0, 10000000);
		}
	};
	
	public static void main(String args[]){
		
		NBody nBodyArr = new NBody();
		
		for(int i=0; i<N; i++){
			nBodyArr.ab[i].resetForce();
		}
		
		for(int t=0; t< steps; t++){
			
			for(int i=0; i<N; i++){
				for(int j=0; j<N; j++){
					if(i != j){
						nBodyArr.ab[i].computeForce(nBodyArr.ab[j]);
					}
				}
			}
			
			for(int i=0; i<N; i++){
				nBodyArr.ab[i].acceleration();
			}
			
			for(int i=0; i<N; i++){
				nBodyArr.ab[i].move(0.1);
			}
			
		}

		System.out.println("---------- Simulation results after t = "+steps+" steps ----------");

		for(int p=0; p<N;p++){
			
			System.out.println();
			System.out.println("---------- Body " +p+ "----------");
			System.out.println("Force in x : "+nBodyArr.ab[p].getForceX());
			System.out.println("Force in y : "+nBodyArr.ab[p].getForceY());
			
			System.out.println("Speed in x : "+nBodyArr.ab[p].getSpeedX());
			System.out.println("Speed in y : "+nBodyArr.ab[p].getSpeedY());
			
			System.out.println("Position in x : "+nBodyArr.ab[p].getPosX());
			System.out.println("Position in y : "+nBodyArr.ab[p].getPosY());
			
		}
		


		
	}

}
