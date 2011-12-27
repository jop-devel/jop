// TODO: add author info and GPL copyright

package jembench.parallel.raytrace;

import java.util.Vector;

import jembench.ParallelBenchmark;

public class Raytrace extends ParallelBenchmark { // implements Runnable {

	  private final static int SCREEN_HEIGHT = 2;
	  private final static int SCREEN_WIDTH = 3;
	  
	  private Camera cam;
	  
	  
	  public Raytrace(){
			int recDepth = 100;
			//Vector  tris = Tri.createRandomTris(NUM_OBJECTS/2, 2, 8);//Add objects to the scene
			//Vector  spheres = Sphere.createRandomSpheres(NUM_OBJECTS/2, 2, 8);//Add objects to the scene
			Vector  tris = Tri.createTris();//Add 5 well defined tris to the scene
			Vector  spheres = Sphere.createSpheres();//Add 5 well defined sheres to the scene
			cam = new Camera(10,0,0,0,0,0,0,0,1,70,20,SCREEN_WIDTH,SCREEN_HEIGHT,tris,spheres,recDepth);
	  }
	  
	  public Runnable getWorker(){
		  cam.curX=0;
		  cam.curY=0;
		  return cam.getRenderThread();
	  }

		public String toString() {
			return "Raytrace";
		}

		
}
