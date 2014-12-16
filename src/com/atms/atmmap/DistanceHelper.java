package com.atms.atmmap;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

public class DistanceHelper {

	public DistanceHelper(){
	}
	
	

	
	public double getDistance(LatLng a, LatLng b){
		return Math.sqrt(Math.pow((a.latitude-b.latitude),2) + Math.pow((b.longitude-a.longitude),2));
	}
	
	
	
	
}
