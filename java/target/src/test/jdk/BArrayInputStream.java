package jdk;

import jvm.TestCase;
import java.io.*;

public class BArrayInputStream extends TestCase {

	public String getName() {
		return "BArrayInputStream";
	}

	public boolean test() {
		boolean Ok=true;
		ByteArrayInputStream myStream1,myStream2;
		byte mySource1[], mySource2[], myTarget[];

		mySource1=new byte[20];
		mySource2=new byte[20];
		myTarget=new byte[20];
		
		
		for(byte i=0;i<20;i++){
			mySource1[i]=i;
			mySource2[i]=i;
			
		}
		
		myStream1 = new ByteArrayInputStream(mySource1);
		myStream2 = new ByteArrayInputStream(mySource2,0,10);
		
		
		//public synchronized int read()
		for(int i=0;i<20;i++){
			Ok=Ok&& myStream1.read()==i;
			if(i<10){
			Ok=Ok&& myStream2.read()==i;
			}
		}
		Ok=Ok&& myStream1.read()==-1;
		Ok=Ok&& myStream2.read()==-1;
		
		//mark(int m) and reset()
		
		//myStream1.mark(13);
		myStream1.reset();
		myStream2.reset();
		//System.out.println(myStream1.read());
		
		for(int i=0;i<10;i++){
			Ok=Ok&& myStream1.read()==i;
			Ok=Ok&& myStream2.read()==i;
		}
		Ok=Ok&& myStream1.available()==10;
		Ok=Ok&& myStream2.available()==0;
		
		for(int i=10;i<20;i++){
			Ok=Ok&& myStream1.read()==i;
		}
		Ok=Ok&& myStream1.read()==-1;
		Ok=Ok&& myStream2.read()==-1;
		
		
		//skip()
		myStream1.reset();
		myStream1.skip(10);
		for(int i=10;i<20;i++){
			Ok=Ok&& myStream1.read()==i;
		}
		myStream1.reset();
		
		
		//read(byte b[],int off,int len)
		//note:the off parameter is the offset in the target array
		myStream1.read(myTarget,8,11);
		for(int i=0;i<11;i++){
			Ok=Ok&& myTarget[i+8]==i;
		}
		
		return Ok;
		
		
	}

}
