package com.atms.atmmap;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DownloaderClass {

	public DownloaderClass(){
		
	}
    static boolean downloadDatabase(Context context) {
        try {
               // Log.d(TAG, "downloading database");
                URL url = new URL("http://imaga.me/atms/atms.s3db");
                /* Open a connection to that URL. */
                URLConnection ucon = url.openConnection();
                /*
                 * Define InputStreams to read from the URLConnection.
                 */
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1).
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                        baf.append((byte) current);
                }

                /* Convert the Bytes read to a String. */
                FileOutputStream fos = null;
                // Select storage location
                fos = context.openFileOutput("atms.s3db", Context.MODE_PRIVATE); 

                fos.write(baf.toByteArray());
                fos.close();
               // Log.d(TAG, "downloaded");
        } catch (IOException e) {
                Log.e("DBDOWNLOADERROR", "downloadDatabase Error IO: " , e);
                return false;
        }  catch (NullPointerException e) {
                Log.e("DBDOWNLOADERROR", "downloadDatabase Error NULL: " , e);
                return false;
        } catch (Exception e){
                Log.e("DBDOWNLOADERROR", "downloadDatabase Error OTHER: " , e);
                return false;
        }
        
        return true;
}
    
    
   
}
