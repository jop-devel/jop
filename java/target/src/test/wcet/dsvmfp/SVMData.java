package wcet.dsvmfp;

public interface SVMData {
	float [][] getTrainingData();
	float [] getTrainingLabels();
	float [][] getTestData();
	float [] getTestLabels();

}
