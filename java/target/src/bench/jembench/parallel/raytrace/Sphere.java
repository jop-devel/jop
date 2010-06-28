package jembench.parallel.raytrace;


import java.util.Vector;

public class Sphere{
	double radius;//radius
	double[]position;
	int[]rgb;
	double reflection;
	
	public Sphere(){
		position= new double[]{0,0,0};
		radius = 1;
		rgb=new int[]{1,2,3};
	}
	

	
	public Sphere(double aPositionX, double aPositionY, double aPositionZ, double aRadius){
		position = new double[]{aPositionX,aPositionY,aPositionZ};
		radius = aRadius;
		rgb=new int[]{1,2,3};
	}
	
	public Sphere(double aPositionX, double aPositionY, double aPositionZ, double aRadius, int red, int green, int blue, double ref){
		position = new double[]{aPositionX,aPositionY,aPositionZ};
		radius = aRadius;
		rgb = new int[]{red,green,blue};
		reflection = ref;
	}
	
	/**
	 * Erzeugt eine Zufällige Szene, gefüllt mit Kugeln.
	 * 
	 * @param spheres_num		Anzahl von Kugeln
	 * @param spheres_maxsize	Maximale Größe einer Kugel
	 * @param dist2zero			Maximale Entfernung zum Koordinatenursprung
	 * @return					generierte Szene
	 */
/*	public static Vector createRandomSpheres(int spheres_num, double spheres_maxsize, double dist2zero){
		Vector result = new Vector(0);
		double rx, ry, rz, rs;
		int colr, colg, colb;
		Random r = new Random();
		for(int i=0; i<spheres_num; i++){
			rx = (r.nextDouble()-r.nextDouble())*dist2zero;
			ry = (r.nextDouble()-r.nextDouble())*dist2zero;
			rz = (r.nextDouble()-r.nextDouble())*dist2zero;
			rs = (r.nextDouble())*spheres_maxsize;
			colr = r.nextInt(256);
			colg = r.nextInt(256);
			colb = r.nextInt(256);
			Sphere s = new Sphere(rx,ry,rz,rs,colr,colg,colb,0.95);
			result.addElement(s);
		}
		return result;
	}*/

	public static Vector createSpheres(){
		Vector result = new Vector(0);
		result.addElement(new Sphere(0.872F,2.926F,-3.470F, 0.443F, 60,207,204, 0.95F));
		result.addElement(new Sphere(-1.241F,1.113F,1.065F, 0.179F, 193,206,122, 0.95F));
		/*result.addElement(new Sphere(3.683F,-2.218F,1.519F, 1.537F, 225,247,98, 0.95F));
		result.addElement(new Sphere(-1.760F,4.712F,-0.842F, 0.786F, 127,231,43, 0.95F));
		result.addElement(new Sphere(-0.253F,2.966F,-1.498F, 0.409F, 72,172,85, 0.95F));
		*/
		return result;
	}

	public double[] reflect(double[]intersection, double[]rayDir){
		double[] result = new double[3];
		double[] normal = new double[3];
		MyVector.to(normal, position, intersection);
		MyVector.normalize(normal, normal);
		MyVector.scale(result, rayDir, -1);//Richtung ändern
		MyVector.rotate(result, result, normal, 180);//Um die Normale drehen
		return result;
	}
}