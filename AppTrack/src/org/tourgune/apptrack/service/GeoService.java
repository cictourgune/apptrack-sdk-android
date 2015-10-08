package org.tourgune.apptrack.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.store.ParamsException;
import org.tourgune.apptrack.utils.GeoServiceValues;
import org.tourgune.apptrack.utils.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 * Servicio de tracking que captura la geolocalizacion periodicamente
 */
public class GeoService extends Service implements LocationListener{

	private Location currentBestLocation;
	private LocationManager lm;

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public void onCreate() {
		super.onCreate();	
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GeoServiceValues.MIN_TIME, GeoServiceValues.MIN_DIST, this);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GeoServiceValues.MIN_TIME, GeoServiceValues.MIN_DIST, this);	
	}

	public void onDestroy() {
		stopSelf();
		super.onDestroy();	
		
		lm.removeUpdates(this);
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

	// Este evento salta cuando el GPS detecta un cambio de posicion
	@Override
	public void onLocationChanged(Location location) {
		float distancia = 0;
		
		if(currentBestLocation!=null){
			distancia = location.distanceTo(currentBestLocation);
		} 
		if(isBetterLocation(location, currentBestLocation)){
			currentBestLocation = location;
			if (Utils.isOnline(getApplicationContext())){
				sendPosToServer(location);
			}
			else{
				Utils.insertPuntoLocal(getApplicationContext(), location);	
			}
		}
	}

		
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {		
	}	
		
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > GeoServiceValues.MIN_TIME;
	    boolean isSignificantlyOlder = timeDelta < -GeoServiceValues.MIN_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	
	
	
	/**
	 * Método que envía la posición actual al servidor. Además, en caso de que haya puntos anteriores en la base de datos, también los envía
	 * 
	 * @param loc Localización actual del dispositivo
	 */
		public void sendPosToServer(Location loc){
			
			try {
				Utils.sendDatabaseParamValues(getApplicationContext());
			} catch (ParamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Utils.sendDatabasePuntos(getApplicationContext());
			
			String url = AppTrackAPI.url + "/open/sdk/point/add" + AppTrackAPI.queryParams;
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   	    
			Calendar cal = Calendar.getInstance();
			String actualDate = dateformat.format(cal.getTime());
						
			String json = "{\"latitud\": \"" + Double.toString(loc.getLatitude()) + "\", ";
			json = json + "\"longitud\": \"" + Double.toString(loc.getLongitude()) + "\", ";
//			json = json + "\"fecha\": \"" + actualDate.toString() + " \" }";
			
			json = json + "\"fecha\": \"" + actualDate.toString() + "\", ";
			json = json + "\"provider\": \"" + loc.getProvider() + " \" }";

			Log.e("json sendPosToServer", json);
			
			String result = Utils.callPOSTService(json, url);
			
			Log.e("result sendPosToServer", result);
			
			if (result==null || !result.equalsIgnoreCase("1")) {
				//INSERTAMOS EN LOCAL
				Utils.insertPuntoLocal(getApplicationContext(), loc);
			
			}
		}
		
}
