//  Eratosthenes Sieve prime number benchmark in Java

package bench;

public class Sieve {

  public static void main(String[] args) {

		util.Dbg.initSer();
		runSieve();
  }

  static void runSieve() {
     int SIZE = 8190;
     boolean flags[] = new boolean[SIZE+1];
     int i, prime, k, iter, count;
     int iterations = 0;
//     double seconds = 0.0;
     int score = 0;
     long startTime, elapsedTime;

     startTime = System.currentTimeMillis();
     while (true) {
        count=0;
        for(i=0; i<=SIZE; i++) flags[i]=true;
        for (i=0; i<=SIZE; i++) {
           if(flags[i]) {
              prime=i+i+3;
              for(k=i+prime; k<=SIZE; k+=prime)
                 flags[k]=false;
              count++;
           }
        }
        iterations++;
        elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime >= 10000) break;
     }
score = iterations / (((int) elapsedTime)/1000) ;
util.Dbg.intVal(score);
/*
     seconds = elapsedTime / 1000.0;
     score = (int) Math.round(iterations / seconds);
     results1 = iterations + " iterations in " + seconds + " seconds";
     if (count != 1899)
        results2 = "Error: count <> 1899";
     else
        results2 = "Sieve score = " + score;
*/
  }


}
