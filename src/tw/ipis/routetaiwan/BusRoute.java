package tw.ipis.routetaiwan;


public class BusRoute {
	String StopName;
	int GoBack;
	int seqNo;
	String Value;
	String comeTime;
	String carId;
	boolean isStart, isDestination;
	
	public BusRoute(String sn, int gb, int seq, String v, String time, String id) {
		StopName = sn;
		GoBack = gb;
		seqNo = seq;
		Value = v;
		comeTime = time;
		carId = id;
		isStart = false;
		isDestination = false;
	}
	public void set_start() {
		isStart = true;
	}
	public void set_destination() {
		isDestination = true;
	}
}