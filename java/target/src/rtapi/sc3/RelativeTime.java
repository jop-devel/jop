package sc3;

public class RelativeTime {

	int usTime;
	
	public RelativeTime(long millis, int nanos) {
		
		usTime = ((int) millis)*1000 + nanos/1000;
	}
}
