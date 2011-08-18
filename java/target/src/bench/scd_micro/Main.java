package scd_micro;

public class Main {
	public static void main(String[] argv)
	{
		Main m = new Main();
		for(int i = 0; i < 100; i++) {
			m.run();
//			te = Native.rdMem(Const.IO_CNT);
			System.out.print("Iteration finished.");
			//System.out.println(te-ts-to);
		}
		System.out.println("Benchmark finished");
	}
	private FrameBuffer frameBuffer;
	private scd_micro.NoiseGenerator noiseGenerator;
	private scd_micro.TransientDetectorScopeEntry cd;

	public Main() {
		frameBuffer = new FrameBuffer();
		noiseGenerator = new NoiseGenerator();
		cd = new TransientDetectorScopeEntry(new StateTable(), Constants.GOOD_VOXEL_SIZE);		
	}
	public void init() {
		
	}
	public void run() {
		genFrame();
		RawFrame f = frameBuffer.getFrame();
		noiseGenerator.generateNoiseIfEnabled();

		cd.setFrame(f);
		cd.run();
	}
	byte callsigns_[] = new byte[50];
	int lengths_[] = new int[10];
	float positions_[] = new float[30];
	private void genFrame() {		
		frameBuffer.putFrame(positions_, lengths_, callsigns_);
	}	
}
