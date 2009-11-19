/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package jvmtest.tc;

import jvmtest.base.*;

public class TcInstrXloadXstore extends TestCase {
	
	/**
	 * Serialization Version UID 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Getter for the textual name of the TestCase
	 * @return Name of the test case
	 */
	public String getTestCaseName() {
		return "TcInstrXloadXstore";
	}
	
	/**
	 * Write the values of the fields to the Stream. This
	 * may be used to calculate the hash value of the object state 
	 * @param os Stream to write the data into
	 */
	public void writeToStream(ByteArrayOutputStreamEx os) {
		os.writeLong(serialVersionUID);
	}
	
	/* iload and istore */
	private boolean iLoad(TestCaseResult tcr) {
		int i0;
		int i1;
		int i2;
		int i3;
		int i4;
		
		i0=0;
		i1=1;
		i2=2;
		i3=3;
		i4=4;
		
		tcr.calcHashInt(i0);
		tcr.calcHashInt(i1);
		tcr.calcHashInt(i2);
		tcr.calcHashInt(i3);
		tcr.calcHashInt(i4);
		
		return (i0==0 &&
				i1==1 &&
				i2==2 &&
				i3==3 &&
				i4==4);
		
	}
	
	/* lload and lstore */
	private boolean lLoadEven(TestCaseResult tcr) {
		long l0;
		long l2;
		long l4;
		
		l0=0L;
		l2=2L;
		l4=4L;
		
		tcr.calcHashLong(l0);
		tcr.calcHashLong(l2);
		tcr.calcHashLong(l4);
		
		return (l0==0 &&
				l2==2 &&
				l4==4);
		
	}
	
	/* lload and lstore */
	private boolean lLoadOdd(TestCaseResult tcr) {
		int i0;
		long l1;
		long l3;
		long l5;
		
		i0=0;
		l1=1L;
		l3=3L;
		l5=5L;
		
		tcr.calcHashInt(i0);
		tcr.calcHashLong(l1);
		tcr.calcHashLong(l3);
		tcr.calcHashLong(l5);
		
		return (i0==0 &&
				l1==1 &&
				l3==3 &&
				l5==5);
		
	}
	
