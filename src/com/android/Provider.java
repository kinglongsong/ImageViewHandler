package com.android;

import android.os.Environment;

public class Provider {

	public static String getImageViewPath() {
		String result = null;
		result = Environment.getExternalStorageDirectory() + "/images/Lion.jpg";
		return result;
	}

}
