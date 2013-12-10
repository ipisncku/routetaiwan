package tw.ipis.routetaiwan;

public class TrainTimeTable {
	String type;
	String id;
	String depart_time;
	String arr_time;
	String duration;
	
	public TrainTimeTable(String traintype, String trationid, String departtime, String arrvetime, String dur) {
		type = traintype;
		id = trationid;
		depart_time = departtime;
		arr_time = arrvetime;
		duration = convert_time2str(dur);
	}
	
	/* duration 0 h 36m -> 0:36 */
	private String convert_time2str(String raw) {
		String out = raw.replaceAll("分", "").replace("小時", "&");
		String time[] = out.split("&");
		if(time.length == 2)
			return String.format("%d:%02d", Integer.parseInt(time[0]), Integer.parseInt(time[1]));
		else
			return "-";
	}
}