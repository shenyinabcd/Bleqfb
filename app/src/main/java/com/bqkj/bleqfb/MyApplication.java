package com.bqkj.bleqfb;

import android.annotation.SuppressLint;
import android.app.Application;

@SuppressLint("SdCardPath") public class MyApplication extends Application{

	public static String filePath = "/sdcard/data/qfb/";
	
	public String FILENAME = "";
	
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		filePath = "/sdcard/data/qfb/";
	}
	public void setFileName(String filename){
		this.FILENAME = filename;
	}
	public String getFileName(){
		
		return FILENAME;
	} 
	public String getFilePath(){
		
		return filePath;
	}
}
