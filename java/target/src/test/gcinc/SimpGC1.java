package gcinc;
public class SimpGC1 {

	
	public void createObjects() {
		int i=1;
		while(true) {
		new myObject();
		System.out.print(i);
		System.out.print(" ");
		i++;
		for(int j=0;j<20000;j++)
			{
			}
		}
	}

	public static void main(String[] args) {
		SimpGC1 sgc= new SimpGC1();
		sgc.createObjects();
	}

}
