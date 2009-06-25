// project: spcmp paper
// example: bubble sort
// author:  Raimund Kirner, 22.06.2009


package sp;

/**
 * A clas to experiment with the Java language.
 * Nothing meaningful in the code.
 *
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 */
class RK_Test {
    final int SIZE = 20;
    Integer IV=5;
    int i;

    // Standard Constructor 
    public RK_Test() {
    }

    // read data from mem
    public void read() {
    } 

    // write data back to mem
    public void write() {
	System.out.println(IV);
    }

    // sort routine
    public void sort() {
    }

    public void testInteger() {
	Integer IVar = IV;
	IVar = 2;
    }


    public static void main(String[] args) {
	RK_Test rk = new RK_Test();

	rk.read();

	rk.testInteger();

	rk.sort();

	rk.write();
    }
}



