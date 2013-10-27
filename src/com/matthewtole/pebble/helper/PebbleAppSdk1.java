package com.matthewtole.pebble.helper;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.UUID;

public class PebbleAppSdk1 extends PebbleApp {
	
	public final int SDK_VERSION = 1;
	
	protected PebbleAppSdk1(Hashtable<String, byte[]> zip_contents) throws PebbleAppInvalidException {
		if (! zip_contents.containsKey("pebble-app.bin")) {
			throw new PebbleAppInvalidException();
		}
		byte[] binFile = zip_contents.get("pebble-app.bin");
		this.name = new String(Arrays.copyOfRange(binFile, 24, 55));
		this.company = new String(Arrays.copyOfRange(binFile, 56, 87));
		String uuid_str = bytesToHex(Arrays.copyOfRange(binFile, 108, 124));
		uuid_str = uuid_str.toLowerCase(Locale.UK);
		uuid_str = uuid_str.substring(0, 8) + 
				"-" + uuid_str.substring(8, 12) + 
				"-" + uuid_str.substring(12, 16) + 
				"-" + uuid_str.substring(16, 20) + 
				"-" + uuid_str.substring(20, 32);
		this.uuid = UUID.fromString(uuid_str);
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

}
