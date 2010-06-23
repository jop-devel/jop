package wcet.dsvmfp;

/*
 @relation weather

 @attribute outlook {sunny, overcast, rainy}
 @attribute temperature real
 @attribute humidity real
 @attribute windy {TRUE, FALSE}
 @attribute play {yes, no}

 @data
 sunny,85,85,FALSE,no
 sunny,80,90,TRUE,no
 overcast,83,86,FALSE,yes
 rainy,70,96,FALSE,yes
 rainy,68,80,FALSE,yes
 rainy,65,70,TRUE,no
 overcast,64,65,TRUE,yes
 sunny,72,95,FALSE,no
 sunny,69,70,FALSE,yes
 rainy,75,80,FALSE,yes
 sunny,75,70,TRUE,yes
 overcast,72,90,TRUE,yes
 overcast,81,75,FALSE,yes
 rainy,71,91,TRUE,no
 */
//14 data points
public class WeatherData extends Data {
	private static float data[][] = {
			// nominals are binarized, reals are normalized,
			// and booleans are also binarized
			// rainy temp humi windy play
			{ 1f, 0f, 0f, 1.000f, 0.645f, 0f, 0f },
			{ 1f, 0f, 0f, 0.762f, 0.806f, 1f, 0f },
			{ 0f, 1f, 0f, 0.905f, 0.677f, 0f, 1f },
			{ 0f, 0f, 1f, 0.286f, 1.000f, 0f, 1f },
			{ 0f, 0f, 1f, 0.190f, 0.484f, 0f, 1f },
			{ 0f, 0f, 1f, 0.048f, 0.161f, 1f, 0f },
			{ 0f, 1f, 0f, 0.000f, 0.000f, 1f, 1f },
			{ 1f, 0f, 0f, 0.381f, 0.968f, 0f, 0f },
			{ 1f, 0f, 0f, 0.238f, 0.161f, 0f, 1f },
			{ 0f, 0f, 1f, 0.524f, 0.484f, 0f, 1f },
			{ 1f, 0f, 0f, 0.524f, 0.161f, 1f, 1f },
			{ 0f, 1f, 0f, 0.381f, 0.806f, 1f, 1f },
			{ 0f, 1f, 0f, 0.810f, 0.323f, 0f, 1f },
			{ 0f, 0f, 1f, 0.333f, 0.839f, 1f, 0f }, };

	public float[][] getData() {
		return data;
	}

	public static void main(String[] args) {

		Data data = new WeatherData();

		System.out.println(data);
	}
}
