package org.tourgune.apptrack.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.store.ParamsException;
import org.tourgune.apptrack.utils.Utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 */
public class BatteryService extends Service {


	private BroadcastReceiver timeTickReceiver;
	private boolean registered = false;

	public class LocalBinder extends Binder {
	    BatteryService getService() {
	        return BatteryService.this;
	    }
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onStart(Intent intent, int startId){
	   

	    IntentFilter filter = new IntentFilter();
	    
	    filter.addAction(Intent.ACTION_BATTERY_CHANGED);     
	    timeTickReceiver = new TimeTickReceiver();

	    this.getApplicationContext().registerReceiver(timeTickReceiver, filter);
	    registered = true;
	}


	@Override
	public void onDestroy(){
	    
	if(registered){

	        this.getApplicationContext().unregisterReceiver(timeTickReceiver);
	        
	    }

	super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
	    // TODO Auto-generated method stub
	    return mBinder;
	}

	public class TimeTickReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {

			if (intent == null)
				return;
			if (context == null)
				return;
			
			String action = intent.getAction();
			
			if (action == null)
				return;
			
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				Log.e("nivel bateria", Integer.toString(level));
				if (level < AppTrackAPI.battery){

					try {
						Utils.sendDatabaseParamValues(context);
					} 
					catch (ParamsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Utils.sendDatabasePuntos(context);
					Utils.stopService(context);
					
				}	

				if (!AppTrackAPI.fechaFin.equalsIgnoreCase("0-0-0")){
					SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
					Calendar cal = Calendar.getInstance();
					String actualDate = dateformat.format(cal.getTime());
					 
					Log.e("actualDate", actualDate);
					Log.e("fechaFin", AppTrackAPI.fechaFin);
					
					// El método parse devuelve null si no se ha podido parsear el string en  según el formato indicado. Este método lanza una excepción NullPointer exception si alguno de sus parámetros es null 
					Date fecha1 = new Date();
					Date fecha2 = new Date();
					
					try {
						fecha1 = dateformat.parse(AppTrackAPI.fechaFin);
						fecha2 = dateformat.parse(actualDate);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	  
					 
					Log.e("comparacion", Integer.toString(fecha1.compareTo(fecha2)));
					if (fecha1.compareTo(fecha2) < 1){
						Log.e("fecha", "detener");
						try{
							Utils.stopService(getApplicationContext());
						} catch (Exception e){
							Log.e("exception", "exception: " + e.toString());
						}
						
					}
				}	
	        }
	    }
	}
}
