package com.matthewtole.pebble.helper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

public class PebbleAppSdk2 extends PebbleApp {
	
	public final int SDK_VERSION = 2;
	
	protected class PebbleAppResource {
		
		protected String type;
		protected String name;
		protected String file;
		protected String characterRegex;

		public PebbleAppResource(JSONObject json) throws JSONException {
			this.type = json.getString("type");
			this.name = json.getString("name");
			this.file = json.getString("file");
			if (this.type == "font" && json.has("characterRegex")) {
				this.characterRegex = json.getString("characterRegex");
			}
		}
		
		public String getType() {
			return this.type;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getFile() {
			return this.file;
		}
		
	};
	
	protected enum AppType { WATCHFACE, APP }

	protected AppType type;
	protected Bitmap icon = null;
	protected ArrayList<PebbleAppResource> resources;

	protected String longName;
	protected int versionCode;
	protected String versionLabel;

	protected PebbleAppSdk2(Hashtable<String, byte[]> zip_contents) throws JSONException {
		
		JSONObject app_info = new JSONObject(new String(zip_contents.get("appinfo.json")));
		
		this.uuid = UUID.fromString(app_info.getString("uuid"));
		this.name = app_info.getString("shortName");
		this.company = app_info.getString("companyName");
		this.longName = app_info.getString("longName");
		this.versionCode = app_info.getInt("versionCode");
		this.versionLabel = app_info.getString("versionLabel");
		this.resources = new ArrayList<PebbleAppResource>();
		
		JSONObject watchapp = app_info.getJSONObject("watchapp");
		this.type = watchapp.getBoolean("watchface") ? AppType.WATCHFACE : AppType.APP;
		
		JSONObject resources = app_info.has("resources") ? app_info.getJSONObject("resources") : null;
		if (resources != null && resources.has("media")) {
			JSONArray media = resources.getJSONArray("media");
			for (int m = 0; m < media.length(); m += 1) {
				JSONObject mediaItem = media.getJSONObject(m);
				try {
					this.resources.add(new PebbleAppResource(mediaItem));
				} catch (JSONException e) {
				}
			}
		}
	}
	
	public String getLongName() {
		return this.longName;
	}
	
	public Bitmap getIcon() {
		return this.icon;
	}
	
	public PebbleAppResource[] getResources() {
		return (PebbleAppResource[]) this.resources.toArray();
	}
	
	public PebbleAppResource[] getResourcesOfType(String type) {
		ArrayList<PebbleAppResource> typeResources = new ArrayList<PebbleAppResource>();
		for (PebbleAppResource app : this.resources) {
			if (app.type == type) {
				typeResources.add(app);
			}
		}
		return (PebbleAppResource[]) typeResources.toArray();
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != PebbleAppSdk2.class) {
			return super.equals(o);
		}
		PebbleAppSdk2 app = (PebbleAppSdk2) o;
		return super.equals(app) && app.versionCode == this.versionCode;
	}
	
	

}
