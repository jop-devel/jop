package gcinc;
public class typeA extends testObject{

		
	public boolean testYourself(int i){
		boolean isOk;
		isOk=myInt1==i && myInt2/i==i && (myInt3-myInt2)/i==i;
		return isOk;
		//return true;
	}
	public typeA(int i){
		myInt1=i;
		myInt2=i*i;
		myInt3=i*i + myInt2;
	}
}
