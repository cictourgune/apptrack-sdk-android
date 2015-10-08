package org.tourgune.apptrack.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.tourgune.apptrack.DatabaseParams;
import org.tourgune.apptrack.DatabasePuntos;
import org.tourgune.apptrack.ParamsContentProvider;
import org.tourgune.apptrack.PuntosContentProvider;
import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.bean.Point;
import org.tourgune.apptrack.bean.PointList;
import org.tourgune.apptrack.bean.Value;
import org.tourgune.apptrack.service.BatteryService;
import org.tourgune.apptrack.service.GeoService;
import org.tourgune.apptrack.store.ParamsException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 * Clase para almacenar los metodos utiles para la correcta ejecucion de la aplicacion
 */
public class Utils {

	/**
	 * Metodo que se ejecuta al iniciarse el tracking para almacenar el modo de captura de la posicion del dispositivo (GPS / WiFi) 
	 * 
	 * @param context El contexto de la aplicacion
	 */
	public static void startService(Context context) {
		//Iniciamos el servicio de tracking
		final Intent intent = new Intent(context, org.tourgune.apptrack.service.GeoService.class);
		context.startService(intent);	
		
		//Iniciamos el servicio de bateria
		final Intent intentBattery = new Intent(context, org.tourgune.apptrack.service.BatteryService.class);
		context.startService(intentBattery);
		
	}
		
	/**
	 * Metodo que detiene los servicios de tracking y de bateria
	 * 
	 * @param context El contexto de la aplicacion
	 */
	
//	stopService(new Intent(this, BatteryService.class));
//    stopService(new Intent(this, GeoService.class));
    
	public static void stopService(Context context){
		// Paramos el servicio de posicionamiento
		Intent intento = new Intent(context, GeoService.class);
		context.stopService(intento);
		boolean prueba = context.stopService(intento);

		// Paramos el servicio de batería
		Intent intento2 = new Intent(context, BatteryService.class);
		boolean prueba2 = context.stopService(intento2);
	
	}
	
	
	public static String getDeviceID(TelephonyManager phonyManager){
		 
		String id = phonyManager.getDeviceId();
		if (id == null){
			id = "not available";
		}
		int phoneType = phonyManager.getPhoneType();
		switch(phoneType){
		case TelephonyManager.PHONE_TYPE_NONE:
			return id;
		 
		case TelephonyManager.PHONE_TYPE_GSM:
			return id;
		 
		case TelephonyManager.PHONE_TYPE_CDMA:
			return id; 
		default:
			return "UNKNOWN: ID=" + id;
		}
		 
	}
	
	/**
	 * Metodo que hace una llamada asincrona de tipo POST
	 * 
	 * @param json 
	 * @param url
	 * @return
	 */
	public static String callPOSTService(String json, String url) {

		PostService async = new PostService(json, url);
		AsyncTask<Void, Void, String> result = async.execute();
		try {
			return result.get();

		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}

	}
	
	/**
	 * Metodo que hace una llamada de tipo GET
	 * 
	 * @param url
	 * @return
	 */
	public static String callGETService(String url) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		String result = null;
		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			result = httpclient.execute(request, handler);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		httpclient.getConnectionManager().shutdown();

