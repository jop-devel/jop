package hwScopeEx.nbody;

public class NBodyCalc implements Runnable {
	
	double t1,t2, time;

	BodyScoped[] B;
	
	NBodyCalc(BodyScoped[] B){
		
		this.B = B;
		
	}
	
	@Override
	public void run() {
		
		t1 = System.currentTimeMillis();
		
		/*
		 * We will use this tempBody object in order to generate the 
		 * aastore references. 
		 */
		BodyScoped tempBody = new BodyScoped();
		
		/*
		 * Reset each body's total force
		 */
		for(int i=0; i<NBodyScoped.N; i++){
			for(int j = 0; j<B[0].Force.length;j++){
				B[i].Force[j] = 0;
			}
		}
		
		for(int t=0; t< NBodyScoped.steps; t++){
			
			for(int i=0; i<NBodyScoped.N; i++){
				tempBody = B[i];
				for(int j=0; j<NBodyScoped.N; j++){
					if(i != j){
						//computeForce(B[i], B[j]);
						computeForce(tempBody, B[j]);
						B[i] = tempBody;
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
		NBodyScoped.time = t2-t1;
		
	}
	
	public void computeForce(BodyScoped bi, BodyScoped bj) {
		
		float r, dx, dy;

		dx = (bj.position[0] - bi.position[0]);
		dy = (bj.position[1] - bi.position[1]);
		r = (float) Math.sqrt(dx*dx + dy*dy);
		
		bi.Force[0] = (NBodyScoped.G * bi.mass * bj.mass)/(dx*dx + dy*dy);
		bi.Force[1] = (bi.Force[0]*dx)/r;
		bi.Force[2] = (bi.Force[0]*dy)/r;
		 
	}
	
	public void acceleration(BodyScoped bi){
		
		bi.acceleration[0] = bi.Force[1]/bi.mass;
		bi.acceleration[1] = bi.Force[2]/bi.mass;

	}
	
	public void move(BodyScoped bi, float dt){
		
		bi.speed[0] = bi.speed[0] + bi.acceleration[0]*dt;
		bi.speed[1] = bi.speed[1] + bi.acceleration[1]*dt;
		
		bi.position[0] = bi.position[0] + bi.speed[0]*dt;
		bi.position[1] = bi.position[1] + bi.speed[1]*dt;

	}
}
