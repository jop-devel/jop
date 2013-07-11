package scjlibs;

public class Thrower implements Runnable{
	
	int[] nums;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("Thrower exec...");
		
		if (nums.length > 10) {
			throw new PropagatedException();
		}

		System.out.println("Ok");
		
	}
	
	public void setNums(int[] nums){
		this.nums = nums;
	}

}
