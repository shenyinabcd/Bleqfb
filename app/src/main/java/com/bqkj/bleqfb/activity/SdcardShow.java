package com.bqkj.bleqfb.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bqkj.bleqfb.R;

public class SdcardShow extends ListActivity {
	private ArrayList<String> items = null;
	private ArrayList<String> paths = null;
	private String rootPath = "/sdcard/data/qfb/";
	private TextView mPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_history);
		File f = new File(rootPath);
		if (!f.exists()) {
			TextView textView = (TextView)findViewById(R.id.toast);
			textView.setText("文件不存在!");
			return ;
		}
		mPath = (TextView) findViewById(R.id.mPath);
		mPath.setTextColor(Color.RED);
		getFileDir(rootPath);
	}

	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File file = new File(filePath);
		File[] files = file.listFiles();
		if (!filePath.equals(rootPath)) {
			items.add("Back To " + rootPath);
			paths.add(rootPath);
			items.add("Back to ../");
			paths.add(file.getParent());
		}
		for (File fileTemp : files) {
			items.add(fileTemp.getName());
			paths.add(fileTemp.getPath());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				SdcardShow.this, R.layout.file_now, items);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		if (file.canRead()) {
			if (file.isDirectory()) {
				getFileDir(paths.get(position));
			} else {
//				new AlertDialog.Builder(this)
//						.setTitle("Message")
//						.setMessage("[" + file.getName() + "] is a file")
//						.setPositiveButton("ok",
//								new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//									}
//								}).show();
				String thePath = rootPath + file.getName();
				Intent intent = new Intent();
				intent.setClass(SdcardShow.this, AchartAcitivty.class);
				intent.putExtra("filepath", thePath);
				startActivity(intent);

			}
		} else {
			new AlertDialog.Builder(this)
					.setTitle("Message")
					.setMessage("權限不足~")
					.setPositiveButton("ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {
								}
							}).show();
		}
	}
}
