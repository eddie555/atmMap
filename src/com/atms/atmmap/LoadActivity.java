package com.atms.atmmap;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class LoadActivity extends Activity {

	 private static int SPLASH_TIME_OUT = 2000;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);
		System.out.println("created");
		
		
		new Handler().postDelayed(new Runnable() {
			 
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

        		System.out.println("SPLASHED");
        		Intent intent = new Intent(getBaseContext(), MainActivity.class);
        		startActivity(intent);
 
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
		
	}
	
	protected void onResume(Bundle savedInstanceState) {
		

		System.out.println("resuming");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
				
		
		getMenuInflater().inflate(R.menu.load, menu);
		
		return true;
	}
	

}