		return result;
	}
	
	/**
	 * Metodo que almacena localmente un punto
	 * 
	 * @param context Contexto de la aplicacion
	 * @param loc Localizacion del dispositivo (latitud, longitud)
	 */
	public static void insertPuntoLocal(Context context, Location loc) {

		Cursor cursor = context.getContentResolver().query(PuntosContentProvider.CONTENT_URI, DatabasePuntos.PROJECTION_ALL_FIELDS, null, null, null);
		if(cursor.getCount() > 3600){			
			deleteDatabase(context);

		}
		
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String actualDate = dateformat.format(cal.getTime());

		ContentValues contentValues = new ContentValues();
		contentValues.put("longitud", Double.toString(loc.getLongitude()));
		contentValues.put("latitud", Double.toString(loc.getLatitude()));
		contentValues.put("fecha", actualDate);
		contentValues.put("provider", loc.getProvider());

		context.getContentResolver().insert(PuntosContentProvider.CONTENT_URI, contentValues);
	
	}
	
	/**
	 * Metodo que almacena localmente un parametro
	 * 
	 * @param context Contexto de la aplicacion
	 * @param val Objeto de tipo Value que contiene el identificador de la variable y el valor de esta
	 */
	public static void insertParamLocal(Context context, Value val) {

		ContentValues contentValues = new ContentValues();
		contentValues.put("idParam", val.getIdParam());
		contentValues.put("valor", val.getValor());


		context.getContentResolver().insert(ParamsContentProvider.CONTENT_URI, contentValues);
	
	}
	
	/**
	 * Metodo que vuelca todos los puntos almacenados localmente a la base de datos del servidor
	 * 
	 * @param context Contexto de la aplicacion
	 */
	public static void sendDatabasePuntos(Context context) {

		String url = AppTrackAPI.url + "/open/sdk/point/addmulti" + AppTrackAPI.queryParams;
		PointList pointList = new PointList();
		
		
		String json = "{ \"puntos\": [ ";
		
		Cursor cursor = context.getContentResolver().query(
				PuntosContentProvider.CONTENT_URI,
				DatabasePuntos.PROJECTION_ALL_FIELDS, null, null, null);
		if(cursor!=null && cursor.getCount()>0){
		if (cursor.moveToFirst() && cursor.getCount() > 1) {
			do {
				
				json = json + "{\"latitud\": \"" + cursor.getString(1) + "\", ";
				json = json + "\"longitud\": \"" + cursor.getString(0) + "\", ";
//				json = json + "\"fecha\": \"" + cursor.getString(2) + "\" },";
				
				json = json + "\"fecha\": \"" + cursor.getString(2) + "\", ";
				json = json + "\"provider\": \"" + cursor.getString(3) + " \" },";
				
				Point point = new Point();
				point.setLatitude(Double.parseDouble(cursor.getString(1)));
				point.setLongitude(Double.parseDouble(cursor.getString(0)));
				point.setDate(cursor.getString(2));
				point.setMode(cursor.getString(3));
				
				pointList.addPoints(point);
				
			} while (cursor.moveToNext());
			
			json = json.substring(0, json.length()-1);
			json = json + "] }";
			
			Log.e("json sendDatabasePuntos", json);

			
			String result = Utils.callPOSTService(json, url);
			Log.e("result sendDatabasePuntos", result);
			int numero;
			if (!result.equalsIgnoreCase("")){
				numero = Integer.parseInt(result);
			}
			else{
				numero = -1;
			}
			if (numero != -1) {

				Iterator<Point> itr = pointList.getPoints().iterator();
	    		while(numero > 0) {	
	    			
	    			Point punto = itr.next();
	    			context.getContentResolver().delete(PuntosContentProvider.CONTENT_URI, "fecha='" + punto.getDate() + "'", null);
	    			numero--;
	    		}	
			}
			
			
		cursor.close();
		}
		}
	}
	
	/**
	 * Metodo que vuelca todos los parametros almacenados localmente a la base de datos del servidor
	 * 
	 * @param context Contexto de la aplicacion
	 */
	public static void sendDatabaseParamValues(Context context) throws ParamsException{

		String url = AppTrackAPI.url + "/open/sdk/value/add" + AppTrackAPI.queryParams;

		String json = "{ \"valores\": [ ";
		
		Cursor cursor = context.getContentResolver().query(
				ParamsContentProvider.CONTENT_URI,
				DatabaseParams.PROJECTION_ALL_FIELDS, null, null, null);
		if(cursor!=null && cursor.getCount()>0){
			if (cursor.moveToFirst() && cursor.getCount() > 1) {
			do {	
				json = json + "{ \"idvariable\": \"" + cursor.getString(0) + "\", \"valorvariable\": \"" + cursor.getString(1) + "\" } ,";

			} while (cursor.moveToNext());
			
			json = json.substring(0, json.length()-1);
			json = json + "] }";
			
			Log.e("json sendDatabaseParamValues", json);

			String result = Utils.callPOSTService(json, url);
			
			Log.e("result sendDatabaseParamValues", "result:" + result);
			if (result.equalsIgnoreCase("")){
				Log.e("conexion", "no hay conexion");
			}
			else if (result.equalsIgnoreCase("1")){
				context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "", null);
				}
			else {
				String insertados = "";
				String novalidos = "";
				int indice;
				
				String cadena = result;
				
				indice = cadena.indexOf(";");
				insertados = cadena.substring(12, indice);
				novalidos = cadena.substring(indice + 13);
				
				if (insertados != ""){
					indice = insertados.indexOf(",");
					while (indice != -1){
						int x  = Integer.parseInt(insertados.substring(0, indice));
						context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "idVariable='" + x + "'", null);
						insertados = insertados.substring(indice + 1);
						indice = insertados.indexOf(",");
					}
					int x  = Integer.parseInt(insertados);
					context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "idVariable='" + x + "'", null);
				}
				
				if (novalidos != ""){
					indice = novalidos.indexOf(",");
					while (indice != -1){
						int x  = Integer.parseInt(insertados.substring(0, indice));
						context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "idVariable='" + x + "'", null);
						insertados = insertados.substring(indice + 1);
						indice = insertados.indexOf(",");
					}
				int x  = Integer.parseInt(insertados);
				context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "idVariable='" + x + "'", null);
				throw new ParamsException (novalidos);
				}

			}
			
			
		}
			cursor.close();
		}
		
	}
	
	/**
	 * Metodo que elimina todas las entradas de la base de datos
	 * 
	 * @param context Contexto de la aplicacion
	 */
	public static void deleteDatabase(Context context){
		context.getContentResolver().delete(ParamsContentProvider.CONTENT_URI, "", null);
	}
	

	/**
	 * Metodo que comprueba la validez de una fecha
	 * 
	 * @param fechax Fecha en formato de cadena de texto
	 * @return True si la vecha es valida. False en caso contrario
	 */
	public static boolean isFechaValida(String fechax) {
		  try {
		      SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		      formatoFecha.setLenient(false);
		      formatoFecha.parse(fechax);
		  } catch (Exception e) {
		      return false;
		  }
		  return true;
		}


	public static boolean isOnline(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static void writeLog (Context context, String log){
				
		try
		{ 
		    File f = new File(AppTrackAPI.DEBUG_FILE);
		 
		    BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		 
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = fin.readLine()) != null) {
		      sb.append(line).append("\n");
		      
		    }
		    sb.append(log).append("\n");
		    
		    OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
			
		    SimpleDateFormat dateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			String actualDate = dateformat.format(cal.getTime());
		    
		    fout.write(actualDate + " " + sb.toString());
		    fout.close();
		    fin.close();
		}
		catch (Exception ex)
		{
		    Log.e("Ficheros", "Error al leer fichero desde tarjeta SD: " + ex);
		}
		
	}
	
}
