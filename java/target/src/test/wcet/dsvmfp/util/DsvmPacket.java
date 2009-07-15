package wcet.dsvmfp.util;

/**
 * Only one packet is manipulated at a time.
 * 
 * @author rup.inf
 * GPL
 */
public class DsvmPacket {
	//
  // Commands
  //
	public static final int TRAININGDATAREQUEST = 1;

	public static final int TRAININGDATA = 2;

	public static final int TESTDATAREQUEST = 10; //0xa

	public static final int TESTDATA = 11; //0xb

	public static final int TESTDATAGUESS = 12; //0xc

	public static final int TESTDATAANSWER = 13; //0xd

	public static final int INIT = 14;
  
	public static final int END = 21;

	public static final int ALIVEREQUEST = 22;

	public static final int ALIVEREPLY = 23;

	// Options
	public static final int NOOPTION = 0;

	// DsvmPacket layout
	static final int COMMANDPOS = 0;

	static final int OPTIONPOS = 1;

	static final int IDPOS = 2;

	static final int HEADERLENGTH = 3;
	
	static final int LBLCOUNTOFF = 1;
	
	static final int LBLDATAOFF = 1;
	
	static final int DIMCOUNTOFF = 1; //+LBL COUNT

	static final int MAXLENGTH = 1500 / 4; // 375 words

	// See DataPacketUtil for the data exchange protocol

	//Integers. pLoad should be accessed through its utility methods
	public static int[] pLoad;

	public static int length;
	
	public static void init(){ // JOP said bytecode 187 was not supported so init is used to make the two objects
    pLoad = new int[MAXLENGTH];
    length = HEADERLENGTH; // just make a command size packet ready
	}

  //
  // Full length manipulators
  // 
  public static void setIntPayload(int[] payLoad) {
    DsvmUtilFP.arrayCopy(payLoad, 0, pLoad, 0, payLoad.length);
    length = payLoad.length;
  }
  
	/**
	 * Copy from payLoad intLength words from starting from offSet. This is used
   * when the interger based payload is copied from the Packet.
	 * @param payLoad
	 * @param intLength
	 * @param offSet
	 */
	public static void setIntPayload(int[] payLoad, int intLength, int offSet) {
	
//		System.out.println("info:"+payLoad.length +","+intLength+","+offSet);
//		for (int i = 0; i < intLength; i++) {
//			System.out.println(payLoad[i]+" i "+i);
//		}
		DsvmUtilFP.arrayCopy(payLoad, offSet, payLoad, 0, intLength);
//		for (int i = 0; i < intLength; i++) {
//			System.out.println("this:"+this.payLoad[i]+" i "+i);
//		}
		length = intLength;
	}

  public static int[] getPayloadInt() {
    int[] actualPayload = new int[length];
    DsvmUtilFP.arrayCopy(pLoad, 0, actualPayload, 0, length);
    return actualPayload;
  }
  
  /**
   * Used for example when a packet arrives over System.in
   * @param payLoad
   * @param byteLength
   * @param byteOffset
   */
	public static void setBytePayload(byte[] byteArray, int byteLength, int byteOffset) {
    for (int j = 0; j < byteLength/4; j++) {
      for (int i = 0, shift = 24; i < 4; i++, shift -= 8)
        pLoad[j] += (0xFF << shift) & (byteArray[j*4+byteOffset*4+i] << shift);
    }
    length = byteLength/4;
	}

  public static int getPayloadLen(){
    return length;
  }  

  public static byte[] getPayloadByte() {
    int[] temp = getPayloadInt();
    return DsvmUtilFP.intToByte(temp);
  }
  
	/**
   * "removes" data part and clears command, option and id fields
   */
	public static void clear(){
		for(int i=0;i<HEADERLENGTH;i++)
			pLoad[i]=0;
		length = HEADERLENGTH;
	}

  //
  // Get and set commands
  //
  public static int[] getData() {
    int[] data = new int[length - HEADERLENGTH];
    DsvmUtilFP.arrayCopy(pLoad, HEADERLENGTH, data, 0, data.length);
    return data;
  }
  
	public static int getCommand() {
		return pLoad[COMMANDPOS];
	}
  
  /**
   * Setting the command also resets the packet to HEADERLENGTH and clears the
   * option and id position fields. 
   * @param command
   */
	public static void setCommand(int command) {
    for(int i=0;i<HEADERLENGTH;i++){
      pLoad[i]=0;
    }

    length = HEADERLENGTH;
    pLoad[COMMANDPOS] = command;
	}

