package org.tourgune.apptrack.main;

import org.tourgune.apptrack.R;
import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.store.ParamsException; 

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
	        AppTrackAPI apptrack = new AppTrackAPI(getApplicationContext(),"0848a92b-c320-4284-a705-c06a3547efb4","asf"); //TODO Singleton
	 
	        apptrack.addIntParam(319, 90); 
	        apptrack.addDateParam(303, 2013, 01, 10); 
			apptrack.pushParamValues();
			
			apptrack.startTracking(getApplicationContext());
		} catch (ParamsException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (NameNotFoundException e) { 
			e.printStackTrace();
		}
        
    
         
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
        
    }
    
   

    
}
