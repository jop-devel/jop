package scd_micro;

public class Main {
	public static void main(String[] argv)
	{
		Main m = new Main();
		m.init();
		for(int i = 0; i < 100; i++) {
			m.run();
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
	public void run() {
		genFrame();
		RawFrame f = frameBuffer.getFrame();
		noiseGenerator.generateNoiseIfEnabled();

		cd.setFrame(f);
		cd.run();
	}
	byte callsigns_[] = new byte[RawFrame.MAX_SIGNS];
	int lengths_[] = new int[RawFrame.MAX_PLANES];
	float positions_[][] = new float[2][3 * RawFrame.MAX_PLANES];
	static int genCount = 0;
	public void init() {
		for(int i = 0; i < RawFrame.MAX_PLANES; i++) {
			lengths_[i] = 10;
			for(int j = 0; j < 10; j++) {
				callsigns_[i*10+j] = (byte) (i+j);
			}
		}
		for(int i = 0; i < 10; ++i) {
			positions_[0][i*3] = Constants.MIN_X;
			positions_[0][i*3+1] = Constants.MIN_Y;
			positions_[0][i*3+2] = Constants.MIN_Z;
		}
		for(int i = 0; i < 10; ++i) {
			positions_[1][i*3] = Constants.MAX_X;
			positions_[1][i*3+1] = Constants.MAX_Y;
			positions_[1][i*3+2] = Constants.MAX_Z;
		}
	}
	private void genFrame() {
		frameBuffer.putFrame(positions_[genCount++ % 2], lengths_, callsigns_);
	}	
}
