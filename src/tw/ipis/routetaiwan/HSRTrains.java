package tw.ipis.routetaiwan;


public class HSRTrains {
	boolean southbound;
	int id;
	int Taipei;	// minutes since 00:00
	int Banqiao;
	int Taoyuang;
	int Hsinchu;
	int Taichung;
	int Chayi;
	int Tainan;
	int Zuoying;
	
	public HSRTrains(boolean south, int carid, int t1, int t2, int t3, int t4, int t5, int t6, int t7, int t8) {
		southbound = south;
		id = carid;
		Taipei = t1;
		Banqiao = t2;
		Hsinchu = t3;
		Taoyuang = t4;
		Taichung = t5;
		Chayi = t6;
		Tainan = t7;
		Zuoying = t8;
	}
	
	public int get_time_by_station(String station) {
		if(station.contains("台北") || station.contains("Taipei"))
			return Taipei;
		else if(station.contains("板橋") || station.contains("Banciao"))
			return Banqiao;
		else if(station.contains("桃園") || station.contains("Taoyuan"))
			return Taoyuang;
		else if(station.contains("新竹") || station.contains("Hsinchu"))
			return Hsinchu;
		else if(station.contains("台中") || station.contains("Taichung"))
			return Taichung;
		else if(station.contains("嘉義") || station.contains("Chiayi"))
			return Chayi;
		else if(station.contains("台南") || station.contains("Tainan"))
			return Tainan;
		else if(station.contains("左營") || station.contains("Zuoying"))
			return Zuoying;
		else 
			return -1;
	}
	
	public String minutes2str(int min) {
		if(min < 0)
			return "-";
		else 
			return String.format("%02d:%02d", min / 60, min % 60);
	}
	
	public String minutes2hour(int min) {
		if(min < 0)
			return "-";
		else 
			return String.format("%d:%02d", min / 60, min % 60);
	}
}