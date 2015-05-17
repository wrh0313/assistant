package com.wrh.assistant.utils;

import android.content.Context;
import android.content.pm.PackageInfo;

public class ApkUtil {
	
	public static boolean isApkInstalled(Context context, String packageName) {
		boolean rc = false;
		
		if(context == null) {
			return rc;
		}
		try{
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);
			if(packageInfo != null) {
				rc = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return rc;
	}
}
