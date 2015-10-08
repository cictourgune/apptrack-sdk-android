package org.tourgune.apptrack.phonegap.plugin;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.store.ParamsException;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 */
public class ApptrackPlugin extends CordovaPlugin { 
	
	private AppTrackAPI apptrack;
  

	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try{
        	
	        if (action.equals("init")) {
	 	            String devToken = args.getString(0); 
	 	            String appToken = args.getString(1); 
	 	            if(devToken==null || devToken=="" || appToken==null || appToken==""){
	 	            	return false;
	 	            }
	 	            try{
	 	               init(devToken, appToken, callbackContext);
	 	            }catch(NameNotFoundException e){
	 	            	e.printStackTrace();
	 	            	return false;
	 	            }
	 	        
	 	            return true;
	 	            
	 	     }else if(action.equals("startTracking")){
	 	    	 	Integer batteryLevel = args.getInt(0); 
	 	    	 	Integer mintime = args.getInt(1); 
	 	    	 	Integer mindist = args.getInt(2); 
	 	    	 	Integer year = args.getInt(3); 
	 	    	 	Integer month = args.getInt(4); 
	 	    	 	Integer day = args.getInt(5); 
	 	    	 	
	 	    	 	//Validar parametros
	 	    	 	if(batteryLevel==null || mintime==null || mindist==null) {
	 	    	 		return false;
	 	    	 	}
	 	    	 	if(batteryLevel>=100 || batteryLevel<0){ //por defecto poner 0 en parametros javascript
	 	    	 		return false;
	 	    	 	} 
	 	    		if(mintime<0 || mindist<0){   //mayores o igual a 0
	 	    	 		return false;
	 	    	 	} 
	 	    		if(year<0 || month<0 || day<0){
	 	    			return false;
	 	    		}
	 	    	 	if(startTracking(batteryLevel, mintime, mindist, year, month, day, callbackContext)!=1){
	 	    	 		return false; 
	 	    	 	}
	 	    	 	return true;
	 	    	 	
	 	     }else if (action.equals("stopTracking")) {
	 	    	 	stopTracking(callbackContext);
	 	            return true;
	 	     }else  if (action.equals("addIntParam")) {
	 	            Integer idVariable = args.getInt(0); 
	 	            Integer value = args.getInt(1);  
	 	            addIntParam(idVariable, value, callbackContext);
	 	            return true;
	 	     }else  if (action.equals("addFloatParam")) {
	 	    	 	Integer idVariable = args.getInt(0); 
	 	    	 	try{
	 	    	 		Float value = Float.parseFloat(args.getString(1));  
	 	  	            addFloatParam(idVariable, value, callbackContext); 
	 	    	 	}catch(NumberFormatException e){
	 	    	 		  return false;
	 	    	 	} 
	 	            return true;
	 	     }else  if (action.equals("addOptionParam")) {
	 	    	 	Integer idVariable = args.getInt(0);
	 	    	 	String value = args.getString(1);   
	 	  	        addOptionParam(idVariable, value, callbackContext); 
	 	            return true;
	 	     } else  if (action.equals("addDateParam")) {
	 	    	 	Integer idVariable = args.getInt(0);
	 	    	 	Integer year = args.getInt(1);
	 	    	 	Integer month = args.getInt(2);
	 	    	 	Integer day = args.getInt(3);  
	 	  	        addDateParam(idVariable, year, month, day, callbackContext);
	 	            return true;
	 	     } else  if (action.equals("pushParamValues")) { 
	 	    	 	pushParamValues(callbackContext);
	 	            return true;
	 	     }  
        }catch(Exception e){
        	return false;
        } 
        return false;
    }
	
	
	//-------------------- apptrack API wrappers
	
	/**
	 * CONSTRUCTOR
	 * 
	 * @param devToken Token del desarrollador
	 * @param appToken Token de la aplicacion
	 * @param callbackContext Contexto de la aplicacion
	 * @throws NameNotFoundException
	 * @throws ParamsException 
	 */
	private void init(String devToken, String appToken, CallbackContext callbackContext) throws NameNotFoundException, ParamsException{
		if(apptrack!=null){ //singleton
			  callbackContext.success("1"); 
		}else{
			  apptrack = new AppTrackAPI(getActivity(),devToken,appToken); 
		      callbackContext.success("1"); 
		}  
	}
	
	
	/**
	 * Metodo que inicia el servicio de tracking y envía la localizacion al servidor
	 * 
	 * @param batteryLevel Nivel de batería al que se detendra el servicio. Evita que la aplicación consuma la batería completa del dispositivo
	 * @param mintime Tiempo mínimo que tiene que pasar entre 2 capturas de posición
	 * @param mindist Distancia mínima que tiene que haber entre 2 capturas de posición
	 * @param year El año de la fecha a partir de la cual el servicio no se ejecutará (Si el año, el mes y el día son 0, la aplicación se ejecutará siempre)
	 * @param month El mes de la fecha a partir de la cual el servicio no se ejecutará (Si el año, el mes y el día son 0, la aplicación se ejecutará si empre)
	 * @param day El día de la fecha a partir de la cual el servicio no se ejecutará (Si el año, el mes y el día son 0, la aplicación se ejecutará siempre)
	 * @param callbackContext Contexto de la aplicacion
	 * @return 1 si el servicio se inicia correctamente, -1 en caso contrario
	 */
	public int startTracking(int batteryLevel, int mintime, int mindist, int year, int month, int day,  CallbackContext callbackContext) {
		try {
			apptrack.startTracking(getActivity(), batteryLevel, mintime, mindist, year, month, day);
		} catch (ParamsException e) { 
			callbackContext.error("ParamsException");  
			e.printStackTrace();
			return -1;
		}
		callbackContext.success("1"); 
		return 1;
	}
	
	/**
	 * Método que detiene el servicio de tracking
	 * 
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void stopTracking(CallbackContext callbackContext) {
		apptrack.stopTracking(getActivity());
		callbackContext.success("1"); 
	}
	
	/**
	 * Método para almacenar una variable de tipo entero
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void addIntParam(int idVariable, int value, CallbackContext callbackContext){
		Integer result = apptrack.addIntParam(idVariable, value);
		callbackContext.success(result.toString()); 
	}
	
	/**
	 * Método para almacenar una variable de tipo decimal
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void addFloatParam(int idVariable, float value, CallbackContext callbackContext){
		Integer result = apptrack.addFloatParam(idVariable, value);
		callbackContext.success(result.toString()); 
	}
	
	/**
	 * Método para almacenar una variable de tipo opción
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void addOptionParam(int idVariable, String value, CallbackContext callbackContext){
		Integer result = apptrack.addOptionParam(idVariable, value);
		callbackContext.success(result.toString()); 
	}
	
	/**
	* Método para almacenar una variable de tipo fecha
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param year El año de la fecha a almacenar
	 * @param month El mes de la fecha a almacenar
	 * @param day El día de la fecha a almacenar
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void addDateParam(int idVariable, int year, int month, int day, CallbackContext callbackContext){
		Integer result = apptrack.addDateParam(idVariable, year, month, day);
		callbackContext.success(result.toString()); 
	}
	
	/**
	 * Método para enviar a la plataforma web todas las variables almacenadas. Para ello, comprueba si hay conexión a internet. 
	 * Si existe manda todas las variables al servidor. 
	 * En caso contrario, almacena en una base de datos local las variables para enviarlas posteriormente cuando se tenga conectividad 
	 * 
	 * @param callbackContext Contexto de la aplicacion
	 */
	public void pushParamValues(CallbackContext callbackContext){
		String result="";;
		try {
			result = apptrack.pushParamValues();
		} catch (ParamsException e) {
			callbackContext.error(result.toString()); 
			e.printStackTrace();
		}
		callbackContext.success(result.toString()); 
	}
	

	//---------------------------- utils
	private Activity getActivity() {
        return this.cordova.getActivity();
    }
	
	

 

}
