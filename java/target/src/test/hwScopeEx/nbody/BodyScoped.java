package hwScopeEx.nbody;

public class BodyScoped {
	
	/*
	 * Array for position data. Index 0 has the x value
	 * and the index 1 has the y value. 
	 */
	public float[] position;
	
	/*
	 * Array for body speed. Index 0 is vx and index 1 is
	 * vy
	 */
	public float[] speed;
	
	public float mass;
	
	/*
	 * Array for total force acting on the body. Index 0 has
	 * the total force, index 1 is Fx and index 2 is Fy
	 */
	public float[] Force;

	/*
	 * Array for acceleration. Index 0 has ax and index 2 is ay
	 */
	public float[] acceleration;
	
	public BodyScoped(){
		
	}
	
	public BodyScoped(float[] p, float[] s, float[] a,
						float mass) {
		
		this.position = p;
		this.speed = s;
		this.acceleration = a;
		this.mass = mass;
		
		this.Force = new float[3];

	}

}
