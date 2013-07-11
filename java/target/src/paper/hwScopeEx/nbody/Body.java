package hwScopeEx.nbody;

import java.lang.Math;

public class Body {
	
	private double x, y;
	private double vx, vy;
	private double mass;
	private double F;
	private double Fx,Fy;
	
	private double a;
	private double ax,ay;
	
	private double G = 6.67e-11;
	
	Body(double x, double y, double vx, double vy, double mass){
		
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vx = vy;
		this.mass = mass;
		
	}
	
	/*
	 * Updates the position and velocity of a body, using the current velocity
	 * and force for an interval of time dt
	 */
	public void move(double dt){
		
		vx = vx + ax*dt;
		vy = vy + ay*dt;
		
		x = x + vx*dt;
		y = y + vy*dt;
	}
	
	public void acceleration(){
		
		ax = Fx/mass; 
		ay = Fy/mass;
	}
	
	/* Used to reset the force acting on a particular body to zero.
	 */
	public void resetForce() {
		
		F = 0;
		 
	}
	
	/* Computes and updates the force acting on a body that is exerted by a second body b, 
	 * using the physics of mutual gravitational interaction.
	 */
	public void computeForce(Body b) {
		
		double r, dx, dy;
		
		dx = (b.x - x);
		dy = (b.y - y);
		r = Math.sqrt(dx*dx + dy*dy);
		
		
		F = (G*mass*b.mass)/(dx*dx + dy*dy);
		Fx = (F*dx)/r;
		Fy = (F*dy)/r;
		 
	}
	
	public double getForceX(){
		return Fx;
	}
	
	public double getForceY(){
		return Fy;
	}
	
	public double getSpeedX(){
		return vx;
	}
	
	public double getSpeedY(){
		return vy;
	}
	
	public double getPosX(){
		return x;
	}
	
	public double getPosY(){
		return y;
	}




}