	/* iload, iload_w and istore, istore_w with more than 256 vars 
	private boolean iBig257Load(TestCaseResult tcr) {
		int i0 = 0;
		int i1 = 1;
		int i2 = 2;
		int i3 = 3;
		int i4 = 4;
		
		int i5;
		int i6;
		int i7;
		int i8;
		int i9;
		int i10;
		int i11;
		int i12;
		int i13;
		int i14;
		int i15;
		int i16;
		int i17;
		int i18;
		int i19;
		int i20;
		int i21;
		int i22;
		int i23;
		int i24;
		int i25;
		int i26;
		int i27;
		int i28;
		int i29;
		int i30;
		int i31;
		int i32;
		int i33;
		int i34;
		int i35;
		int i36;
		int i37;
		int i38;
		int i39;
		int i40;
		int i41;
		int i42;
		int i43;
		int i44;
		int i45;
		int i46;
		int i47;
		int i48;
		int i49;
		int i50;
		int i51;
		int i52;
		int i53;
		int i54;
		int i55;
		int i56;
		int i57;
		int i58;
		int i59;
		int i60;
		int i61;
		int i62;
		int i63;
		int i64;
		int i65;
		int i66;
		int i67;
		int i68;
		int i69;
		int i70;
		int i71;
		int i72;
		int i73;
		int i74;
		int i75;
		int i76;
		int i77;
		int i78;
		int i79;
		int i80;
		int i81;
		int i82;
		int i83;
		int i84;
		int i85;
		int i86;
		int i87;
		int i88;
		int i89;
		int i90;
		int i91;
		int i92;
		int i93;
		int i94;
		int i95;
		int i96;
		int i97;
		int i98;
		int i99;
		int i100;
		int i101;
		int i102;
		int i103;
		int i104;
		int i105;
		int i106;
		int i107;
		int i108;
		int i109;
		int i110;
		int i111;
		int i112;
		int i113;
		int i114;
		int i115;
		int i116;
		int i117;
		int i118;
		int i119;
		int i120;
		int i121;
		int i122;
		int i123;
		int i124;
		int i125;
		int i126;
		int i127;
		int i128;
		int i129;
		int i130;
		int i131;
		int i132;
		int i133;
		int i134;
		int i135;
		int i136;
		int i137;
		int i138;
		int i139;
		int i140;
		int i141;
		int i142;
		int i143;
		int i144;
		int i145;
		int i146;
		int i147;
		int i148;
		int i149;
		int i150;
		int i151;
		int i152;
		int i153;
		int i154;
		int i155;
		int i156;
		int i157;
		int i158;
		int i159;
		int i160;
		int i161;
		int i162;
		int i163;
		int i164;
		int i165;
		int i166;
		int i167;
		int i168;
		int i169;
		int i170;
		int i171;
		int i172;
		int i173;
		int i174;
		int i175;
		int i176;
		int i177;
		int i178;
		int i179;
		int i180;
		int i181;
		int i182;
		int i183;
		int i184;
		int i185;
		int i186;
		int i187;
		int i188;
		int i189;
		int i190;
		int i191;
		int i192;
		int i193;
		int i194;
		int i195;
		int i196;
		int i197;
		int i198;
		int i199;
		int i200;
		int i201;
		int i202;
		int i203;
		int i204;
		int i205;
		int i206;
		int i207;
		int i208;
		int i209;
		int i210;
		int i211;
		int i212;
		int i213;
		int i214;
		int i215;
		int i216;
		int i217;
		int i218;
		int i219;
		int i220;
		int i221;
		int i222;
		int i223;
		int i224;
		int i225;
		int i226;
		int i227;
		int i228;
		int i229;
		int i230;
		int i231;
		int i232;
		int i233;
		int i234;
		int i235;
		int i236;
		int i237;
		int i238;
		int i239;
		int i240;
		int i241;
		int i242;
		int i243;
		int i244;
		int i245;
		int i246;
		int i247;
		int i248;
		int i249;
		int i250;
		int i251;
		int i252;
		
		int i254 = 254;
		int i255 = 255;
		int i256 = 256;
		int i257 = 257;
		
		return (i0==0 &&
		        i1==1 &&
		        i2==2 &&
		        i3==3 &&
		        i4==4 &&
		        i254==254 &&
		        i255==255 &&
		        i256==256 &&
		        i257==257);
		
	}
    */
	
	private boolean iBig31Load(TestCaseResult tcr) {
		int i0=0;
		int i1=1;
		int i2=2;
		int i3=3;
		int i4=4;
		int i5=5;
		int i6=6;
		int i7=7;
		int i8=8;
		int i9=9;
		int i10=10;
		int i11=11;
		int i12=12;
		int i13=13;
		int i14=14;
		int i15=15;
		int i16=16;
		int i17=17;
		int i18=18;
		int i19=19;
		int i20=20;
		int i21=21;
		int i22=22;
		int i23=23;
		int i24=24;
		int i25=25;
		int i26=26;
		int i27=27;
		int i28=28;
		int i29=29;
		int i30=30;
		
		tcr.calcHashInt(i0);
		tcr.calcHashInt(i1);
		tcr.calcHashInt(i2);
		tcr.calcHashInt(i3);
		tcr.calcHashInt(i4);
		tcr.calcHashInt(i5);
		tcr.calcHashInt(i6);
		tcr.calcHashInt(i7);
		tcr.calcHashInt(i8);
		tcr.calcHashInt(i9);
		tcr.calcHashInt(i29);
		tcr.calcHashInt(i30);
		
		return (i0==0 &&
		        i1==1 &&
		        i2==2 &&
		        i3==3 &&
		        i4==4 &&
		        i5==5 &&
		        i6==6 &&
		        i7==7 &&
		        i8==8 &&
		        i9==9 &&
		        i10==10 &&
		        i11==11 &&
		        i12==12 &&
		        i13==13 &&
		        i14==14 &&
		        i15==15 &&
		        i16==16 &&
		        i17==17 &&
		        i18==18 &&
		        i19==19 &&
		        i20==20 &&
		        i21==21 &&
		        i22==22 &&
		        i23==23 &&
		        i24==24 &&
		        i25==25 &&
		        i26==26 &&
		        i27==27 &&
		        i28==28 &&
		        i29==29 &&
		        i30==30);		
	}

