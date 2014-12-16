package com.atms.atmmap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MarkerPopUpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_marker_pop_up);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.marker_pop_up, menu);
		return true;
	}

}
