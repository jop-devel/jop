package embjbench;

class DoAll {

	public static void main(String[] args) {

		Execute.perform(new BenchMark());
		Execute.perform(new BenchIinc());
		Execute.perform(new BenchLdc());
		Execute.perform(new BenchInvoke());
for (;;);
	}
			
}
