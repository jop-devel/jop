package jembench.parallel.raytrace;

public class MyVector {
	/**
	 * Gibt einen um den Faktor f skalierten Vektor v zurück.
	 * @param v		der Vector der skaliert wird
	 * @param f  	der Faktor mit dem der Vektor skaliert wird
	 * @return      der skalierte Vektor
	 */
	static void scale(double[]r, double[]a, double f){
		r[0] = a[0]*f;
		r[1] = a[1]*f;
		r[2] = a[2]*f;
	}
	
	/**
	 * Gibt den normalisierten Vektor von v zurück.
	 * @param v		der zu normalisierende Vektor
	 * @return		der normalisierte Vektor
	 */
	static void normalize(double[]r, double[]a){
		double l = length(r);
		r[0] = a[0]*(1/l);
		r[1] = a[1]*(1/l);
		r[2] = a[2]*(1/l);
	}
	

	/**
	 * Berechnet die Länge des Vektors v.
	 * @param v		Vektor dessen Länge zurückgegeben wird
	 * @return		die Länge des Vektors v
	 */
	static double length(double[]v){
		return (double)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
	}
	
	/**
	 * Multipliziert den Vektor v und die 3x3 Matrix M.
	 * @param a		der zu multiplizierende Vektor
	 * @param M		die zu multiplizierende Matrix
	 * @return		das Produkt von v und M
	 */
	static void mul_mtx(double[]r, double[]a, double[][]M){
		r[0] = a[0]*M[0][0]	+a[1]*M[0][1]	+a[2]*M[0][2]; 
		r[1] = a[0]*M[1][0]	+a[1]*M[1][1]	+a[2]*M[1][2]; 
		r[2] = a[0]*M[2][0]	+a[1]*M[2][1]	+a[2]*M[2][2];
	}
	
	/**
	 * Rotiert den Vektor v um die Achse ax um alpha Grad.
	 * @param a der zu rotierende Vektor
	 * @param ax die Rotationsachse
	 * @param alpha der Rotationswinkel(Grad)
	 * @return der rotierte Vektor
	 */
	static void rotate(double []r, double[]a, double[]ax2, double alpha){
		alpha = Math.toRadians(alpha);
	    normalize(ax2, ax2);
		double c = Math.cos(alpha);
	    double s = Math.sin(alpha);
	    //Rotationsmatrix:
	    double M[][] = {
	           {c+ax2[0]*ax2[0]*(1-c), 			  ax2[0]*ax2[1]*(1-c)-ax2[2]*s, 	  ax2[0]*ax2[2]*(1-c)+ax2[1]*s},
	           {  ax2[1]*ax2[0]*(1-c)+ax2[2]*s, 	c+ax2[1]*ax2[1]*(1-c), 			  ax2[1]*ax2[2]*(1-c)-ax2[0]*s},
	           {  ax2[2]*ax2[0]*(1-c)-ax2[1]*s, 	  ax2[2]*ax2[1]*(1-c)+ax2[0]*s, 	c+ax2[2]*ax2[2]*(1-c)}
	           };
	    mul_mtx(r, a, M);
	}

	/**
	 * Berechnet das Skalarprodukt zweier Vektoren.
	 * @param a	erster Vektor
	 * @param b zweiter Vektor
	 * @return	Skalarprodukt der beiden Vektoren
	 */
	static double dotProd(double[]a,double[]b){
		return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
	}

	/**
	 * Berechnet das Kreuzprodukt zweier Vektoren.
	 * @param a	erster Vektor
	 * @param b zweiter Vektor
	 * @return	Kreuzprodukt der beiden Vektoren
	 */
	static void crossProd(double[]r, double[]a, double[]b){
		r[0] = a[1]*b[2] - a[2]*b[1];
		r[1] = a[2]*b[0] - a[0]*b[2];
		r[2] = a[0]*b[1] - a[1]*b[0];
	}
	
	/**
	 * Addiert 2 Vektoren
	 * 
	 * @param a	erster Vektor
	 * @param b zweiter Vektor
	 * @return	Summer der beiden Vektoren
	 */
	static void add(double[]r, double[]a, double[]b){
		r[0] = a[0]+b[0];
		r[1] = a[1]+b[1];
		r[2] = a[2]+b[2];
	}

	/**
	 * Substrahiert 2 Vektoren
	 * 
	 * @param a erster Vektor
	 * @param b zweiter Vektor
	 * @return	Differenz der beiden Vektoren
	 */
	static void sub(double[]r, double[]a, double[]b){
		r[0] = a[0]-b[0];
		r[1] = a[1]-b[1];
		r[2] = a[2]-b[2];
	}
	
	/**
	 * Berechnet den Verbindungsvektor von 2 Ortsvektoren
	 * 
	 * @param a	erster Vektor
	 * @param b zweiter Vektor
	 * @return	Verbindungsvektor von a nach b
	 */
	static void to(double[]r, double[]a, double[]b){
		sub(r,b,a);
	}
	

	/**
	 * Berechnet den Ortsvektor eines Punktes auf einer Geraden
	 * 
	 * @param v	Ursprung der Geraden
	 * @param d	Richtungsvektor der Geraden
	 * @param t	Entfernung entlang der Geraden
	 * @return	Ortsvektor des Punktes v+dt
	 */
	static void line(double[]r, double[]p, double[]d, double t){
		scale(r, d, t);
		add(r, r, p);
	}
	
	/**
	 * Überprüft ob 2 Vektoren die selben Vorzeichen haben
	 * 
	 * @param a	erster Vektor
	 * @param b zweiter Vektor
	 * @return	haben beide Vektoren das selbe Vorzeichen ?
	 */
	static boolean sign(double[]a, double[]b){
		if((a[0]>=0 && b[0]>=0) || (a[0]<0 && b[0]<0)){
			if((a[1]>=0 && b[1]>=0) || (a[1]<0 && b[1]<0)){
				if((a[2]>=0 && b[2]>=0) || (a[2]<0 && b[2]<0)){
					return true;
				}
			}
		}
		return false;
	}
}
