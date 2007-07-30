package gcinc;
public class typeB implements testObject{
	int myInt1,myInt2,myInt3;
	
	public boolean testYourself(int i){
		boolean isOk;	
		isOk= (myInt1/i)/i==i && (myInt2-i)/4==i && (((myInt3-6)/i)-5)/(4*i)==i;  
		return true;	
	}
	
	public typeB(int i){
		myInt1=i*i*i;
		myInt2=4*i+i;
		myInt3=4*i*i+5*i+6;
		
	}
		
}
