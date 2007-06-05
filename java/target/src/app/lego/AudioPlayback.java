package lego;

import lego.utils.TrivialFS;

public class AudioPlayback
{

//	static final int SAMPLERATE = 22050;
//	static final int SAMPLERATE = 48000;
	static final int SAMPLERATE = 44100;

	static final int US_PER_SAMPLE = 23; // more correct for 44100 Hz 
	//1000000/SAMPLERATE;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		new RtThread(10, 1000000/SAMPLERATE)
//		{
//		public void run()
		{
			System.out.print("Samplerate: ");
			System.out.println(SAMPLERATE);
			System.out.print("us/sample: ");
			System.out.println(US_PER_SAMPLE);

			while (true)
			{
				int count = TrivialFS.getFileCount();

				for (int i = 0; i < count; i++)
					lego.utils.AudioPlayback.playFromTrivialFS(i, US_PER_SAMPLE);
			}
//			}
//			};

//			RtThread.startMission();
//			}
		}
	}
}