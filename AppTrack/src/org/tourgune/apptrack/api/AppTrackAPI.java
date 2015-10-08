package org.tourgune.apptrack.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import org.tourgune.apptrack.store.ParamsException;
import org.tourgune.apptrack.store.Store;
import org.tourgune.apptrack.utils.GeoServiceValues;
import org.tourgune.apptrack.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 */
public class AppTrackAPI {
	//identificadores de usuario, apliaccion y desarrollador
	public String dToken;
	public String aToken;
	public Context contexto;
	public  Store store;
	public static String queryParams;
	public static int battery = 15; 
	public static String url; 
	public static String fechaFin;
	
	public static String batteryService;
	public static String geoService;
	
	private static String uniqueID = null;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	private static boolean DEBUG_MODE = false;
	public static boolean sdDisponible = false;
	public static boolean sdAccesoEscritura = false;
	public static String DEBUG_FILE;
	
	
	/**
	 * CONSTRUCTOR
	 * 
	 * @param context Contexto de la aplicaci�n
	 * @param devToken Token de desarrollador
	 * @param appToken Token de aplicaci�n 
	 * @throws NameNotFoundException
	 * @throws ParamsException 
	 */
	public AppTrackAPI(Context context, String devToken, String appToken) throws NameNotFoundException, ParamsException{
		
		dToken = devToken;
		aToken = appToken;
		String uuid = id(context);
		queryParams = "?devToken=" + dToken + "&appToken=" + aToken + "&imei=" + uuid;  
		DEBUG_MODE = true;
		
		if (DEBUG_MODE == true){
			//Comprobamos el estado de la memoria externa (tarjeta SD)
			String estado = Environment.getExternalStorageState();
			 
			if (estado.equals(Environment.MEDIA_MOUNTED))
			{
			    sdDisponible = true;
			    sdAccesoEscritura = true;
			}
			else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
			{
			    sdDisponible = true;
			    sdAccesoEscritura = false;
			}
			else
			{
			    sdDisponible = false;
			    sdAccesoEscritura = false;
			}
			
			if (sdDisponible && sdAccesoEscritura){
				try
				{
				    File ruta_sd = context.getExternalFilesDir(null);
				 
				    File f = new File(ruta_sd.getAbsolutePath(), "apptrack_log.txt");
				    DEBUG_FILE = f.toString();
				    OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
				 
				    fout.write("Log AppTrack 2.");
				    
				    fout.close();
				}
				catch (Exception ex)
				{
				    Log.e("Ficheros", "Error al escribir fichero a tarjeta SD: " + ex);
				}
			}
		}
		
		ApplicationInfo ai;
	 
		ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		Bundle bundle = ai.metaData;
		Integer port = bundle.getInt("apptrack.port");
		String domain = bundle.getString("apptrack.domain"); 

		batteryService = bundle.getString("apptrack.battery");
		geoService = bundle.getString("apptrack.geo");
		
		url= "http://"+domain+":"+port+"/apptrack"; 
		store = new Store();
		
		int idvar;
		try{
			idvar = Integer.parseInt(Utils.callGETService(url + "/open/sdk/variable/plataforma" + queryParams));
		}
		catch (Exception ex)
		{
			Log.e("Plataforma", "Error en la lectura del identificador de la variable plataforma: " + ex);
			idvar = -1;
		}
		if (idvar == -1){
			throw new ParamsException("Identificador de la variable plataforma");
		}
		else{
			addOptionParam(idvar, "android");
//			pushParamValues();
		}
		
		this.contexto = context;
 
	}
	