	private boolean lBig31Load(TestCaseResult tcr) {
		long l0=0L;
		long l1=1L;
		long l2=2L;
		long l3=3L;
		long l4=4L;
		long l5=5L;
		long l6=6L;
		long l7=7L;
		long l8=8L;
		long l9=9L;
		long l10=10L;
		long l11=11L;
		long l12=12L;
		long l13=13L;
		long l14=14L;
		
		tcr.calcHashLong(l0);
		tcr.calcHashLong(l1);
		tcr.calcHashLong(l2);
		tcr.calcHashLong(l3);
		tcr.calcHashLong(l4);
		tcr.calcHashLong(l5);
		tcr.calcHashLong(l6);
		tcr.calcHashLong(l7);
		tcr.calcHashLong(l8);
		tcr.calcHashLong(l9);
		tcr.calcHashLong(l10);
		tcr.calcHashLong(l11);
		tcr.calcHashLong(l12);
		tcr.calcHashLong(l13);
		tcr.calcHashLong(l14);
		
		return (l0==0 &&
		        l1==1 &&
		        l2==2 &&
		        l3==3 &&
		        l4==4 &&
		        l5==5 &&
		        l6==6 &&
		        l7==7 &&
		        l8==8 &&
		        l9==9 &&
		        l10==10 &&
		        l11==11 &&
		        l12==12 &&
		        l13==13 &&
		        l14==14);
	}

	private boolean fBig31Load(TestCaseResult tcr) {
		// only use binary exact representable values
		float f0=0;
		float f1=1;
		float f2=2;
		float f3=3;
		float f4=4;
		float f5=5;
		float f6=6;
		float f7=7;
		float f8=8;
		float f9=9;
		float f10=10;
		float f11=11;
		float f12=12;
		float f13=13;
		float f14=14;
		float f15=15;
		float f16=16;
		float f17=17;
		float f18=18;
		float f19=19;
		float f20=20;
		float f21=21;
		float f22=22;
		float f23=23;
		float f24=24;
		float f25=25;
		float f26=26;
		float f27=27;
		float f28=28;
		float f29=29;
		float f30=30;
		
		tcr.calcHashFloat(f0);
		tcr.calcHashFloat(f1);
		tcr.calcHashFloat(f2);
		tcr.calcHashFloat(f3);
		tcr.calcHashFloat(f4);
		tcr.calcHashFloat(f5);
		tcr.calcHashFloat(f6);
		tcr.calcHashFloat(f7);
		tcr.calcHashFloat(f8);
		tcr.calcHashFloat(f9);
		tcr.calcHashFloat(f29);
		tcr.calcHashFloat(f30);
		
		return (f0==0 &&
		        f1==1 &&
		        f2==2 &&
		        f3==3 &&
		        f4==4 &&
		        f5==5 &&
		        f6==6 &&
		        f7==7 &&
		        f8==8 &&
		        f9==9 &&
		        f10==10 &&
		        f11==11 &&
		        f12==12 &&
		        f13==13 &&
		        f14==14 &&
		        f15==15 &&
		        f16==16 &&
		        f17==17 &&
		        f18==18 &&
		        f19==19 &&
		        f20==20 &&
		        f21==21 &&
		        f22==22 &&
		        f23==23 &&
		        f24==24 &&
		        f25==25 &&
		        f26==26 &&
		        f27==27 &&
		        f28==28 &&
		        f29==29 &&
		        f30==30);		
	}
	
	private boolean dBig31Load(TestCaseResult tcr) {
		double d0=0;
		double d1=1;
		double d2=2;
		double d3=3;
		double d4=4;
		double d5=5;
		double d6=6;
		double d7=7;
		double d8=8;
		double d9=9;
		double d10=10;
		double d11=11;
		double d12=12;
		double d13=13;
		double d14=14;
		
		tcr.calcHashDouble(d0);
		tcr.calcHashDouble(d1);
		tcr.calcHashDouble(d2);
		tcr.calcHashDouble(d3);
		tcr.calcHashDouble(d4);
		tcr.calcHashDouble(d5);
		tcr.calcHashDouble(d6);
		tcr.calcHashDouble(d7);
		tcr.calcHashDouble(d8);
		tcr.calcHashDouble(d9);
		tcr.calcHashDouble(d10);
		tcr.calcHashDouble(d11);
		tcr.calcHashDouble(d12);
		tcr.calcHashDouble(d13);
		tcr.calcHashDouble(d14);
		
		return (d0==0 &&
		        d1==1 &&
		        d2==2 &&
		        d3==3 &&
		        d4==4 &&
		        d5==5 &&
		        d6==6 &&
		        d7==7 &&
		        d8==8 &&
		        d9==9 &&
		        d10==10 &&
		        d11==11 &&
		        d12==12 &&
		        d13==13 &&
		        d14==14);
	}
	
