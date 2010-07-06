package jembench.parallel.raytrace;

import java.util.Vector;

public class RenderThread implements Runnable {
	double[] cameraPosition;
	double[] cameraTarget;
	double[] cameraZenith;
	Camera camera;
	int screenWidth;
	int screenHeight;
	double fieldOfView;
	double depthOfView;
	int hit;
	int[] w;
	int[] h;
	int[] r;
	int[] g;
	int[] b;
	double[] z;
	long time_initray;
	long time_renderPixel;
	int numPixels;
	int numTris;
	int numSpheres;
	Vector tris;
	Vector spheres;
	int recDepth;

	/**
	 * Konstruktor für einen RenderThread
	 * 
	 * @param aPart
	 *            Screenpartition die vom Thread abgearbeitet wird
	 * @param aCameraPosition
	 *            Kameraposition
	 * @param aCameraTarget
	 *            Point-of-Interest der Kamera
	 * @param aCameraZenith
	 *            Oben-Vektor der Kamera
	 * @param aScreenWidth
	 *            Bildschirmbreite(Pixel)
	 * @param aScreenHeight
	 *            Bildschirmhöhe(Pixel)
	 * @param aFieldOfView
	 *            Horizontales Sichtfeld(Grad)
	 * @param aDepthOfView
	 *            Sichtweite
	 * @param aScene
	 *            Zu berechnende Szene
	 */
	public RenderThread(String tName, int numPixels, 
			double[] aCameraPosition, double[] aCameraTarget,
			double[] aCameraZenith, int aScreenWidth, int aScreenHeight,
			double aFieldOfView, double aDepthOfView, Vector aTris,
			Vector aSpheres, int aRecDepth, Camera camera) {
		int i;
		cameraPosition = new double[aCameraPosition.length];
		cameraTarget = new double[aCameraTarget.length];
		cameraZenith = new double[aCameraZenith.length];
		
		for(i=0;i<aCameraPosition.length;i++)
			cameraPosition[i] = aCameraPosition[i];
		for(i=0;i<aCameraTarget.length;i++)
			cameraTarget[i] = aCameraTarget[i];
		for(i=0;i<aCameraZenith.length;i++)
			cameraZenith[i] = aCameraZenith[i];
		this.camera = camera;
		screenWidth = aScreenWidth;
		screenHeight = aScreenHeight;
		fieldOfView = aFieldOfView;
		depthOfView = aDepthOfView;
		hit = 0;
		w = new int[1];
		h = new int[1];
		this.numPixels = numPixels;
		z = new double[numPixels];
		r = new int[numPixels];
		g = new int[numPixels];
		b = new int[numPixels];
		time_initray = 0;
		time_renderPixel = 0;
		tris = aTris;
		numTris = tris.size();
		spheres = aSpheres;
		numSpheres = spheres.size();
		recDepth = aRecDepth;
	}


	/**
	 * run()-Methode
	 */
	public void run(){
		double[] viewVector = new double[3];
		double[] leftVector = new double[3];
		double[] upVector = new double[3];
		double[] rayVector = new double[3];
		double[] result;
		viewVector = new double[3];
		leftVector = new double[3];
		upVector = new double[3];
		rayVector = new double[3];;
		double delta, tmp, depth;
		int w, h, k, i=0, j;
		//Die "tmp_x" double-Arrays werden der intersect2-Methode mitgegeben damit nicht für jeden Schnitt neue Variablen angelegt werden müssen: 
		double[] tmp_1 = new double[8];
		double[][] tmp_3 = new double[9][3];
		double[][] tmp_6 = new double[3][6];
		
		//Strahl erzeugung vorbereiten:
	    delta = this.fieldOfView/this.screenWidth;//The Angle between two pixels
		for(k=0;k<3;k++)
			viewVector[k] = cameraTarget[k]-cameraPosition[k];
		tmp = Math.sqrt(viewVector[0]*viewVector[0] + viewVector[1]*viewVector[1] + viewVector[2]*viewVector[2]);
		for(k=0;k<3;k++)
			viewVector[k]*=(1/tmp);
		leftVector[0] = cameraZenith[1]*viewVector[2] - cameraZenith[2]*viewVector[1];
		leftVector[1] = cameraZenith[2]*viewVector[0] - cameraZenith[0]*viewVector[2];
		leftVector[2] = cameraZenith[0]*viewVector[1] - cameraZenith[1]*viewVector[0];
		upVector[0] = viewVector[1]*leftVector[2] - viewVector[2]*leftVector[1];
		upVector[1] = viewVector[2]*leftVector[0] - viewVector[0]*leftVector[2];
		upVector[2] = viewVector[0]*leftVector[1] - viewVector[1]*leftVector[0];
		
		//Laufe alle Pixel durch:
		
		while (camera.getNextCoordinates(this.w, this.h)){
			//Strahl erzeugen:
			w = this.w[0];
			h = this.h[0];
			//System.out.println("calculate "+w+","+h);
			MyVector.rotate(rayVector, viewVector, leftVector, w*delta-((this.screenWidth/2)*delta));
			MyVector.rotate(rayVector, rayVector, upVector, h*delta-((this.screenHeight/2)*delta));
			
			Vector scene = new Vector(this.spheres.size()+this.tris.size());
			for(j=this.spheres.size()-1;j>=0;j--)
				scene.addElement(this.spheres.elementAt(j));
			for(j=this.tris.size()-1;j>=0;j--)
				scene.addElement(this.tris.elementAt(j));
				
			//Schneide mit allen Objekten:
			result = Ray.intersect(scene, cameraPosition, rayVector, depthOfView, tmp_1, tmp_3, tmp_6, recDepth); 
			if((result[0]>0 && result[0]<z[i]) || (result[0]>0 && z[i]<=0)){
				depth = 1-(result[0]/depthOfView);
				this.z[i]=result[0];
				this.r[i] = (int)(result[1]*depth);
				this.g[i] = (int)(result[2]*depth);
				this.b[i] = (int)(result[3]*depth);
			}
			i++;
		}
	}
}