	/**
	 * Metodo que inicia el servicio de tracking y env�a la localizacion al servidor
	 * 
	 * @param context Contexto de la aplicaci�n
	 * @param batteryLevel Nivel de bater�a al que se detendra el servicio. Evita que la aplicaci�n consuma la bater�a completa del dispositivo
	 * @param mintime Tiempo m�nimo que tiene que pasar entre 2 capturas de posici�n
	 * @param mindist Distancia m�nima que tiene que haber entre 2 capturas de posici�n
	 * @param mode Elige el modo de captura de la posicion (0 = GPS y 1 = WIFI)
	 * @param year El a�o de la fecha a partir de la cual el servicio no se ejecutar� (Si el a�o, el mes y el d�a son 0, la aplicaci�n se ejecutar� siempre)
	 * @param month El mes de la fecha a partir de la cual el servicio no se ejecutar� (Si el a�o, el mes y el d�a son 0, la aplicaci�n se ejecutar� siempre)
	 * @param day El d�a de la fecha a partir de la cual el servicio no se ejecutar� (Si el a�o, el mes y el d�a son 0, la aplicaci�n se ejecutar� siempre)
	 * @throws ParamsException Se lanzar� cuando existan valores no permitidos en alguno de los parametros de entrada
	 */
	public void startTracking(Context context, int batteryLevel, int mintime, int mindist, int year, int month, int day) throws ParamsException{

		if (batteryLevel < 0 || batteryLevel > 100 || mintime < 0 || mindist < 0)
			throw new ParamsException("Start tracking params");
		
		String fecha = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
		if (year == 0 && month == 0 && day == 0){
			fechaFin = fecha;
		}
		else if (Utils.isFechaValida(fecha)){
			fechaFin = fecha;
		}
		else{
			throw new ParamsException("Start tracking date params");
		}
		if (mintime >= 120000 )
			GeoServiceValues.MIN_TIME = mintime;
		else if (mintime != 0)
			throw new ParamsException("Start tracking min time param");
		if (mindist != 0)
			GeoServiceValues.MIN_DIST = mindist;
		if (batteryLevel != 0)
			battery = batteryLevel;
		
		Utils.startService(context);
	}
	
	/**
	 * M�todo que inicia el servicio de tracking y env�a la localizaci�n al servidor con los valores por defecto
	 * 
	 * @param context Contexto de la aplici�n
	 */
	public void startTracking(Context context) { 
		fechaFin = "0-0-0";
		Utils.startService(context);
	}
	
	/**
	 * M�todo que detiene el servicio de tracking
	 * 
	 * @param context Contexto de la aplici�n
	 */
	public void stopTracking(Context context){
		Utils.stopService(context);
	}
	
	/**
	 * M�todo para almacenar una variable de tipo entero
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public int addIntParam(int idVariable, int value){	
		return store.addIntParam(idVariable, value);
	}
	
	/**
	 * M�todo para almacenar una variable de tipo decimal
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public int addFloatParam(int idVariable, float value){	
		return store.addFloatParam(idVariable, value);	
	}

	/**
	 * M�todo para almacenar una variable de tipo opci�n
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public int addOptionParam(int idVariable, String value){
		return store.addOptionParam(idVariable, value);	
	}
	
	/**
	 * M�todo para almacenar una variable de tipo fecha
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param year El a�o de la fecha a almacenar
	 * @param month El mes de la fecha a almacenar
	 * @param day El d�a de la fecha a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public int addDateParam(int idVariable, int year, int month, int day){
		
		String fecha = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
		if (Utils.isFechaValida(fecha)){
			int result = store.addDateParam(idVariable, fecha);		
			return result; 
		}
		else
			return -1;
	}
	
	/**
	 * M�todo para enviar a la plataforma web todas las variables almacenadas. Para ello, comprueba si hay conexi�n a internet. 
	 * Si existe manda todas las variables al servidor. 
	 * En caso contrario, almacena en una base de datos local las variables para enviarlas posteriormente cuando se tenga conectividad  
	 * 
	 * @return 1 si los datos son correctamente insertados, si no una cadena en la que se especificar�n cu�les son las variables insertadas y cu�les son las no v�lidas
	 * @throws ParamsException Se lanzar� cuando existan identificadores de variables inexistentes o valores fuera de los rangos permitidos para alguna de las variables
	 */
	public String pushParamValues() throws ParamsException{
		return store.pushParamValues(contexto);
	}
    
	
	public synchronized static String id(Context context) {
	    if (uniqueID == null) {
	        SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
	        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
	        if (uniqueID == null) {
	            uniqueID = UUID.randomUUID().toString();
	            Editor editor = sharedPrefs.edit();
	            editor.putString(PREF_UNIQUE_ID, uniqueID);
	            editor.commit();
	        }
	    }
	    return uniqueID;
	}
	
}
