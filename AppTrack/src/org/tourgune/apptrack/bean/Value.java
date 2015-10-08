package org.tourgune.apptrack.bean;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 * 
 * Bean de la tabla variables de la base de datos
 */
public class Value {

	private int idParam;
	private String valor; 
	
	public Value(int idParam, String valor) {
		super();
		this.idParam = idParam;
		this.valor = valor; 
	}

	public int getIdParam() {
		return idParam;
	}

	public void setIdParam(int idParam) {
		this.idParam = idParam;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

 
	
}
