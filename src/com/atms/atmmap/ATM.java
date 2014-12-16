package com.atms.atmmap;

import com.google.android.gms.maps.model.LatLng;

public class ATM {

		LatLng latlng;
	String address,owner;
	double lng,lat;
	public ATM(String address, double lng, double lat, String owner){
		this.lng=lng;
		this.lat=lat;
		this.owner=owner;
		this.address=address;
		this.latlng = new LatLng(lat,lng);
	}
}
