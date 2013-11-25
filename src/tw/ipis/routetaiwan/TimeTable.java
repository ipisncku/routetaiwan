package tw.ipis.routetaiwan;

public class TimeTable {
	boolean sun, mon, tue, wed, thu, fri, sat;
	int time;	/* minutes since 00:00 */
	String depart_station;
	String carrier;
	boolean hasback;
	
	public TimeTable(int t, boolean bool, String depart_sta, String carr, boolean back) {
		sun = mon = tue = wed = thu = fri = sat = bool;
		time = t;
		depart_station = depart_sta;
		hasback = back;
		carrier = carr;
	}
	
	public void set_sun(boolean bool) {
		sun = bool;
	}
	
	public void set_mon(boolean bool) {
		mon = bool;
	}
	
	public void set_tue(boolean bool) {
		tue = bool;
	}
	
	public void set_wed(boolean bool) {
		wed = bool;
	}
	
	public void set_thu(boolean bool) {
		thu = bool;
	}
	
	public void set_fri(boolean bool) {
		fri = bool;
	}
	
	public void set_sat(boolean bool) {
		sat = bool;
	}
	
	public boolean check_weekday(int weekday) {
		switch(weekday) {
		case 0:
			return sun;
		case 1:
			return mon;
		case 2:
			return tue;
		case 3:
			return wed;
		case 4:
			return thu;
		case 5:
			return fri;
		case 6:
			return sat;
		default:
			return false;
		}
	}
	
	public String minutes2str() {
		if(time < 0)
			return "-";
		else 
			return String.format("%02d:%02d", time / 60, time % 60);
	}
}