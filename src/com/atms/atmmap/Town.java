package com.atms.atmmap;

import com.google.android.gms.maps.model.LatLng;

public class Town {

	    LatLng latlng;
		public String name;
		public double lng,lat;
		int city;
		public Town(String name, double lng, double lat, int city){
			this.lng=lng;
			this.lat=lat;
			this.name=name;
			this.city=city;
			this.latlng = new LatLng(lat,lng);
		}
}
