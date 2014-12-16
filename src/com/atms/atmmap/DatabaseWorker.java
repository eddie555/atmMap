package com.atms.atmmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class DatabaseWorker extends SQLiteOpenHelper {
	Context mContext;
	SQLiteDatabase db;

	public DatabaseWorker(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		this.mContext = context;
		if (!checkDataBase()) {
		   updateDB();

		} else {

			System.out.println("NOT DOWNLOADING DATABASE");
		}
		
		checkForUpdate();
	}

	private void updateDB(){
		System.out.println("DOWNLOADING DATABASE");
		dc = new DownloaderClass();
		dc.downloadDatabase(mContext);
		copyServerDatabase();
	}
	private DownloaderClass dc;
	private static final int DATABASE_VERSION = 2;

	private static final String DB_DIRECTORY = "/data/data/com.atms.atmmap/databases/";
	private static final String DB_NAME = "atms.s3db";

	private static final String DB_FULL_PATH = DB_DIRECTORY + DB_NAME;
	private static final String DATABASE_NAME = "atms";

	@Override
	public void onCreate(SQLiteDatabase db) {
		// db.execSQL(DICTIONARY_TABLE_CREATE);

	}

	/**
	 * Check if the database exist
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		db = null;
		try {
			db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
					SQLiteDatabase.OPEN_READONLY);
			db.close();
		} catch (SQLiteException e) {
			System.out.println("NO DATABASE");
		}
		return db != null ? true : false;
	}

	public void checkForUpdate() {
		String myUri = "http://imaga.me/atms/version";
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(myUri);

		HttpResponse response;
		String liveVersion = null;
		try {
			response = httpClient.execute(get);
			// Build up result
			liveVersion = EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String localVersion = null;
		if (liveVersion != null) {
			
			try{
			db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
					SQLiteDatabase.OPEN_READONLY);
			String selectQuery = "SELECT version FROM version WHERE id = 0";
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					localVersion = cursor.getString(0);
				} while (cursor.moveToNext());
			}
			cursor.close();
			
			db.close();
			}
			catch(Exception e){
				
			}
			if(localVersion == null){
				updateDB();
			}else
				if(Integer.parseInt(localVersion) < Integer.parseInt(liveVersion)){
				updateDB();
				}
			
		}

	}
	


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * Copies your database from your local downloaded database that is copied
	 * from the server into the just created empty database in the system
	 * folder, from where it can be accessed and handled. This is done by
	 * transfering bytestream.
	 * */
	private void copyServerDatabase() {
		// by calling this line an empty database will be created into the
		// default system path
		// of this app - we will then overwrite this with the database from the
		// server
		SQLiteDatabase db = getReadableDatabase();
		db.close();

		OutputStream os = null;
		InputStream is = null;
		try {
			// Log.d(TAG, "Copying DB from server version into app");
			is = mContext.openFileInput(DB_NAME);
			os = new FileOutputStream(DB_DIRECTORY+DB_NAME); // XXX change this

			copyFile(os, is);
		} catch (Exception e) {
			Log.e("DBDOWNLOADERROR",
					"Server Database was not found - did it download correctly?",
					e);
		} finally {
			try {
				// Close the streams
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Log.e("DBDOWNLOADERROR", "failed to close databases");
			}
		}
		Log.d("GOOD", "Done Copying DB from server");
	}

	public boolean fileExists(Context context, String filename) {
		File file = context.getFileStreamPath(filename);
		if (file == null || !file.exists()) {
			return false;
		}
		return true;
	}

	public ArrayList<ATM> getClosestAtms(LatLng ll) {
		ArrayList<ATM> retList = new ArrayList<ATM>();
		//db.close();
		db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
				SQLiteDatabase.OPEN_READONLY);
		String limit = "45";
		String selectQuery = "SELECT address,lng,lat,owner FROM atms_accurate ORDER BY ABS("
				+ ll.latitude
				+ "3 - lat) + ABS("
				+ ll.longitude
				+ " - lng) ASC LIMIT " + limit;
		// String selectQuery =
		// "SELECT *, ( 3959 * acos( cos( radians("+ll.latitude+") ) * cos( radians( lat ) ) * cos( radians( lng ) - radians("+ll.longitude+") ) + sin( radians("+ll.latitude+") ) * sin( radians( lat ) ) ) ) AS distance FROM atms_accurate HAVING distance < 50 ORDER BY distance LIMIT 0 , "+limit;
		// String selectQuery = "SELECT  * FROM atms_accurate";
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				ATM atm = new ATM(cursor.getString(0), cursor.getFloat(1),
						cursor.getFloat(2), cursor.getString(3));
				retList.add(atm);
			} while (cursor.moveToNext());
		}
		cursor.close();

		db.close();
		return retList;

	}

	public ArrayList<Town> getTowns(CharSequence charSequence) {
		ArrayList<Town> retList = new ArrayList<Town>();
		//db.close();
		db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
				SQLiteDatabase.OPEN_READONLY);
		String limit = "30";
		String selectQuery = "SELECT name,lng,lat,city FROM towns_accurate where name like '"
				+ charSequence + "%' ORDER BY city DESC";
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Town town = new Town(cursor.getString(0), cursor.getFloat(1),
						cursor.getFloat(2), cursor.getInt(3));
				retList.add(town);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		return retList;

	}

	public String getNearestTown(LatLng ll) {
		ArrayList<Town> retList = new ArrayList<Town>();
		//db.close();
		db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
				SQLiteDatabase.OPEN_READONLY);
		String limit = "1";
		String selectQuery = "SELECT name,lng,lat,city FROM towns_accurate ORDER BY ABS("
				+ ll.latitude
				+ "3 - lat) + ABS("
				+ ll.longitude
				+ " - lng) ASC LIMIT " + limit;

		// String selectQuery =
		// "SELECT *, ( 3959 * acos( cos( radians("+ll.latitude+") ) * cos( radians( lat ) ) * cos( radians( lng ) - radians("+ll.longitude+") ) + sin( radians("+ll.latitude+") ) * sin( radians( lat ) ) ) ) AS distance FROM towns_accurate HAVING distance < 50 ORDER BY distance LIMIT 0 , "+limit;
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Town town = new Town(cursor.getString(0), cursor.getFloat(1),
						cursor.getFloat(2), cursor.getInt(3));
				retList.add(town);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return retList.get(0).name;

	}

	private void copyFile(OutputStream os, InputStream is) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
		os.flush();
	}
}