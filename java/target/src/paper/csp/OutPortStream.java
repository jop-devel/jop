package csp;

import java.io.DataOutputStream;
import java.io.IOException;

public class OutPortStream implements OutPort {
	DataOutputStream os;
	int dstID;
	
	public OutPortStream(int dstID, DataOutputStream os) {
		this.os = os;
		this.dstID = dstID;
	}
	
	
	public void noAck_send(int buffer[], int cnt) {
		try {
			os.writeInt(cnt+1); // account for header
			os.writeInt(dstID);
			for(int i=0;i<cnt;i++)
				os.writeInt(buffer[i]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