	/* aload and astor, references */
	private boolean rBig31Load(TestCaseResult tcr) {
		Object o0=new Integer(0);
		Object o1=new Integer(1);
		Object o2=new Integer(2);
		Object o3=new Integer(3);
		Object o4=new Integer(4);
		Object o5=new Integer(5);
		Object o6=new Integer(6);
		Object o7=new Integer(7);
		Object o8=new Integer(8);
		Object o9=new Integer(9);
		Object o10=new Integer(10);
		Object o11=new Integer(11);
		Object o12=new Integer(12);
		Object o13=new Integer(13);
		Object o14=new Integer(14);
		Object o15=new Integer(15);
		Object o16=new Integer(16);
		Object o17=new Integer(17);
		Object o18=new Integer(18);
		Object o19=new Integer(19);
		Object o20=new Integer(20);
		Object o21=new Integer(21);
		Object o22=new Integer(22);
		Object o23=new Integer(23);
		Object o24=new Integer(24);
		Object o25=new Integer(25);
		Object o26=new Integer(26);
		Object o27=new Integer(27);
		Object o28=new Integer(28);
		Object o29=new Integer(29);
		Object o30=new Integer(30);
		
		tcr.calcHashString(o0.toString());
		tcr.calcHashString(o1.toString());
		tcr.calcHashString(o2.toString());
		tcr.calcHashString(o3.toString());
		tcr.calcHashString(o4.toString());
		tcr.calcHashString(o5.toString());
		tcr.calcHashString(o6.toString());
		tcr.calcHashString(o7.toString());
		tcr.calcHashString(o8.toString());
		tcr.calcHashString(o9.toString());
		tcr.calcHashString(o29.toString());
		tcr.calcHashString(o30.toString());
		
		return (o0.equals(new Integer(0)) &&
		        o1.equals(new Integer(1)) &&
		        o2.equals(new Integer(2)) &&
		        o3.equals(new Integer(3)) &&
		        o4.equals(new Integer(4)) &&
		        o5.equals(new Integer(5)) &&
		        o6.equals(new Integer(6)) &&
		        o7.equals(new Integer(7)) &&
		        o8.equals(new Integer(8)) &&
		        o9.equals(new Integer(9)) &&
		        o10.equals(new Integer(10)) &&
		        o11.equals(new Integer(11)) &&
		        o12.equals(new Integer(12)) &&
		        o13.equals(new Integer(13)) &&
		        o14.equals(new Integer(14)) &&
		        o15.equals(new Integer(15)) &&
		        o16.equals(new Integer(16)) &&
		        o17.equals(new Integer(17)) &&
		        o18.equals(new Integer(18)) &&
		        o19.equals(new Integer(19)) &&
		        o20.equals(new Integer(20)) &&
		        o21.equals(new Integer(21)) &&
		        o22.equals(new Integer(22)) &&
		        o23.equals(new Integer(23)) &&
		        o24.equals(new Integer(24)) &&
		        o25.equals(new Integer(25)) &&
		        o26.equals(new Integer(26)) &&
		        o27.equals(new Integer(27)) &&
		        o28.equals(new Integer(28)) &&
		        o29.equals(new Integer(29)) &&
		        o30.equals(new Integer(30))
		        );		
	}
	
