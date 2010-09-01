package wcet.dsvmfp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.classification.*;
import wcet.dsvmfp.model.smo.kernel.*;

public class TestSMO {
  static int m = 60;
  static int data_fp[][] = new int[m][];
  static int y_fp[] = new int[m];
  static int testdata_fp[][] = new int[m][];
  static int testlabel_fp[] = new int[m];

  // 0 belongs to positive
  static int errcnt = 0;
  static int time = 0;

  // Run this to see the whole program run
  // Notice that only the deplyyRT() method is RT enabled
  public static void goAll(){
	init();
	deployRT();
	report();
  }

  // non-real time inialization of SVM
  public static void init() {
    // DATA
    // int[][] traindata_fp = { {FP.intToFp(1)}, {FP.intToFp(3)},
    // {FP.intToFp(5)} };
    // int[] trainy_fp = { FP.intToFp(-1), FP.intToFp(+1), FP.intToFp(+1) };
    // int[] testdata_fp = { FP.intToFp(3)};//, FP.intToFp(0) };
    // new SMOBinaryClassifierFP();

    // Training instances
    // Remember to make same as in dsvm.test.smo.ServerData
    // Change these files for the four setups
    TrainingData2.assign(data_fp, y_fp);
    TestData2.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData2.assign(data_fp, y_fp);
//    dsvmfp.TestData2.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData3.assign(data_fp, y_fp);
//    dsvmfp.TestData3.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData4.assign(data_fp, y_fp);
//    dsvmfp.TestData4.assign(testdata_fp,testlabel_fp);

    SMOBinaryClassifierFP.setData_fp(data_fp);
    SMOBinaryClassifierFP.setY_fp(y_fp);

    // Train the model prior to deployment
    SMOBinaryClassifierFP.mainRoutine();
  }

  // Real-time part of SVM
  // This is the method that is to be called and analyzed from a WCA tool
  public static void deployRT(){



    for (int i = 0; i < m; i++) { // @WCA loop=60


      int starttime = Native.rd(Const.IO_US_CNT);
      int t = Native.rd(Const.IO_CNT);
      //System.out.println("---ALIVE1---" + i);
      //int smores = SMOBinaryClassifierFP.getFunctionOutputTestPointFP(testdata_fp[i]);;
      int smores = SMOBinaryClassifierFP.getFunctionOutputTestPointFP(testdata_fp[i]);;
      //System.out.println("---ALIVE2---" + i);
      t = Native.rd(Const.IO_CNT) - t;
      time += Native.rd(Const.IO_US_CNT)-starttime;
//      System.out.print("classification time cycles:");
//      System.out.println(t);
      if(smores<0 && testlabel_fp[i]>=0){
        errcnt++;
      }
      else if(smores >= 0 && testlabel_fp[i]<0){
        errcnt++;
      }
      //System.out.println(FP.fpToStr(SMOBinaryClassifierFP.getFunctionOutputTestPointFP(testdata_fp)));
    }
  }


  // Show testual output from the system (non-real time)
  public static void report()
  {
    System.out.println("---TESTING---");
    System.out.print("Error cnt:");
    System.out.println(errcnt);
    System.out.print("#sv");
    System.out.println(SMOBinaryClassifierFP.getSV());
    System.out.print("total time (classifying):");
    System.out.print(time);
    System.out.println(" us");
    System.out.print("per observation time (classifying):");
    System.out.print(time/m);
    System.out.println(" us");
  }

}
