package jembench.parallel.raytrace;

import java.util.Vector;


public class Camera {
	double[] position;	//position
	double[] target;	//position of target
	double[] zenith;	//up-vector
	double fieldOfView;		//horizontal field of view in degrees
	double depthOfView;		//depth of view
	int screenWidth;		//projection width in pixels
	int screenHeight;		//projection height in pixels
	Vector tris;
	Vector spheres;
	int cBuffer[][][];
	double zBuffer[][];
	int recDepth;
	int deliveredThreads=0;
	int curX=0;
	int curY=0;
	


	
	/**
	 * Konstruktor
	 * 
	 * @param aPositionX	x-Koordinate der Kameraposition
	 * @param aPositionY	y-Koordinate der Kameraposition
	 * @param aPositionZ	z-Koordinate der Kameraposition
	 * @param aTargetX		x-Koordinate des Point-of-interest
	 * @param aTargetY		y-Koordinate des Point-of-interest
	 * @param aTargetZ		z-Koordinate des Point-of-interest
	 * @param aZenithX		x-Koordinate des Oben-Vektors
	 * @param aZenithY		y-Koordinate des Oben-Vektors
	 * @param aZenithZ		z-Koordinate des Oben-Vektors
	 * @param aFieldOfView	horizontales Sichtfeld (Grad)
	 * @param aDepthOfView	Sichtweite
	 * @param aScreenWidth	Bildschrimbreite (Pixel)
	 * @param aScreenHeight	Bildschirmhöhe (Pixel)
	 * @param aSceneObjects Vektor mit den Objekten der Szene
	 */
	public Camera(double aPositionX, double aPositionY, double aPositionZ, double aTargetX, double aTargetY, double aTargetZ, double aZenithX, double aZenithY, double aZenithZ, double aFieldOfView, double aDepthOfView, int aScreenWidth, int aScreenHeight, Vector aTris, Vector aSpheres, int rec){
		position=new double[]{aPositionX,aPositionY,aPositionZ};
		target=new double[]{aTargetX,aTargetY,aTargetZ};
		zenith=new double[]{aZenithX,aZenithY,aZenithZ};
		fieldOfView=aFieldOfView;
		depthOfView=aDepthOfView;
		screenWidth=aScreenWidth;
		screenHeight=aScreenHeight;
//		cBuffer = new int[screenWidth][screenHeight][3];
		cBuffer = new int[screenWidth][][];
		for (int i=0; i<screenWidth; ++i) {
			cBuffer[i] = new int[screenHeight][3];
		}
		zBuffer = new double[screenWidth][screenHeight];
		tris = aTris;
		spheres = aSpheres;
		recDepth = rec;
	}
		

	public RenderThread getRenderThread(){
		int pixels;
		pixels = (screenHeight*screenWidth);
		deliveredThreads++;
		return new RenderThread(String.valueOf(deliveredThreads), pixels ,  this.position, this.target, this.zenith, this.screenWidth, this.screenHeight, this.fieldOfView, this.depthOfView, this.tris, this.spheres, this.recDepth, this);//Create the thread
	}


	/**
	 * Schreibt eine Screenpartition zurück in die Kamera-Puffer
	 * 
	 * @param aPart	Screenpartition die zurückgeschrieben wird
	 */
	public synchronized void writePixelsToBuffer(int[]w, int[]h, int[]r, int[]g, int[]b, double[]z){
		for(int i=0; i<w.length; i++){
			if(r!=null) this.cBuffer[w[i]][h[i]][0]=r[i];
			if(g!=null) this.cBuffer[w[i]][h[i]][1]=g[i];
			if(b!=null) this.cBuffer[w[i]][h[i]][2]=b[i];
			if(z!=null) this.zBuffer[w[i]][h[i]]=z[i];
		}
	}
	
	/**
	 * Gibt die Pixelkoordinaten(x und y Werte der Pixel) für einen Teil des Bildpuffers zurück.
	 * 
	 * @param aNumPartitions	Gibt an in wieviele Stücke der Bildpuffer zerlegt wird
	 * @param aOffset			Gibt an welches Stück zurückgegebenwird
	 * @return					Das int[2][] Array in dem die x und y Koordinaten gespeichert sind
	 */
	public int[][] getPixelsFromBuffer(int aNumPartitions, int aOffset){
		int numPixels = this.screenHeight*this.screenWidth;
		int length = numPixels/aNumPartitions;
		if(aOffset < numPixels%aNumPartitions)
			length++;
		int[][]result = new int [2][length];
		int k=0;
		for(int i=0; i<this.screenWidth; i++){
			for(int j=0; j<this.screenHeight; j++){
				if((j*this.screenWidth+i)%aNumPartitions == aOffset){
					result[0][k] = i;
					result[1][k] = j;
					k++;
				}
			}
		}
		return result;
	}
	
	public synchronized boolean getNextCoordinates(int [] x, int[] y){
		curX++;
		if(curX>=this.screenWidth){
			curX=0;
			curY++;
		}
		if(curY>=this.screenHeight)
			return false;
		x[0]=curX;
		y[0]=curY;
		return true;
	}
	
	/**
	 * Legt die Bildpuffer neu an.
	 */
	public void clearBuffers(){
		this.cBuffer = null; //new int[screenWidth][screenHeight][3];
		this.zBuffer = null; //new double[screenWidth][screenHeight];
	}
	
}

