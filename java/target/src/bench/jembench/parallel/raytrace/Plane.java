package jembench.parallel.raytrace;

public class Plane {
	double[] p;
	double[] n;
	
	/**
	 * Standardkonstruktur einer Ebene
	 */
	public Plane(){
		p = new double[]{0,0,0};
		n = new double[]{1,0,0};
	}
	


	
	/**
	 * Konstruktor einer Ebene
	 * 
	 * @param px	x-Koordinate eines Punktes der Ebene
	 * @param py	y-Koordinate eines Punktes der Ebene
	 * @param pz	z-Koordinate eines Punktes der Ebene
	 * @param nx	x-Koordinate des Normalenvektors der Ebene
	 * @param ny	y-Koordinate des Normalenvektors der Ebene
	 * @param nz	z-Koordinate des Normalenvektors der Ebene
	 */
	public Plane(double px, double py, double pz, double nx, double ny, double nz){
		p = new double[]{px,py,pz};
		n = new double[]{nx,ny,nz};
	}
}
