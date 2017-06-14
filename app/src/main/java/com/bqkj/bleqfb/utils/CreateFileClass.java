package com.bqkj.bleqfb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bqkj.bleqfb.MyApplication;
import com.bqkj.bleqfb.R;

public class CreateFileClass {

	//	public String filename;
	public Context mContext;
	//	public String mCompleteName;
	public String mFilepath;
	public String mFileContent;
	public MyApplication mApp;
	private String mfilename;
	private String fieldfile;
	/**
	 * 保存文件
	 * @param mContext 上下文
	 * @param filepath 保存的文件路径
	 * @param fileContent 保存的文件内容
	 */
	public CreateFileClass(Context mContext, String filepath,String fileContent) {
		this.mContext = mContext;
		this.mFilepath = filepath;
		this.mFileContent = fileContent;
	}
	public void createFile() {
		Calendar c = Calendar.getInstance();
		Integer mYear = c.get(Calendar.YEAR);
		Integer mMonth = c.get(Calendar.MONTH) + 1;
		Integer mDay = c.get(Calendar.DAY_OF_MONTH);
		Integer mHour = c.get(Calendar.HOUR_OF_DAY);
		Integer mMinute = c.get(Calendar.MINUTE);
		Integer mSecond = c.get(Calendar.SECOND);

		String smonth = mMonth.toString();
		String sday = mDay.toString();
		String shour = mHour.toString();
		String sminute = mMinute.toString();
		String sseond = mSecond.toString();
		if (mMonth < 10) {
			smonth = "0" + smonth;
		}
		if (mDay < 10) {
			sday = "0" + sday;
		}
		if (mHour < 10) {
			shour = "0" + shour;
		}
		if (mMinute < 10) {
			sminute = "0" + sminute;
		}
		if (mSecond < 10) {
			sseond = "0" + sseond;
		}
		String filename = mYear.toString() + smonth + sday + shour + sminute + sseond;
		fieldfile = filename + ".txt";
		mfilename = filename;

		mApp = new MyApplication();
//		mApp.setFileName(filename);
		// 显示对话框输入文件名
		LayoutInflater factory = LayoutInflater.from(mContext); // 图层模板生成器句柄
		final View DialogView = factory.inflate(R.layout.dialog_sname, null); // 用sname.xml模板生成视图模板
		new AlertDialog.Builder(mContext).setTitle("保存数据到...")
				.setMessage("完整路径：" + "\r\n" + mFilepath + filename+".txt")
				.setView(DialogView) // 设置视图模板
				.setPositiveButton("确定", new DialogInterface.OnClickListener() // 确定按键响应函数
				{
					public void onClick(DialogInterface dialog,
										int whichButton) {
						try {
							if (Environment.getExternalStorageState()
									.equals(Environment.MEDIA_MOUNTED)) { // 如果SD卡已准备好

								mfilename = mfilename + ".txt"; // 在文件名末尾加上.txt
								// fname.setText(filename);
//										String strs[] = mFilepath.split("/");
								File sdCardDir = Environment
										.getExternalStorageDirectory(); // 得到SD卡根目录
								File BuildDir1 = new File(sdCardDir,
										"/data"); // 打开data目录，如不存在则生成
								File BuildDir = new File(BuildDir1,
										"/qfb");
								if (BuildDir.exists() == false)
									BuildDir.mkdirs();
								File saveFile = new File(BuildDir, mfilename); // 新建文件句柄，如已存在仍新建文档
								saveFile.createNewFile();
//										FileOutputStream outStream = new FileOutputStream(
//												"/sdcard/data/laba/" + mFilename, true); // 打开文件输入流
//										outStream.write(mFileContent.getBytes());
//										outStream.close();

//										saveThread myThread = new saveThread(filename, mFileContent);
//								        new Thread(myThread).start();
								save(fieldfile, mFileContent);
							} else {
								Toast.makeText(mContext, "没有存储卡！", Toast.LENGTH_LONG).show();
							}
						} catch (Exception e) {
							e.toString();
							return;
						}
					}
				}).setNegativeButton("取消", // 取消按键响应函数,直接退出对话框不做任何处理
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
										int which) {
					}
				}).show(); // 显示对话框
	}

//	class saveThread implements Runnable{
//
//		public String fileString;
//		public String saveString;
//
//		public saveThread(String fileString , String saveString) {
//			this.fileString = fileString;
//			this.saveString = saveString;
//		}
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		save(fileString, saveString);
//	}}

	public void save(String name,String content) {
		try {
			FileOutputStream outStream = new FileOutputStream(
					mApp.getFilePath() + name, true); // 打开文件输入流
			outStream.write(content.getBytes());
			outStream.close();
			Toast.makeText(mContext,"保存成功", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.toString();
			Toast.makeText(mContext, "保存失败", Toast.LENGTH_LONG).show();
		}
	}
}
