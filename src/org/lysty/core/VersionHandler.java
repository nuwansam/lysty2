package org.lysty.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lysty.dao.Version;
import org.lysty.util.Utils;

public class VersionHandler {
	private final static String USER_AGENT = "Mozilla/5.0";
	private static final Object CURRENT_VERSION = "current_version";
	private static Logger logger = Logger.getLogger(VersionHandler.class);

	public static boolean isNewVersionAvailable() {
		String url = "https://api.github.com/repos/nuwansam/lysty2/releases";
		try {
			String response = sendGet(url);
			JSONArray array = new JSONArray(response);
			JSONObject obj;
			String tagName;
			int majorV;
			int minorV;
			int subV;
			String[] versionStrs;
			Version currentV = getCurrentVersion();
			for (int i = 0; i < array.length(); i++) {
				obj = array.getJSONObject(i);
				tagName = obj.get("tag_name").toString();
				if (Utils.stringNotNullOrEmpty(tagName)) {
					tagName = tagName.substring(1);
					System.out.println(tagName);
					versionStrs = tagName.split("\\.");
					majorV = Integer.parseInt(versionStrs[0]);
					minorV = Integer.parseInt(versionStrs[1]);
					if (majorV > currentV.majorVersion) {
						return true;
					} else if (majorV == currentV.majorVersion) {
						if (minorV > currentV.minorVersion) {
							return true;
						}
					}
				}
			}
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	public static Version getCurrentVersion() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(
					"config/version.properties")));
			String currentVersion = properties.get(CURRENT_VERSION).toString();
			return new Version(currentVersion);
		} catch (Exception e) {
			logger.error("Error loading version properties", e);
		}
		return new Version("0.0.0");
	}

	// HTTP GET request
	private static String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		if (responseCode != 200) {
			return null;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();

	}

}
