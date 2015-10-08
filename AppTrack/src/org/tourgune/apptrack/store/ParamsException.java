package org.tourgune.apptrack.store;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 */
public class ParamsException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param params Cadena con los parametros que hacen saltar la excepcion
	 */
	public ParamsException(String params){
		super(params);
	}
	
}
