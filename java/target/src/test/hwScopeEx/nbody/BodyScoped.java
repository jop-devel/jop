package hwScopeEx.nbody;

public class BodyScoped {
	
	/*
	 * Array for position data. Index 0 has the x value
	 * and the index 1 has the y value. 
	 */
	public double[] position;
	
	/*
	 * Array for body speed. Index 0 is vx and index 1 is
	 * vy
	 */
	public double[] speed;
	
	public double mass;
	
	/*
	 * Array for total force acting on the body. Index 0 has
	 * the total force, index 1 is Fx and index 2 is Fy
	 */
	public double[] Force;

	/*
	 * Array for acceleration. Index 0 has ax and index 2 is ay
	 */
	public double[] acceleration;
	
	public BodyScoped(){
		
	}
	
	public BodyScoped(double[] position, double[] speed, double[] acceleration,
						double mass) {
		
		this.position = position;
		this.speed = speed;
		this.acceleration = acceleration;
		this.mass = mass;
		
		this.Force = new double[3];

	}

}