	public static void setLabelFP(int label_fp) {
		pLoad[HEADERLENGTH+LBLCOUNTOFF] = 1;
		pLoad[HEADERLENGTH+LBLCOUNTOFF+LBLDATAOFF] = label_fp;
		pLoad[HEADERLENGTH+LBLCOUNTOFF+LBLDATAOFF+DIMCOUNTOFF] = 0;
		length = HEADERLENGTH+LBLCOUNTOFF+LBLDATAOFF+DIMCOUNTOFF; //6 
	}

  public static int getLabelFP() {
    // Return first label
    int label_fp = pLoad[HEADERLENGTH+1];
    return label_fp;
  }

	public static void setData(int[] data) {
		DsvmUtilFP.arrayCopy(data, 0, pLoad, HEADERLENGTH, data.length);
		length = HEADERLENGTH + data.length;
	}

  /**
   * It creates a new array object. The returned ref can be saved in the data
   * array.
   * @param packetData
   * @return
   */
  public static int[] getDataFP() {
    int offSet = pLoad[0+HEADERLENGTH] + 1;
    int numDim = pLoad[offSet];
    offSet++;
    int[] data_fp = new int[numDim];
    for (int i = 0; i < numDim; i++) {
      data_fp[i] = pLoad[offSet + i * 2 + 1];
    }
    return data_fp;
  }
  
	public static int getId() {
		return pLoad[IDPOS];
	}

	public static void setId(int id) {
		pLoad[IDPOS] = id;
	}

	public static int getOption() {
		return pLoad[OPTIONPOS];
	}

	public static void setOption(int option) {
		pLoad[OPTIONPOS] = option;
	}

	// CONTENT PART OF PACKET (WHAT FOLLOWS THE HEADER)//
	// It will depend on which command the packet is carrying.

	// DATA SECTION (follow HEADER when data is sent)
	// This is a set of static methods to assist packing the content
	// part of the packet.
	// data packet format, positions are 32 bit words, arrays 0-based
	// [Size]:Name:Text
	// -----------------------------------------------------------
	// [1]:noLab:Number of labels
	// 0..noLab[1]:label_fp:Labels
	// [1]:noDim:Number of dimensions in data
	// 0..noDim[2]:dimPair_fp:Dimension numbers and fp values
	// -----------------------------------------------------------
	// Example: [1][-1][2][0][1.3][2][4.4] (use fp data when for real)
	// One label (-1), 3 dimensional data,
	// first dim = 1.3, second dim = 0, third dim = 4.4

	public static int[] makeLabelFP(int label_fp) {
		int[] payLoad = new int[3];
		// 0 dim
		payLoad[2] = 0;
		int[] labelLoad = labelToPayLoad(label_fp);
		DsvmUtilFP.arrayCopy(labelLoad, 0, payLoad, 0, labelLoad.length);
		return payLoad;
	}

	public static int[] makeDataFP(int[] data_fp) {
		int[] dataPayLoad = DsvmPacket.dataToPayLoad(data_fp);
		int[] payLoad = new int[1 + dataPayLoad.length];
		// No label
		payLoad[0] = 0;
		DsvmUtilFP.arrayCopy(dataPayLoad, 0, payLoad, 1, dataPayLoad.length);
		return payLoad;
	}

	public static int[] makeLabelandDataFP(int label_fp, int[] data_fp) {
		int[] labelPayLoad = labelToPayLoad(label_fp);
		int[] dataPayLoad = DsvmPacket.dataToPayLoad(data_fp);
		int[] payLoad = new int[labelPayLoad.length + dataPayLoad.length];
		DsvmUtilFP.arrayCopy(labelPayLoad, 0, payLoad, 0, labelPayLoad.length);
		DsvmUtilFP.arrayCopy(dataPayLoad, 0, payLoad, labelPayLoad.length,
				dataPayLoad.length);
		return payLoad;
	}

	private static int[] labelToPayLoad(int label_fp) {
		int[] payLoad = { 1, label_fp };
		return payLoad;
	}

  /**
   * Creates the sparse data vactors. 
   * TODO: make it write directly in the payload and not create objects
   * @param data_fp
   * @return
   */
	private static int[] dataToPayLoad(int[] data_fp) {
		int dimCount = 0;
		for (int i = 0; i < data_fp.length; i++)
			if (data_fp[i] != 0)
				dimCount++;
		int[] payLoad = new int[1 + 2 * dimCount];
		payLoad[0] = dimCount;
		int offSet = 1;
		dimCount = 0;
		for (int i = 0; i < data_fp.length; i++) {
			if (data_fp[i] != 0) {
				payLoad[offSet + dimCount * 2] = i;
				payLoad[offSet + dimCount * 2 + 1] = data_fp[i];
				dimCount++;
			}
		}
		return payLoad;
	}

}
