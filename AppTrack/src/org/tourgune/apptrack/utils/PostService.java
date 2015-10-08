package org.tourgune.apptrack.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 * Metodo para llamar a los servicios POST de manera asincrona (necesario a partir de la version 4 de android)
 */
public class PostService extends AsyncTask<Void, Void, String>{
	private String json;
	private String url;
	
	public PostService(String jsonPar, String urlPar) {
		
		json = jsonPar;
		url = urlPar;
	
	}

	protected String doInBackground(Void... params) {
		StringEntity se = null;
		HttpPost postRequest = new HttpPost(url);
		BasicHttpResponse httpResponse = null;

		String respStr="";
		try {
			se = new StringEntity(json, HTTP.UTF_8);
			se.setContentType("application/json");
			postRequest.setEntity(se);
			HttpClient httpclient = new DefaultHttpClient();
			
			httpResponse = (BasicHttpResponse) httpclient.execute(postRequest);
			
			respStr = EntityUtils.toString(httpResponse.getEntity());
			
		} catch (Exception e) {
			Log.e("respuesta catch", e.toString());
			return respStr;
		}
		return respStr;
	}



}
