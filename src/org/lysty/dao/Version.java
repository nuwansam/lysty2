package org.lysty.dao;

import org.lysty.util.Utils;

public class Version {
	public int majorVersion;
	public int minorVersion;
	public int subVersion;

	public Version(String versionStr) {
		if (Utils.stringNotNullOrEmpty(versionStr)) {
			String[] cVs = versionStr.split("\\.");
			majorVersion = Integer.parseInt(cVs[0]);
			minorVersion = Integer.parseInt(cVs[1]);
			subVersion = 0;
			if (cVs.length == 3) {
				subVersion = Integer.parseInt(cVs[2]);
			}
		}
	}
}