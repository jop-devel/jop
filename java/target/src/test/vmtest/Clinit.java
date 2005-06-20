/*
 * Created on 26.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package vmtest;

public class Clinit {

static int abc = 123;
static int def = 456;

static int[] a = { 0, 1, 2, 3, 4, 5, -1 };

static int[] b = { 123, -123, 456, -456, 50000, -50000 };

	public static void main(String[] args) {
		

		boolean ok = true;

		System.out.println("Hello");
		int val = abc+def;
		System.out.println(val);

		if (val!=123+456) ok = false;

		if (a[0]!=0) ok = false;
		if (a[1]!=1) ok = false;
		if (a[2]!=2) ok = false;
		if (a[3]!=3) ok = false;
		if (a[4]!=4) ok = false;
		if (a[5]!=5) ok = false;
		if (a[6]!=-1) ok = false;

		if (b[0]!=123) ok = false;
		if (b[1]!=-123) ok = false;
		if (b[2]!=456) ok = false;
		if (b[3]!=-456) ok = false;
		if (b[4]!=50000) ok = false;
		if (b[5]!=-50000) ok = false;

		System.out.print("Clinit test ");
		if (ok) {
			System.out.println("ok");
		} else {
			System.out.println("FAILED!");
		}


	}
}
