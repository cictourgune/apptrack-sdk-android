package org.tourgune.apptrack.store;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.tourgune.apptrack.api.AppTrackAPI;
import org.tourgune.apptrack.bean.Value; 
import org.tourgune.apptrack.utils.Utils;

import android.content.Context;
import android.util.Log;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 */
public class Store {
	
	public  ArrayList<Value> paramValues = new ArrayList<Value>();

	/**
	 * Método para almacenar una variable de tipo entero
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public  int addIntParam(int idParam, int value){
		
		Value param = new Value(idParam, String.valueOf(value));
		boolean guardado = paramValues.add(param);
		int result = 0;
		if (guardado == true)
			result = 1;
		else 
			result = -1;
		return result; 
	}
	
	/**
	 * Método para almacenar una variable de tipo decimal
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public  int addFloatParam(int idParam, float value){
		
		Value param = new Value(idParam, String.valueOf(value));
		boolean guardado = paramValues.add(param);
		int result;
		if (guardado == true)
			result = 1;
		else 
			result = -1;
		return result;
		
	}
	
	/**
	 * Método para almacenar una variable de tipo opción
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param value Valor a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public  int addOptionParam(int idParam, String value){
		
		Value param = new Value(idParam, String.valueOf(value));
		boolean guardado = paramValues.add(param);
		int result;
		if (guardado == true)
			result = 1;
		else 
			result = -1;
		return result;
	}
	
	/**
	 * Método para almacenar una variable de tipo fecha
	 * 
	 * @param idVariable Identificador de la variable que se puede consultar en la herramienta web
	 * @param year El año de la fecha a almacenar
	 * @param month El mes de la fecha a almacenar
	 * @param day El día de la fecha a almacenar
	 * @return 1 si se almacena correctamente y -1 si no se almacena correctamente
	 */
	public  int addDateParam(int idParam, String value){
		
		Value param = new Value(idParam, String.valueOf(value));
		boolean guardado = paramValues.add(param);
		int result;
		if (guardado == true)
			result = 1;
		else 
			result = -1;
		return result;
	}
	
	/**
	 * Método para enviar a la plataforma web todas las variables almacenadas. Para ello, comprueba si hay conexión a internet. 
	 * Si existe manda todas las variables al servidor. 
	 * En caso contrario, almacena en una base de datos local las variables para enviarlas posteriormente cuando se tenga conectividad  
	 * 
	 * @return 1 si los datos son correctos si no una cadena en la que se especificarán cuáles son las variables insertadas y cuáles son las no válidas
	 * @throws ParamsException Se lanzará cuando existan identificadores de variables inexistentes o valores fuera de los rangos permitidos para alguna de las variables
	 */
	public  String pushParamValues(Context contexto) throws ParamsException{
	
		if(!(Utils.isOnline(contexto))){
			
			Iterator<Value> iter = paramValues.iterator();
			while(iter.hasNext()) {
				Value valor = iter.next();
				Utils.insertParamLocal(contexto, valor); 
			}
			paramValues.clear(); 
			return "1";
		}
		else {
	
			String urls = AppTrackAPI.url + "/open/sdk/value/add" + AppTrackAPI.queryParams;
			String json = "{ \"valores\": [ ";
		
			Iterator<Value> itr = paramValues.iterator();
			while(itr.hasNext()) {
				Value value = itr.next();
				json = json + "{ \"idvariable\": \"" + value.getIdParam() + "\", \"valorvariable\": \"" + value.getValor() + "\" } ,";
			}
		
			json = json.substring(0, json.length()-1);
			json = json + "] }";
			Log.e("json pushParamValues", json);
		
			String result = Utils.callPOSTService(json, urls);
		
			Log.e("result pushParamValues", result);
		
			if (result.equalsIgnoreCase("1")){
				paramValues.clear();
			}
			
			else {
				String insertados = "";
				String novalidos = "";
				int indice;		
				String cadena = result;
				Log.e("cadena prueba", cadena);
				indice = cadena.indexOf(";");
				if (indice == 12)
					insertados = "";
				else
					insertados = cadena.substring(12, indice);
				novalidos = cadena.substring(indice + 13);
			
				if (insertados != ""){
				indice = insertados.indexOf(",");
				while (indice != -1){
					int x  = Integer.parseInt(insertados.substring(0, indice));
					deleteParam(x);
					insertados = insertados.substring(indice + 2);
					indice = insertados.indexOf(",");
				}
				int x  = Integer.parseInt(insertados);
				deleteParam(x);
			}
		
			if (novalidos != ""){
				String novalidos2 = novalidos;
				indice = novalidos2.indexOf(",");
				while (indice != -1){
					int x  = Integer.parseInt(novalidos2.substring(0, indice));
					deleteParam(x);
					novalidos2 = novalidos2.substring(indice + 2);
					indice = novalidos2.indexOf(",");
				}
				int x  = Integer.parseInt(novalidos2);
				deleteParam(x);
				throw new ParamsException (novalidos);
			}
			}

			return result;
		}
	}
	
	/**
	 * Metodo para eliminar variables del array de variables
	 * 
	 * @param param El identificador de la variable a eliminar
	 * @return 1 si se elimina correctamente y -1 si no se puede eliminar
	 */
	public  int deleteParam(int param){
		int result = 0;
		for (int i = 0; i < paramValues.size(); i++){
			if (param == paramValues.get(i).getIdParam()){
				paramValues.remove(i);
				result = 1;
			}
			else 
				result = -1;
		}
		return result;
	}
	
}
