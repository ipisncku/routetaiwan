package tw.ipis.routetaiwan;

public class TrainTable {
	String station;
	String coming_time;
	String depart_time;
	boolean istrain;
	
	public TrainTable(String sta, String cometime, String departtime, boolean train) {
		station = sta;
		coming_time = cometime;
		depart_time = departtime;
		istrain = train;
	}
}