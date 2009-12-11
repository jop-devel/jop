package wcet.dsvmfp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import wcet.dsvmfp.model.smo.classification.SMOBinaryClassifierFloat;

public class TestSMOFloat {

    // control  if the benchmark is done in parallel or not
    public static final boolean PARALELSVM = true;

	static int m; // numbers of rows in the data matrix
	static float data_fp[][];
	static float y_fp[];
	// TODO
	static float testdata_fp[][] = new float[m][];
	static float testlabel_fp[] = new float[m];

	// 0 belongs to positive
	static int errcnt = 0;
	static int time = 0;
	static int cycles = 0;

	static SMOBinaryClassifierFloat smo = new SMOBinaryClassifierFloat();;

	// Run this to see the whole program run
	// Notice that only the deplyyRT() method is RT enabled
	public static void goAll() {
		init();
		deployRT();
		report();
	}

	public static void main(String args[]) {
		//init();
		goAll();
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
		// SVMData d = new TrainingData1Float();
		// data_fp = d.getTrainingData();
		// y_fp = d.getTrainingLabels();
		// m = y_fp.length;

		// TrainingData1Float.assign(data_fp, y_fp);
		// TestData2.assign(testdata_fp,testlabel_fp);
		// dsvmfp.TrainingData2.assign(data_fp, y_fp);
		// dsvmfp.TestData2.assign(testdata_fp,testlabel_fp);
		// dsvmfp.TrainingData3.assign(data_fp, y_fp);
		// dsvmfp.TestData3.assign(testdata_fp,testlabel_fp);
		// dsvmfp.TrainingData4.assign(data_fp, y_fp);
		// dsvmfp.TestData4.assign(testdata_fp,testlabel_fp);

		// Data id = new IrisFlowerData();
		Data id = new WeatherData();
		id.toString();
		float data[][] = id.getData();
        int[] datacols = new int[] { 0, 1, 2, 3, 4, 5 };
		data_fp = getDataDim(data, datacols);
		int targetIndex = 6;
		float positiveID = 1.0f;
		y_fp = getTarget(data, targetIndex, positiveID);
		m = y_fp.length;

		smo.setData_fp(data_fp);
		smo.setY_fp(y_fp);

		// Train the model prior to deployment
		smo.mainRoutine();
	}

	/**
	 * Get the data out of the data matrix.
	 *
	 * @param data
	 *            datamatrix
	 * @param dims
	 *            array with indicies of the desired vectors
	 * @return new datamatrix
	 */
	static float[][] getDataDim(float[][] data, int[] dims) {
		int r = data.length;
		int c = data[0].length;
		int newc = dims.length;
		float[][] newdata = new float[r][newc];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < newc; j++) {
				newdata[i][j] = data[i][dims[j]];
			}
		}
		return newdata;
	}

	/**
	 * Get the target vector and convert it to a binary target.
	 *
	 * @param data
	 *            datamatrix
	 * @param targetdim
	 *            index of target vector (last index)
	 * @param positiveClassID
	 *            id of the positive class, which will be +1 and the rest will
	 *            be -1
	 * @return target vector
	 */
	static float[] getTarget(float[][] data, int targetdim,
			float positiveClassID) {
		int r = data.length;
		float[] target = new float[r];
		for (int i = 0; i < r; i++) {
			if (data[i][targetdim] == positiveClassID) {
				target[i] = +1.0f;
			} else {
				target[i] = -1.0f;
			}
		}
		return target;
	}

	// Real-time part of SVM
	// This is the method that is to be called and analyzed from a WCA tool
	public static void deployRT() {
		time = 0;
		cycles = 0;

		for (int i = 0; i < m; i++) { // @WCA loop=14

			int starttime = Native.rd(Const.IO_US_CNT);
			int t = Native.rd(Const.IO_CNT);

			// int smores =
			float smores = smo.getFunctionOutputFloat(i, TestSMOFloat.PARALELSVM);
			if (smores < 0 && y_fp[i] >= 0) {
				errcnt++;
			} else if (smores >= 0 && y_fp[i] < 0) {
				errcnt++;
			}

			t = Native.rd(Const.IO_CNT) - t;
			cycles += t;
			time += Native.rd(Const.IO_US_CNT) - starttime;
			//System.out.print("classification time cycles:");
			//System.out.println(t);
		}
	}

	// Show testual output from the system (non-real time)
	public static void report() {
		System.out.println("---TESTING---");
		System.out.print("Error cnt:");
		System.out.println(errcnt);
		System.out.print("#sv");
		System.out.println(smo.getSV());

		System.out.print("total cycles (classifying):");
		System.out.print(cycles);
		System.out.println(" cycles");
		System.out.print("per observation cycles (classifying):");
		System.out.print(cycles / m);
		System.out.println(" cycles");

		System.out.print("total time (classifying):");
		System.out.print(time);
		System.out.println(" us");
		System.out.print("per observation time (classifying):");
		System.out.print(time / m);
		System.out.println(" us");
	}

}
