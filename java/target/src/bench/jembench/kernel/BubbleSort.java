package jembench.kernel;

import jembench.SerialBenchmark;

public class BubbleSort extends SerialBenchmark {

	private final static int  SIZE2 = 10;

	private final int numbers[];

	public BubbleSort() {
		numbers = new int[2*SIZE2];
	}

	public int perform(int cnt) {

		int i;
		int s = SIZE2;
		int ar[] = numbers;
		int tmp;
		boolean repeat;
		
		for (int j=0; j<cnt; ++j) {
			//fill up array {0 2 4 ... 5 3 1}
			for(i=0;i<s;i++){
				ar[i]=2*i;
				ar[s-i]=(2*i)+1;
			}
			repeat=true;
			s=(2*SIZE2)-1;
			while(repeat){
				repeat=false;
				for(i=0;i<s;i++)
					if(ar[i]>ar[i+1]){
						tmp=ar[i];
						ar[i]=ar[i+1];
						ar[i+1]=tmp;
						repeat=true;
					}
			}
		}

		return 0;
	}


	public String toString() {

		return "BubbleSort";
	}

}
