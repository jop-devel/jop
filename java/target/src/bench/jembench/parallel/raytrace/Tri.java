package jembench.parallel.raytrace;

import java.util.Vector;

public class Tri{
	double[] a;
	double[] b;
	double[] c;
	int[]rgb;
	double reflection;
	

	
	public Tri(double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz, int red, int green, int blue, double ref){
		a = new double[]{ax,ay,az};
		b = new double[]{bx,by,bz};
		c = new double[]{cx,cy,cz};
		rgb=new int[]{red,green,blue};
		reflection = ref;
	}
	

	public String toString(){
		return new String("(("+this.a[0]+this.a[1]+this.a[2]+")("+this.b[0]+this.b[1]+this.b[2]+")("+this.c[0]+this.c[1]+this.c[2]+")("+this.rgb[0]+this.rgb[1]+this.rgb[2]+"))");
	}
	

	public void getPlane(double[]r, double[]tmp1, double[]tmp2, double[]tmp3){
		if(r==null || r.length<6)
			r = new double[6];
		if(tmp1==null || tmp1.length<3)
			tmp1 = new double[3];
		if(tmp1==null || tmp2.length<3)
			tmp2 = new double[3];
		if(tmp1==null || tmp3.length<3)
			tmp3 = new double[3];
		
		MyVector.to(tmp1,a,b);
		MyVector.to(tmp2,a,c);
		MyVector.crossProd(tmp3, tmp1, tmp2);
		MyVector.normalize(tmp1, tmp3);
		r[0] = a[0];
		r[1] = a[1];
		r[2] = a[2];
		r[3] = tmp1[0];
		r[4] = tmp1[1];
		r[5] = tmp1[2];
	}
	
	/**
	 * Erzeugt eine zufällige Szene, gefüllt mit Dreiecken.
	 * 
	 * @param tri_num		Anzahl von Dreiecken
	 * @param tri_maxsize	Maximale Größe der Dreiecke
	 * @param dist2zero		Maximale Entfernung zum Koordinatenursprung
	 * @return				generierte Szene
	 */
/*	public static Vector createRandomTris(int tri_num, double tri_maxsize, double dist2zero){
		Vector result = new Vector(0);
		for(int i=0; i<tri_num; i++){
			Random r = new Random();
			double ax = (r.nextDouble()-r.nextDouble())*dist2zero;
			double ay = (r.nextDouble()-r.nextDouble())*dist2zero;
			double az = (r.nextDouble()-r.nextDouble())*dist2zero;
			
			double bx = ax+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			double by = ay+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			double bz = az+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			
			double cx = ax+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			double cy = ay+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			double cz = az+(r.nextDouble()-r.nextDouble())*tri_maxsize;
			
			int colr = r.nextInt(256);
			int colg = r.nextInt(256);
			int colb = r.nextInt(256);
			
			result.addElement(new Tri(ax,ay,az,bx,by,bz,cx,cy,cz,colr,colg,colb,0.95));
		}
		return result;
	}*/

	public static Vector createTris(){
		Vector result = new Vector(0);
		result.addElement(new Tri(0.865F,-6.271F,0.370F, -0.428F,-6.525F,0.773F, 0.816F,-7.231F,1.132F, 129,96,145, 0.95F));
		result.addElement(new Tri(-2.171F,1.013F,3.697F, -1.391F,1.458F,4.923F, -1.841F,1.294F,3.171F, 26,91,246, 0.95F));
		/*result.addElement(new Tri(-1.805F,0.945F,-1.777F, -1.915F,1.559F,-1.839F, -2.614F,1.289F,-1.160F, 199,67,215, 0.95F));
		result.addElement(new Tri(-1.242F,-4.499F,1.120F, -0.307F,-4.425F,0.040F, -3.044F,-3.670F,1.293F, 229,36,49, 0.95F));
		result.addElement(new Tri(-3.154F,0.854F,-2.775F, -3.214F,-0.319F,-3.299F, -3.331F,1.849F,-3.681F, 62,117,239, 0.95F));
		*/
		return result;
	}


	public double[] reflect(double[]intersection, double[]rayDir){
		double[] result = new double[3];
		double[] normal = new double[6];
		this.getPlane(normal, new double[3], new double[3], new double[3]);
		normal[0]=normal[3];
		normal[1]=normal[4];
		normal[2]=normal[5];
		MyVector.normalize(normal, normal);
		MyVector.scale(result, rayDir, -1);//Richtung ändern
		MyVector.rotate(result, result, normal, 180);//Um die Normale drehen
		return result;
	}
}