	/* *aload and *astore, arrays */
	private boolean aLoad(TestCaseResult tcr) {
		char[] Alphabet = {
				'a', 'b', 'c', 'd', 'e',
				'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o',
				'p', 'q', 'r', 's', 't',
				'u', 'v', 'w', 'x', 'y',
				'z'};
		
		byte[] byteArray = new byte[20];
		char[] charArray = new char[20];
		short[] shortArray = new short[20];
		int[] intArray = new int[20];
		long [] longArray = new long[20];
		float[] floatArray = new float[20];
		double[] doubleArray = new double[20];
		Object[] objArray = new Object[20];
		
		for (byte i=0;i<20;i++) {
			byteArray[i]=i;
			charArray[i]=Alphabet[i];
			shortArray[i]=i;
			intArray[i]=i;
			longArray[i]=i;
			floatArray[i]=i;
			doubleArray[i]=i;
			objArray[i] = new Integer(i);

			tcr.calcHashInt(byteArray[i]);
			tcr.calcHashInt(charArray[i]);
			tcr.calcHashInt(shortArray[i]);
			tcr.calcHashInt(intArray[i]);
			tcr.calcHashLong(longArray[i]);
			tcr.calcHashFloat(floatArray[i]);
			tcr.calcHashDouble(doubleArray[i]);
			tcr.calcHashString(objArray[i].toString());
		}
		
		boolean Result = true;
		for (int i=0;i<20;i++) {
			Result = 
				Result &&
				byteArray[i]==i &&
				charArray[i]==Alphabet[i] &&
				shortArray[i]==i &&
				intArray[i]==i &&
				longArray[i]==i &&
				floatArray[i]==i &&
				doubleArray[i]==i &&
				objArray[i].equals(new Integer(i));
		}
		
		return Result;		
	}
	
	private boolean aMulti2Load(TestCaseResult tcr) {
		byte[][] byteArray = new byte[20][10];
		short[][] shortArray = new short[20][10];
		int[][] intArray = new int[20][10];
		long [][] longArray = new long[20][10];
		float[][] floatArray = new float[20][10];
		double[][] doubleArray = new double[20][10];
		Object[][] objArray = new Object[20][10];

		for (int x=0;x<20;x++)
			for (int y=0;y<10;y++) {
				int val = x*10+y;

				byteArray[x][y] = (byte)val;
				shortArray[x][y] = (short)val;
				intArray[x][y] = val;
				longArray[x][y] = val;
				floatArray[x][y] = val;
				doubleArray[x][y] = val;
				objArray[x][y] = new Integer(val);

				tcr.calcHashInt(byteArray[x][y]);
				tcr.calcHashInt(shortArray[x][y]);
				tcr.calcHashInt(intArray[x][y]);
				tcr.calcHashLong(longArray[x][y]);
				tcr.calcHashFloat(floatArray[x][y]);
				tcr.calcHashDouble(doubleArray[x][y]);
				tcr.calcHashString(objArray[x][y].toString());
			}


		boolean Result = true;
		for (int x=0;x<20;x++)
			for (int y=0;y<10;y++) {
				int val = x*10+y;
				Result = 
					Result &&
					byteArray[x][y]==(byte)val &&
					shortArray[x][y]==(short)val &&
					intArray[x][y]==val &&
					longArray[x][y]==val &&
					floatArray[x][y]==val &&
					doubleArray[x][y]==val; // &&
					objArray[x][y].equals(new Integer(val));
			}

		return Result;
	}
	
	private boolean aMulti3Load(TestCaseResult tcr) {
		byte[][][] byteArray = new byte[20][10][7];
		short[][][] shortArray = new short[20][10][7];
		int[][][] intArray = new int[20][10][7];
		long [][][] longArray = new long[20][10][7];
		float[][][] floatArray = new float[20][10][7];
		double[][][] doubleArray = new double[20][10][7];
		Object[][][] objArray = new Object[20][10][7];
		
		for (int x=0;x<20;x++)
			for (int y=0;y<10;y++)
				for (int z=0;z<7;z++) {
					int val = x*100+y*10+z;
					
					byteArray[x][y][z] = (byte)val;
					shortArray[x][y][z] = (short)val;
					intArray[x][y][z] = val;
					longArray[x][y][z] = val;
					floatArray[x][y][z] = val;
					doubleArray[x][y][z] = val;
					objArray[x][y][z] = new Integer(val);
					
					tcr.calcHashInt(byteArray[x][y][z]);
					tcr.calcHashInt(shortArray[x][y][z]);
					tcr.calcHashInt(intArray[x][y][z]);
					tcr.calcHashLong(longArray[x][y][z]);
					tcr.calcHashFloat(floatArray[x][y][z]);
					tcr.calcHashDouble(doubleArray[x][y][z]);
					tcr.calcHashString(objArray[x][y][z].toString());
				}
		
		
		boolean Result = true;
		for (int x=0;x<20;x++)
			for (int y=0;y<10;y++)
				for (int z=0;z<7;z++) {
					int val = x*100+y*10+z;
					Result = 
						Result &&
						byteArray[x][y][z]==(byte)val &&
						shortArray[x][y][z]==(short)val &&
						intArray[x][y][z]==val &&
						longArray[x][y][z]==val &&
						floatArray[x][y][z]==val &&
						doubleArray[x][y][z]==val &&
						objArray[x][y][z].equals(new Integer(val));
				}

		return Result;
	}
	
