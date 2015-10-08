package org.tourgune.apptrack;

import java.lang.reflect.Field;

import org.tourgune.apptrack.api.AppTrackAPI;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * AppTrack
 *
 * Created by CICtourGUNE on 10/04/13.
 * Copyright (c) 2013 CICtourGUNE. All rights reserved.
 */
public class PuntosContentProvider extends ContentProvider {
	private static final String INVALID_URI_MESSAGE= "Invalid Uri ";
	
	
//	public static final String AUTHORITY_PART= 	"org.tourgune.apptrack.puntos";
	public static final String AUTHORITY_PART=initAuthority();
//	"org.tourgune.apptrack.params";


	private static String initAuthority() {
		String authority = null;
		
		try {
		
		    ClassLoader loader = ParamsContentProvider.class.getClassLoader();
		
		    Class<?> clz = loader.loadClass("org.tourgune.apptrack.ProviderAuthority");
		    Field declaredField = clz.getDeclaredField("CONTENT_AUTHORITY_POINTS");
		
		    authority = declaredField.get(null).toString();
		} catch (ClassNotFoundException e) {} 
		catch (NoSuchFieldException e) {} 
		catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		
		return authority;
	}
	
	
	public static final int CODE_ALL_ITEMS= 1;
	public static final int CODE_SINGLE_ITEM= 2;
	public static final String CONTENT_PREFIX= "content://";
	public static final Uri CONTENT_URI= Uri.parse(CONTENT_PREFIX + AUTHORITY_PART + "/puntos");
	
	public static final String MIME_TYPE_ALL_ITEMS="vnd.android.cursor.dir/vnd." + AUTHORITY_PART;
	public static final String MIME_TYPE_SINGLE_ITEMS="vnd.android.cursor.item/vnd." + AUTHORITY_PART;
	
	public static final UriMatcher URI_MATCHER;
	
	private SQLiteDatabase database;
	private DatabasePuntos dbHelper;
	
	
	
	
	
	static
	{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY_PART, "puntos", CODE_ALL_ITEMS);
		URI_MATCHER.addURI(AUTHORITY_PART, "puntos/#", CODE_SINGLE_ITEM);
	}
	
		
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		int rowsAffected =0;
		switch (URI_MATCHER.match(uri))
		{
		case CODE_ALL_ITEMS:
			rowsAffected = this.getOrOpenDatabase().delete(DatabasePuntos.TABLE_NAMES, where, whereArgs);
			break;
		case CODE_SINGLE_ITEM:
			String singleRecordId = uri.getPathSegments().get(1);
			rowsAffected = this.getOrOpenDatabase().delete(DatabasePuntos.TABLE_NAMES, 
														   DatabasePuntos.CAMPO1 + "=" + singleRecordId, 
														   whereArgs);
			break;
		default:
			throw new IllegalArgumentException(INVALID_URI_MESSAGE + uri);
		}
		return rowsAffected;
	}

	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (URI_MATCHER.match(uri))
		{
		case CODE_ALL_ITEMS:
			return MIME_TYPE_ALL_ITEMS;
		case CODE_SINGLE_ITEM:
			return MIME_TYPE_SINGLE_ITEMS;
		default:
			throw new IllegalArgumentException(INVALID_URI_MESSAGE + uri);
		}
	}

	
	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		long rowID = getOrOpenDatabase().insert(DatabasePuntos.TABLE_NAMES, DatabasePuntos.DATABASE_NAME, arg1);
		Uri newRecordUri = null;
		switch (URI_MATCHER.match(arg0))
		{
		case CODE_ALL_ITEMS:
			if (rowID > 0)
				newRecordUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			break;
		default:
			throw new IllegalArgumentException (INVALID_URI_MESSAGE + arg0);
			
		}
		return newRecordUri;
	}

	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		dbHelper = new DatabasePuntos(getContext());
		database = dbHelper.getWritableDatabase();
		return database != null && database.isOpen();
	}

	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sort) {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		
		qBuilder.setTables(DatabasePuntos.TABLE_NAMES);
		cursor = qBuilder.query(getOrOpenDatabase(), projection, selection, selectionArgs, null, null, null);
		
		return cursor;
	}

	
	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private  SQLiteDatabase getOrOpenDatabase()
	{
		SQLiteDatabase db = null;
		if (this.database!=null && database.isOpen())
			db= this.database;
		else
			db = dbHelper.getWritableDatabase();
		return db;
	}

}
