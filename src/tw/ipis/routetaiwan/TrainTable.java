package tw.ipis.routetaiwan;

import android.widget.TableRow;

public class TrainTable {
	String station;
	String coming_time;
	String depart_time;
	boolean istrain;
	boolean leaved;
	int delaymins;
	boolean start;
	boolean destination;
	TableRow tr;
	
	public TrainTable(String sta, String cometime, String departtime, boolean train, boolean leave) {
		station = sta;
		coming_time = cometime;
		depart_time = departtime;
		istrain = train;
		leaved = leave;
		delaymins = -1;
		start = destination = false;
		tr = null;
	}
	
	public void set_delay(int mins) {
		delaymins = mins;
	}
	
	public void set_start() {
		start = true;
	}
	
	public void set_destination() {
		destination = true;
	}
	
	public void set_train() {
		istrain = true;
	}
	
	public void set_tablerow(TableRow t) {
		tr = t;
	}
	
	public String dumpitem() {
		return String.format("站名:\"%s\" 進站:\"%s\" 出站:\"%s\" 離站:%s 車:%s", station, coming_time, depart_time, leaved ? "是" : "否", istrain ? "是" : "否");
	}
}