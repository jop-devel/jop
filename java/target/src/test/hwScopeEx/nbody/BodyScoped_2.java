package hwScopeEx.nbody;

public class BodyScoped_2 {
	
	/*
	 * Array for position data. Index 0 has the x value
	 * and the index 1 has the y value. 
	 */
	//public float[] position;
	
	/*
	 * Array for body speed. Index 0 is vx and index 1 is
	 * vy
	 */
	//public float[] speed;
	
	//public float mass;
	
	/*
	 * Array for total force acting on the body. Index 0 has
	 * the total force, index 1 is Fx and index 2 is Fy
	 */
	//public float[] Force;

	/*
	 * Array for acceleration. Index 0 has ax and index 2 is ay
	 */
	//public float[] acceleration;
	
	
	//public Object[] params;
	
	public Params pp;
	
	public BodyScoped_2(){
		
	}
	
//	public BodyScoped_2(float[] p, float[] s, float[] a,
//						float mass) {
		
	public BodyScoped_2(float[] pos, float[] speed, float[] acc,
			int mass) {

		pp = new Params();
		pp.pos = pos;
		pp.speed = speed;
		pp.acc = acc;
		pp.mass = mass;
	}
}

class Params{
	
	float[] pos;
	float[] speed;
	float[] acc;
	float[] F;
	int mass;

	Params(){
		
		pos = new float[NBodyScoped.DIMENSIONS];
		speed = new float[NBodyScoped.DIMENSIONS];
		acc = new float[NBodyScoped.DIMENSIONS];
		F = new float[NBodyScoped.DIMENSIONS + 1];
		
		}
		
	}
