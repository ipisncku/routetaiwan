package tw.ipis.routetaiwan;


public class HSRTrains {
	boolean southbound;
	String id;
	int Taipei;	// minutes since 00:00
	int Banqiao;
	int Taoyuang;
	int Taichung;
	int Chayi;
	int Tainan;
	int Kaohsiung;
	
	public HSRTrains(boolean south, String carid, int t1, int t2, int t3, int t4, int t5, int t6, int t7) {
		southbound = south;
		id = carid;
		Taipei = t1;
		Banqiao = t2;
		Taoyuang = t3;
		Taichung = t4;
		Chayi = t5;
		Tainan = t6;
		Kaohsiung = t7;
	}
}