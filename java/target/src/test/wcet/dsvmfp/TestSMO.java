package wcet.dsvmfp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.classification.*;
import wcet.dsvmfp.model.smo.kernel.*;

public class TestSMO {

  public static void start() {
    // DATA
    // int[][] traindata_fp = { {FP.intToFp(1)}, {FP.intToFp(3)},
    // {FP.intToFp(5)} };
    // int[] trainy_fp = { FP.intToFp(-1), FP.intToFp(+1), FP.intToFp(+1) };
    // int[] testdata_fp = { FP.intToFp(3)};//, FP.intToFp(0) };
    // new SMOBinaryClassifierFP();

    // Training instances
    // Remember to make same as in dsvm.test.smo.ServerData
    int m = 60;
    int data_fp[][] = new int[m][];
    int y_fp[] = new int[m];
    int testdata_fp[][] = new int[m][];
    int testlabel_fp[] = new int[m];
    // Change these files for the four setups
    TrainingData1.assign(data_fp, y_fp);
    TestData1.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData2.assign(data_fp, y_fp);
//    dsvmfp.TestData2.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData3.assign(data_fp, y_fp);
//    dsvmfp.TestData3.assign(testdata_fp,testlabel_fp);
//    dsvmfp.TrainingData4.assign(data_fp, y_fp);
//    dsvmfp.TestData4.assign(testdata_fp,testlabel_fp);

    SMOBinaryClassifierFP.setData_fp(data_fp);
    SMOBinaryClassifierFP.setY_fp(y_fp);

    //SMOBinaryClassifierFP.mainRoutine();

    int errcnt = 0;
    // 0 belongs to positive
    int time = 0;
    for (int i = 0; i < m; i++) { // @WCA loop=2
      int starttime = Native.rd(Const.IO_US_CNT);
      int t = Native.rd(Const.IO_CNT);
      int smores = SMOBinaryClassifierFP.getFunctionOutputTestPointFP(testdata_fp[i]);;
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
}
