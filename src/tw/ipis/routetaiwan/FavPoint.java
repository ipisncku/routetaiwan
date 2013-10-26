package tw.ipis.routetaiwan;

import java.io.File;

import com.google.android.gms.maps.model.LatLng;


public class FavPoint {
	String name;
	String phonenum;
	LatLng location;
	String description;
	File file;
	
	public FavPoint(String n, LatLng l) {
		name = n;
		phonenum = null;
		location = l;
		description = null;
	}
	
	public FavPoint(String n, String p, LatLng l) {
		name = n;
		phonenum = p;
		location = l;
		description = null;
	}
	
	public FavPoint(String n, String p, LatLng l, String d) {
		name = n;
		phonenum = p;
		location = l;
		description = d;
	}
	
	public void set_description(String d) {
		description = d;
	}
	
	public void set_filename(File f) {
		file = f;
	}
}