package org.tourgune.apptrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 */
public class DatabasePuntos extends SQLiteOpenHelper {

	public static final String DATABASE_NAME ="puntos.db";
	public static final int DABASE_VERSION=1;
	public static final String TABLE_NAMES ="puntos";
	public static final String CAMPO1 ="longitud";
	public static final String CAMPO2 ="latitud";
	public static final String CAMPO3 ="fecha";
	public static final String CAMPO4 ="provider";

	
	public static final String PROJECTION_ALL_FIELDS[] = new String[]{CAMPO1,CAMPO2, CAMPO3, CAMPO4};
	

    /** Constructor */
    public DatabasePuntos(Context context) {
		super(context, DATABASE_NAME, null, DABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + TABLE_NAMES + "  (" + CAMPO1 + " TEXT, " + CAMPO2 + " TEXT, " + CAMPO3 + " TEXT, " + CAMPO4 + " TEXT)");
		
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
 
    
}