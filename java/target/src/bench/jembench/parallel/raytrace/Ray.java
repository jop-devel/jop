package jembench.parallel.raytrace;

import java.util.Vector;

public class Ray{
	
	public static double[] intersect(Sphere s, double[] rayPos, double[] rayDir, double dov, double[] tmp_1, double[][]tmp_3, double[][]tmp_6){
		double[] result = tmp_6[0];
		result[0] = -1;
		double[] c = tmp_3[2];
		c = s.position;
		double[] p0_sub_c = tmp_3[3]; 
		p0_sub_c[0] = rayPos[0]-c[0];
		p0_sub_c[1] = rayPos[1]-c[1];
		p0_sub_c[2] = rayPos[2]-c[2];
		double ra = tmp_1[1];
		ra = s.radius;
		double A = tmp_1[2];
		double B = tmp_1[3];
		double C = tmp_1[4];
		double D = tmp_1[5];
		double t1 = tmp_1[1];
		double t2 = tmp_1[1];
		
		A = rayDir[0]*rayDir[0] + rayDir[1]*rayDir[1] + rayDir[2]*rayDir[2];
		B = 2 * (rayDir[0]*p0_sub_c[0] + rayDir[1]*p0_sub_c[1] + rayDir[2]*p0_sub_c[2]);
		C = (p0_sub_c[0]*p0_sub_c[0] + p0_sub_c[1]*p0_sub_c[1] + p0_sub_c[2]*p0_sub_c[2]) - ra*ra;
		D = B*B - 4*A*C;

		t1=-1;
		t2=-1;
		
		if(D < 0){//Kein Schnittpunkt
			t1=-1;
		}
		if(D == 0){//Ein Schnittpunkt
			t1 = ( -B - Math.sqrt(D)) / (2.0 * A);
		}
		if(D > 0){//2 Schnittpunkte
			t1 = (-B - Math.sqrt(D))/(2.0*A);
			t2 = (-B + Math.sqrt(D))/(2.0*A);
			if(t2 < t1 && t2 > 0)
				t1=t2;
		}
		if((result[0] < 0 && t1 > 0 && t1 < dov) || (t1 < result[0] && t1 > 0 && t1 < dov)){
			result[0] = t1;
			result[1] = s.rgb[0];
			result[2] = s.rgb[1];
			result[3] = s.rgb[2];
			return result;
		}
		result[0] = -1;
		return result;
	}
	
	public static double[] intersect(Vector sceneObjects, double[] rayPosition, double[] rayDirection, double dov, double[] tmp_1, double[][]tmp_3, double[][]tmp_6, int numRecursions){
		double[]rayPos = rayPosition;
		double[]rayDir = rayDirection;
		double[]resultTmp = tmp_6[0];
		double[]result = new double[6];
		double[]result2= new double[6];
		double ref=0;
		result[0] = -1;
		Object nearestObject = null;
		
		// for(Object o:sceneObjects) { 
		for (int i=0; i<sceneObjects.size(); ++i) {
			Object o = sceneObjects.elementAt(i);
			
			if(o instanceof Sphere)
				resultTmp = intersect((Sphere)o, rayPos, rayDir, dov, tmp_1, tmp_3, tmp_6);
			else if(o instanceof Tri)
				resultTmp = intersect((Tri)o, rayPos, rayDir, dov, tmp_1, tmp_3, tmp_6);
			
			if(resultTmp[0] != -1){//Treffer ?
				if((resultTmp[0]>0 && resultTmp[0] < result[0]) || (resultTmp[0]>0 && result[0] <= 0)){ //nähester Treffer ?
					nearestObject = o;
					ref = 0;
					if(o instanceof Sphere)
						ref = ((Sphere)o).reflection;
					else if(o instanceof Tri)
						ref = ((Tri)o).reflection;
					result[0] = 			  resultTmp[0];
					result[1] = (int)((1-ref)*resultTmp[1]);
					result[2] = (int)((1-ref)*resultTmp[2]);
					result[3] = (int)((1-ref)*resultTmp[3]);
				}
			}
		}
		
		if(numRecursions>0 && nearestObject!=null){
			double[] refPos = new double[3];
			double[] refDir = new double[3];
			MyVector.scale(refPos, rayDir, result[0]-0.001);//Reflektionsort bestimmen
			MyVector.add(refPos, refPos, rayPos);
			if(nearestObject instanceof Sphere)
				refDir = ((Sphere)nearestObject).reflect(rayPos, rayDir);//Reflektionsrichtung bestimmen
			else if(nearestObject instanceof Tri)
				refDir = ((Tri)nearestObject).reflect(rayPos, rayDir);//Reflektionsrichtung bestimmen
			result2 = intersect(sceneObjects, refPos, refDir, dov, tmp_1, tmp_3, tmp_6, numRecursions-1);
			if(result2[0]>0){
				result[1] += (int)((ref)*result2[1]);
				result[2] += (int)((ref)*result2[2]);
				result[3] += (int)((ref)*result2[3]);
			}
		}
		return result;
	}
	
	public static double[] intersect(Tri tri, double[] rayPos, double[]rayDir, double dov, double[] tmp_1, double[][]tmp_3, double[][]tmp_6){
		double[] result = tmp_6[0];
		double[] pl = tmp_6[0];
		double[] lb = tmp_3[2];
		double[] tmp1 = tmp_3[3];
		double[] tmp2 = tmp_3[4];
		double t = tmp_1[0];
		//Schneidet der Strahl die Ebene des Dreiecks ?
		tri.getPlane(pl, tmp_3[2], tmp_3[3], tmp_3[4]);
		MyVector.add(lb, rayPos, rayDir);
		MyVector.scale(tmp1,pl,-1);
		double d = tmp_1[1];
		d = tmp1[0]*pl[3] + tmp1[1]*pl[4] + tmp1[2]*pl[5];
		MyVector.sub(tmp1,lb,rayPos);
		double D = tmp_1[2];
		D = tmp1[0]*pl[3] + tmp1[1]*pl[4] + tmp1[2]*pl[5];
		
		//MyVector wegmachen!
		if(D!=0){//Strahl schneidet die Dreiecksebene:
			t = (-d-(rayPos[0]*pl[3]+rayPos[1]*pl[4]+rayPos[2]*pl[5]))/D;
			double[]v = tmp_3[5];
			MyVector.scale(v, rayDir, t);
			MyVector.add(v,v,rayPos);
			
			MyVector.sub(tmp1,v,tri.a);
			MyVector.sub(tmp2, tri.b, tri.a);
			double[] avXab = tmp_3[6];
			MyVector.crossProd(avXab, tmp1, tmp2);
			
			MyVector.sub(tmp1,v,tri.b);
			MyVector.sub(tmp2,tri.c,tri.b);
			double[] bvXbc = tmp_3[7];
			MyVector.crossProd(bvXbc, tmp1, tmp2);
			
			MyVector.sub(tmp1,v,tri.c);
			MyVector.sub(tmp2,tri.a,tri.c);
			double[] cvXca = tmp_3[8];
			MyVector.crossProd(cvXca, tmp1, tmp2);
			//Zeigen alle in die selbe Richtung ? == Liegt der Schnittpunkt im Dreieck ?
			if(MyVector.sign(avXab, bvXbc) && MyVector.sign(bvXbc, cvXca)){
				result[0] = t;
				result[1] = tri.rgb[0];
				result[2] = tri.rgb[1];
				result[3] = tri.rgb[2];
				return result;
			}
		}
		result[0] = -1;
		return result;
	}

}
