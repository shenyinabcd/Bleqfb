/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bqkj.bleqfb.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bqkj.bleqfb.MyApplication;
import com.bqkj.bleqfb.R;
import com.bqkj.bleqfb.service.BluetoothLeService;
import com.bqkj.bleqfb.utils.CreateFileClass;
import com.bqkj.bleqfb.utils.SampleGattAttributes;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mDataField;
	private TextView mDataHistory;
	private String mDeviceName;
	private String mDeviceAddress;
	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private ScrollView sv;      //翻页句柄

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	public MyApplication myApp;
	private ActionBar actionBar;

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
									   IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private ProgressDialog dialog = null;
	/**
	 * 接收蓝牙广播
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				Toast.makeText(DeviceControlActivity.this, "蓝牙连接成功", 1).show();
				dialog.show();
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				Toast.makeText(DeviceControlActivity.this, "蓝牙连接断开", Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				clearUI();
				mBluetoothLeService.connect(mDeviceAddress);
				dialog.hide();
//				updateConnectionState(R.string.connected_server);
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				//服务加载完毕
				// Show all the supported services and characteristics on the
				// user interface.
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
				dialog.hide();
				updateConnectionState(R.string.connected_server);

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				//数据显示
				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				dialog.hide();
//				updateConnectionState(R.string.connected_server);
			}

		}
	};

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.

	// 选定service的charactistic的时候触发的事件
	private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
									int groupPosition, int childPosition, long id) {
			if (mGattCharacteristics != null) {
				final BluetoothGattCharacteristic characteristic = mGattCharacteristics
						.get(groupPosition).get(childPosition);

				final int charaProp = characteristic.getProperties();
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
					// If there is an active notification on a characteristic,
					// clear
					// it first so it doesn't update the data field on the user
					// interface.
					if (mNotifyCharacteristic != null) {

						mBluetoothLeService.setCharacteristicNotification(
								mNotifyCharacteristic, false);
						mNotifyCharacteristic = null;
					}
					mBluetoothLeService.readCharacteristic(characteristic);
				}
				if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

					mNotifyCharacteristic = characteristic;
					mBluetoothLeService.setCharacteristicNotification(
							characteristic, true);
				}
				return true;
			}
			return false;
		}
	};

	private void clearUI() {
		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
		mDataField.setText(R.string.no_data);
		mConnectionState.setText(R.string.disconnected);

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		actionBar=getActionBar();
		actionBar.show();

		myApp = new MyApplication();
//		设置主界面视图
//				setContentView(R.layout.layout_slidingmenu_1);
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mGattServicesList.setOnChildClickListener(servicesListClickListner);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);
		mDataHistory = (TextView)findViewById(R.id.history_data);

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		sv = (ScrollView)findViewById(R.id.scrollView);  //得到翻页句柄


	}
	@Override
	protected void onResume() {
		super.onResume();

		dialog = new ProgressDialog(DeviceControlActivity.this);
		dialog.setMessage("正在加载服务");
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gatt_save, menu);
		//return true;
		return super.onCreateOptionsMenu(menu);
	}
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.gatt_save, menu);
//		if (mConnected) {
//			menu.findItem(R.id.menu_connect).setVisible(false);
//			menu.findItem(R.id.menu_disconnect).setVisible(true);
//		} else {
//			menu.findItem(R.id.menu_connect).setVisible(true);
//			menu.findItem(R.id.menu_disconnect).setVisible(false);
//		}
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
//			Toast.makeText(DeviceControlActivity.class, "保存", Toast.LENGTH_SHORT);
				new CreateFileClass(DeviceControlActivity.this,myApp.getFilePath() , mDataHistory.getText().toString()).createFile();

				return true;
			case R.id.action_delete:
				mDataHistory.setText("");
				return true;
			case R.id.action_history:
				Intent it2 = new Intent();
				it2.setClass(this, SdcardShow.class);
				startActivity(it2);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
//			String str = mDataHistory.getText().toString()+"\r\n"+getTime()+data;
//			mDataHistory.setText(str);
			mDataHistory.append(getTime()+data+"\r\n");
			sv.scrollTo(0,mDataHistory.getMeasuredHeight()); //跳至数据最后一页

		}
	}
	/**获取当前系统时间*/
	public String getTime(){

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss;   ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);

		return str;
	}
	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	/**演示如何遍历支持的GATT 服务和特性。 在这个示例中，我们填充绑定到的数据结构ExpandableListView 在UI上。*/
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		// String unknownServiceString =
		// getResources().getString(R.string.unknown_service);
		// String unknownCharaString =
		// getResources().getString(R.string.unknown_characteristic);

		//服务数据
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		//特性数据
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		// 遍历可用的GATT服务
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			// if (uuid.equals("0000fff0-0000-1000-8000-00805f9b34fb")) {
			currentServiceData.put(LIST_NAME,
					SampleGattAttributes.lookup(uuid, "Data CharaString"));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				// charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();

				charas.add(gattCharacteristic);
				currentCharaData.put(LIST_NAME,
						SampleGattAttributes.lookup(uuid, "Data CharaString"));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);

				if (uuid.equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
					final int charaProp = gattCharacteristic.getProperties();

					if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
						// If there is an active notification on a characteristic,
						// clear
						// it first so it doesn't update the data field on the user
						// interface.
						if (mNotifyCharacteristic != null) {
							mBluetoothLeService.setCharacteristicNotification(
									mNotifyCharacteristic, false);
							mNotifyCharacteristic = null;
						}
						mBluetoothLeService.readCharacteristic(gattCharacteristic);
					}
					if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
						mNotifyCharacteristic = gattCharacteristic;
						mBluetoothLeService.setCharacteristicNotification(
								gattCharacteristic, true);
					}
				}

			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
			// }
		}

		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
				this, gattServiceData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
				LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
				android.R.id.text2 }, gattCharacteristicData,
				android.R.layout.simple_expandable_list_item_2, new String[] {
				LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
				android.R.id.text2 });
		mGattServicesList.setAdapter(gattServiceAdapter);

	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

}
