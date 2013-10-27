package com.matthewtole.pebble.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;

import android.content.Context;

public abstract class PebbleApp {
	
	public final int SDK_VERSION = 0;
	public static String TAG = PebbleApp.class.getSimpleName();
	
	public static class PebbleAppException extends Exception {
		private static final long serialVersionUID = -8718625773497433478L;
		public PebbleAppException(Exception e) {
			super(e);
		}
		public PebbleAppException(String string) {
			super(string);
		}
	}
		
	public static class PebbleAppInvalidException extends PebbleAppException {
		private static final long serialVersionUID = -8536744274838144732L;	
		public PebbleAppInvalidException() {
			super("The file is not valid Pebble app.");
		}
	};
		
	protected UUID uuid;
	protected String name;
	protected String company;
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCompany() {
		return this.company;
	}
	
	@Override
	public boolean equals(Object o) {
		PebbleApp app = (PebbleApp)o;
		return app.getUUID().equals(getUUID());
	}

	@Override
	public String toString() {
		return getName();
	}

	protected static ZipEntry getZipEntry(ZipInputStream zip, String filename) {
		ZipEntry zip_entry;
		try {
			// zip.reset();
			while ((zip_entry = zip.getNextEntry()) != null) {
			    if (zip_entry.getName().compareTo(filename) == 0) {
			        return zip_entry;
			    }
			    zip_entry.getName();
			}
		} catch (IOException e) {	
			// TODO: Handle this error better.
		}
		return null;
	}
		
	protected static byte[] readZipEntryContents(ZipInputStream zip) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int data;
		try {
			while ((data = zip.read()) != -1) output.write(data);
			output.close();
		} catch (IOException e) {
			// TODO: Handle this error better.
		}
	    return output.toByteArray();
	}
	
	protected static Hashtable<String, byte[]> getZipContents(ZipInputStream zip) {
		Hashtable<String, byte[]> contents = new Hashtable<String, byte[]>();
		
		ZipEntry zip_entry;
		try {
			while ((zip_entry = zip.getNextEntry()) != null) {
				contents.put(zip_entry.getName(), readZipEntryContents(zip));
			}
		}
		catch (IOException e) {	
		}		
		return contents;
	}
	
	public static PebbleApp load(Context context, String path) 
			throws FileNotFoundException, PebbleAppInvalidException {

		FileInputStream pbw_file;
        ZipInputStream pbw_zip;
        
        pbw_file = new FileInputStream(path);
        pbw_zip = new ZipInputStream(pbw_file);
        Hashtable<String, byte[]> zip_contents = getZipContents(pbw_zip);
        try {
			pbw_zip.close();
		} catch (IOException e) {
		}
        
        if (zip_contents.containsKey("appinfo.json")) {
    		try {
				return new PebbleAppSdk2(zip_contents);
			} catch (JSONException e) {
				throw new PebbleAppInvalidException();
			}
    	}
        else {
        	PebbleAppSdk1 app = new PebbleAppSdk1(zip_contents);
        	return app;
        }
	}
	
	public static PebbleApp loadUrl(Context context, String url) 
			throws PebbleAppInvalidException, PebbleAppException {

		File cacheDir = new File(context.getCacheDir(), "com.matthewtole.pebble.helper");
		cacheDir.mkdirs();
		File tmpPbw = null;
		try {
			tmpPbw = File.createTempFile("downloaded-", "pbw", cacheDir);
		} catch (IOException e) {
			throw new PebbleAppException(e);
		}
		if (tmpPbw == null) {
			throw new PebbleAppException("PBW file was not created for some reason");
		}

		URL url_obj = null;
		URLConnection url_conn = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		FileOutputStream fos = null;
		boolean success = false;
		
		try {
			url_obj = new URL(url);
			url_conn = url_obj.openConnection();
			is = url_conn.getInputStream();
			bis = new BufferedInputStream(is);
			baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			fos = new FileOutputStream(tmpPbw);
			fos.write(baf.toByteArray());
			success = true;
			
		} catch (MalformedURLException e) {
			throw new PebbleAppException(e);
		} catch (IOException e) {
			throw new PebbleAppException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new PebbleAppException(e);
				}
			}
		}
		
		if (! success) {
			throw new PebbleAppException("Downloading failed for some reason");
		}
		
		try {
			PebbleApp app = load(context, tmpPbw.getAbsolutePath()); 
			return app;
		} catch (FileNotFoundException e) {
			throw new PebbleAppException(e);
		}
	}
	
}