	private Object getNull() {
		return null;
	}
	
	private boolean aExceptions(TestCaseResult tcr) {
		boolean Result = true;
		boolean catched;
		
		int[] array = (int[])getNull();
		Result = array== null;
		
		
		catched = false;
		try {
			array[2] = 5;
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			array[0] = 123;
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			array[-1] = 123;
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		array = new int[10];
		
		for (int i=0;i<10;i++) {
			array[i]=i*i;
		}

		catched = false;
		try {
			array[-1] = 123;
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));

		catched = false;
		try {
			if (array[-1]==1) {
				Result = false;
			}
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			if (array[10]==100) {
				Result = false;
			}
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));

		
		return Result;
	}
	
	private boolean aMulti2Exceptions(TestCaseResult tcr) {
		boolean Result = true;
		boolean catched;
		
		/* null tests in 1. dim */
		int[][] array = (int[][])getNull();
		Result = array == null;		
		
		catched = false;
		try {
			array[2] = new int[2];
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			array[0] = new int[0];
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			array[-1] = new int[1];
		} catch(NullPointerException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		
		/* null tests in 2. dim */
		array = new int[10][];
		for (int i=0;i<10;i++) 
			Result = Result && array[i]==null;	
		
		for (int i=0;i<10;i++) {
			catched = false;
			try {
				array[i][2] = 2;
			} catch(NullPointerException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));
			
			catched = false;
			try {
				array[i][0] = 0;
			} catch(NullPointerException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));
			
			catched = false;
			try {
				array[i][-1] = -1;
			} catch(NullPointerException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));	
			
			array[i]=new int[i*2];
		}
		
		
		
		for (int x=0;x<array.length;x++)
			for (int y=0;y<array[x].length;y++){
				array[x][y]=x*1000+y;
			}

		for (int x=0;x<array.length;x++)
			for (int y=0;y<array[x].length;y++){
				Result = Result && array[x][y]==x*1000+y;
			}
		
		catched = false;
		try {
			array[-1] = null;
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));

		catched = false;
		try {
			if (array[-1]==null) {
				Result = false;
			}
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		catched = false;
		try {
			if (array[10]==null) {
				Result = false;
			}
		} catch(IndexOutOfBoundsException np) {
			catched = true;			
		}
		Result = Result && catched;
		tcr.calcHashInt((Result ? 1:0));
		
		for (int x=0;x<array.length;x++) {
			catched = false;
			try {
				array[x][-1] = 0;
			} catch(IndexOutOfBoundsException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));

			catched = false;
			try {
				if (array[x][-1]==1) {
					Result = false;
				}
			} catch(IndexOutOfBoundsException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));
			
			catched = false;
			try {
				if (array[x][array[x].length]==100) {
					Result = false;
				}
			} catch(IndexOutOfBoundsException np) {
				catched = true;			
			}
			Result = Result && catched;
			tcr.calcHashInt((Result ? 1:0));
		}
		
		return Result;
	}
	
	
	/**
	 * Test case method
	 */
	public TestCaseResult run() {
		boolean Result;
		TestCaseResult FResult = TestCaseResultFactory.createResult();
		
		Result = 
			iLoad(FResult) &&
			lLoadEven(FResult) &&
			lLoadOdd(FResult) &&
			// iBig257Load(FResult) && // depend on JOP settings
			iBig31Load(FResult) &&
			lBig31Load(FResult) &&
			fBig31Load(FResult) &&
			dBig31Load(FResult) &&
			rBig31Load(FResult) &&
			aLoad(FResult) &&
			// aMulti2Load(FResult) && // raise an exception in jop-sim
			aExceptions(FResult) &&
			aMulti2Exceptions(FResult);
		
		FResult.calcResult(Result, this);

		return FResult;
	}

}