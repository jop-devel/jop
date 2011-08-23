// Temp test code
package wcet.devel;

public class WCADevel {

  static public int dowcet() {
	for(int j = 0; j < 5; j++) // @WCA loop=5
	  bar();

	return 0;
  }

   static void bar() {
       int i = 0;
       i = i + 1;
   }


}

/* saving a copy
public class WCADevel {

  static public int dowcet() {
	//A a = new A();
    A a = new B();

    bar(a);

	return 0;
  }

   static void bar(A a) {
       a.foo();
   }


}

//a can now be an A or it can also be a B. So
//either A.foo() or B.foo() is invoked. We don't know,
//so we have to consider both possibilities for the WCET


// from ms mail
class A {

   void foo() {
     int ia = 1;
   }
}

class B extends A {

   // overwrites A.foo()
   void foo() {
     int ib = 3;
     ib++;
     ib--;
   }

}
*/