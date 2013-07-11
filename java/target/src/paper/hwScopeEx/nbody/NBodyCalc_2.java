package hwScopeEx.nbody;

public class NBodyCalc_2 implements Runnable {
	
	double t1,t2, time;

	BodyScoped_2[] B;
	
	NBodyCalc_2(BodyScoped_2[] B){
		
		this.B = B;
		
	}
	
	@Override
	public void run() {
		
		for (int k= 0; k < NBodyScoped.ITERATIONS; k++){
		
		t1 = System.currentTimeMillis();
		
		/*
		 * We will use this tempBody object in order to generate the 
		 * aastore references. 
		 */
		BodyScoped_2 tempBody = new BodyScoped_2();
		
		/*
		 * Reset each body's total force
		 */
		for(int i=0; i<NBodyScoped.N; i++){
			for(int j = 0; j<B[0].pp.F.length;j++){
				B[i].pp.F[j] = 0;
			}
		}
		
		for(int t=0; t< NBodyScoped.steps; t++){
			
			for(int i=0; i<NBodyScoped.N; i++){
				tempBody = B[i];
				for(int j=0; j<NBodyScoped.N; j++){
					if(i != j){
						//computeForce(B[i], B[j]);
						computeForce(tempBody, B[j]);
						B[i].pp = tempBody.pp;
					}
				}
			}
			
			for(int i=0; i<NBodyScoped.N; i++){
				//acceleration(B[i]);
				tempBody = B[i];
				acceleration(tempBody);
				B[i] = tempBody;
			}
			
			for(int i=0; i<NBodyScoped.N; i++){
				tempBody = B[i];
				move(tempBody, (float) 0.1);
				B[i] = tempBody;
				//move(B[i], 0.1);
			}
			
		}
		
		t2 = System.currentTimeMillis();
		NBodyScoped.time = NBodyScoped.time + (t2-t1);
		}
	}
	
	public void computeForce(BodyScoped_2 bi, BodyScoped_2 bj) {
		
		float r, dx, dy;

		dx = (bj.pp.pos[0] - bi.pp.pos[0]);
		dy = (bj.pp.pos[1] - bi.pp.pos[1]);
		r = (float) Math.sqrt(dx*dx + dy*dy);
		
		bi.pp.F[0] = (NBodyScoped.G * bi.pp.mass * bj.pp.mass)/(dx*dx + dy*dy);
		bi.pp.F[1] = (bi.pp.F[0]*dx)/r;
		bi.pp.F[2] = (bi.pp.F[0]*dy)/r;
		 
	}
	
	public void acceleration(BodyScoped_2 bi){
		
		bi.pp.acc[0] = bi.pp.F[1]/bi.pp.mass;
		bi.pp.acc[1] = bi.pp.F[2]/bi.pp.mass;

	}
	
	public void move(BodyScoped_2 bi, float dt){
		
		bi.pp.speed[0] = bi.pp.speed[0] + bi.pp.acc[0]*dt;
		bi.pp.speed[1] = bi.pp.speed[1] + bi.pp.acc[1]*dt;
		
		bi.pp.pos[0] = bi.pp.pos[0] + bi.pp.speed[0]*dt;
		bi.pp.pos[1] = bi.pp.pos[1] + bi.pp.speed[1]*dt;

	}
}
