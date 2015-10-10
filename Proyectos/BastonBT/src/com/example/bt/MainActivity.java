package com.example.bt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import android.util.Log;
import com.weyr_associates.eidclient.R;

public class MainActivity extends Activity {
	Button btnService;
	private Boolean KeepScreenOn = false;
	DecimalFormat df = new DecimalFormat();
	ImageView mLogoImage, more;
	BluetoothAdapter mBluetoothAdapter;
	ArrayAdapter<String> mArrayAdapter;
	ArrayList<String> mAddresses;
	ArrayList<String> mNames;
	Set<BluetoothDevice> pairedDevices;
	ListView lv;
	private static final int REQUEST_ENABLE_BT = 1;
	Button closebtn;
	Dialog dialog;
	public static String currentMAC = null;
	ArrayList<String> macsConectadas;
	String currentName = null;
	Intent eid;
	boolean keepGoing;
	Button finddev;
	static String datoBaston = null;
	static boolean canRead = true;
	public static IBinder globService;
	private static String nextActivity = null;
	private ListView dispConectados;
	
	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EIDService.MSG_UPDATE_STATUS:
				Bundle b1 = msg.getData();
				datoBaston = b1.getString("info1");				
				break;
			case EIDService.MSG_UPDATE_LOG_APPEND:
				Bundle b2 = msg.getData();
				//LogMessage(b2.getString("logappend"));
				if (b2.getString("logappend").contains("Failed")) {
					//LogMessage("FOUND!");
					doUnbindService();
					stopService(new Intent(MainActivity.this, EIDService.class));
					keepGoing = true;
					btnService.setEnabled(false);
					finddev.setEnabled(true);
					finddev.setText("Dispositivos Enlazados");
					//nextAct.setVisibility(View.INVISIBLE);
					failedToConnect();
				}else{
					if (b2.getString("logappend").contains("Connected")) {
						finish();
						/*
						if (nextActivity != null) {
							Class cl;
							try {
								cl = Class.forName(nextActivity);
								Intent in = new Intent(MainActivity.this, cl);
								//in.putExtra("datoBaston", datoBaston);
								nombreBaston.setText(currentName);
								nextAct.setEnabled(true);
								//nextAct.setVisibility(View.VISIBLE);
								startActivity(in);
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						*/
					}
				}
				break;
			case EIDService.MSG_UPDATE_LOG_FULL:
				Bundle b3 = msg.getData();
				break;
			case EIDService.MSG_THREAD_SUICIDE:
				//Log.i("Activity", "Service informed Activity of Suicide.");
				doUnbindService();
				stopService(new Intent(MainActivity.this, EIDService.class));
				//LogMessage("Service Stopped");
				keepGoing = true;
				btnService.setEnabled(false);
				finddev.setEnabled(true);
				finddev.setText("Dispositivos Enlazados");
				//nextAct.setVisibility(View.INVISIBLE);
				failedToConnect();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	public void failedToConnect(){
		Dialog dialog = new Dialog(this){		
			public boolean dispatchTouchEvent(MotionEvent event){
		        dismiss();
		        return false;
		    }
		};
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.device_failed);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.show();
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			globService = service;
			try {
				//Register client with service
				Message msg = Message.obtain(null, EIDService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				//Request a status update.
				msg = Message.obtain(null, EIDService.MSG_UPDATE_STATUS, 0, 0);
				mService.send(msg);
				
				//Request full log from service.
				msg = Message.obtain(null, EIDService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
				
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do anything with it
			}
		}
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
			mService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		setContentView(R.layout.main);
		
		btnService = (Button)findViewById(R.id.btnService);
		finddev = (Button)findViewById(R.id.con2);
		//more = (ImageView)findViewById(R.id.more);
		mArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_white_text);
		mAddresses = new ArrayList<String>();
		mNames = new ArrayList<String>();
		keepGoing = true;
		btnService.setEnabled(false);
		finddev.setEnabled(true);
		finddev.setText("Dispositivos Enlazados");
		//nextAct = (Button)findViewById(R.id.nextAct);
		macsConectadas = new ArrayList<String>();
		dispConectados = (ListView)findViewById(R.id.dispConectados);
		
		
		restoreMe(savedInstanceState);
		CheckIfServiceIsRunning();
		powerSave();
		enableBluetooth();
		
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		
	}
	
	public void goNextActivity(View v){		
		Class cl;
		try {
			cl = Class.forName(nextActivity);
			Intent in = new Intent(MainActivity.this, cl);
			startActivity(in);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void goBack(View v){
		super.onBackPressed();
	}
	
	public void goMore(View v){
		
	}
	
	public void enableBluetooth(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
		}
		
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
	}
	
	public void powerSave(){
		//REDUCIR BRILLO
		//WindowManager.LayoutParams lp = getWindow().getAttributes();
        //lp.screenBrightness = 0.1f;
        //getWindow().setAttributes(lp);
        
        //APAGAR GPS
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3")); 
            sendBroadcast(poke);
        }
	}
	
	private String SetDefaultStatusText() {
		String t = "Contact: oogiem@desertweyr.com"; 
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return "Version: " + packageInfo.versionName + "\n" + t;
		} catch (PackageManager.NameNotFoundException e) {
			return t;	
		}
	}
	@Override
	public void onStop(){
		super.onStop();
	}

	
	public void onPause(){
		super.onPause();
	}
	
	
	public void onResume() {
		super.onResume();
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		//datoBaston = "zzz";
		//canRead = true;
		/*
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		KeepScreenOn = preferences.getBoolean("keepscreenon", false);
		
		if (mIsBound) { // Request a status update.
			if (mService != null) {
				try {
					//Request service reload preferences, in case those changed
					Message msg = Message.obtain(null, EIDService.MSG_RELOAD_PREFERENCES, 0, 0);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {}
			}
		}
		
		if (KeepScreenOn) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		*/
	}
	
	@Override
	
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putString("textinfo1", textInfo1.getText().toString());
		outState.putString("connectbuttontext", btnService.getText().toString());
	}
	
	private void restoreMe(Bundle state) {
		if (state!=null) {
			//textInfo1.setText(state.getString("textinfo1"));
			btnService.setText(state.getString("connectbuttontext"));
		}
	}

	public void CheckIfServiceIsRunning() {
		//If the service is running when the activity starts, we want to automatically bind to it.
		if (EIDService.isRunning()) {
			doBindService();
			mLogoImage.setVisibility(View.GONE);
		} else {
			btnService.setText("Conectar");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}


	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	
	final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            mAddresses.add(device.getAddress());
	        }
	    }
	};
	

	public void findDevices(View v){
		//startActivity(new Intent(this, EditPreferences.class));
		if (!mBluetoothAdapter.isEnabled()) {
			enableBluetooth();
			return;
		}
		
		dialog = new Dialog(this){};
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.devices);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(R.drawable.trans_black));
		//dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.show();
		
		/*
		closebtn = (Button) dialog.findViewById(R.id.close);
        closebtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        */
        
        //Correspondiente al dialogo, no mover inicializacion
		lv = (ListView)dialog.findViewById(R.id.devices);
		lv.setAdapter(mArrayAdapter);
		
		mArrayAdapter.clear();
		mAddresses.clear();
		
		int i = 0;
		for (BluetoothDevice dev : pairedDevices) {
            mArrayAdapter.add(dev.getName() + "\n" + dev.getAddress());
            mAddresses.add(dev.getAddress());
            mNames.add(dev.getName());
			i++;
		}
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				currentMAC = mAddresses.get(position);
				currentName = mNames.get(position);
				
				//BORRARRRRRRRRRRRRRRRR
				macsConectadas.add(currentName);
				updateConnectedDevices();
				//------------------------
				
				btnService.setEnabled(true);
				finddev.setText(currentName);
				dialog.dismiss();
			  }
		});
	}
	
	public void updateConnectedDevices(){
		String[] macs = new String[macsConectadas.size()];
		for (int i = 0; i < macsConectadas.size(); i++){
			macs[i] = macsConectadas.get(i);
		}
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, macs);
		dispConectados.setAdapter(mAdapter);
	}
	
	public void goConnect(View v) {
		//mLogoImage.setVisibility(View.GONE);
		if(keepGoing){
			//LogMessage("Starting Service");
			startService(new Intent(MainActivity.this, EIDService.class));
			doBindService();
			keepGoing = false;
			btnService.setEnabled(true);
			finddev.setEnabled(false);
			macsConectadas.add(currentMAC);
			updateConnectedDevices();
		} else {
			doUnbindService();
			stopService(new Intent(MainActivity.this, EIDService.class));
			//LogMessage("Service Stopped");
			keepGoing = true;
			btnService.setEnabled(false);
			finddev.setEnabled(true);
			finddev.setText("Dispositivos Enlazados");
			//nextAct.setVisibility(View.INVISIBLE);
//				if (KeepScreenOn) {
//					getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//				}
		}
	}
	
	private OnClickListener ListenerToggleDisplayMsgType = new OnClickListener() {
		public void onClick(View v){
			if(btnService.getText() != "Connect"){
				if (mService != null) {
					try {
						//Request change of display message type
						Message msg = Message.obtain(null, EIDService.MSG_TOGGLE_LOG_TYPE, 0, 0);
						msg.replyTo = mMessenger;
						mService.send(msg);
					} catch (RemoteException e) {}
				}
			}
		}
	};

	/*
	private void LogMessage(String m) {
		//Check if log is too long, shorten if necessary.
		if (textLog.getText().toString().length() > 4000) {
			String templog = textLog.getText().toString();
			int tempi = templog.length();
			tempi = templog.indexOf("\n", tempi-1000);
			textLog.setText(templog.substring(tempi+1));
		}
		
		textLog.append("\n" + m);
		svLog.post(new Runnable() { 
		    public void run() { 
		    	svLog.fullScroll(ScrollView.FOCUS_DOWN); 
		    } 
		}); 
	}
	*/

	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(this, EIDService.class), mConnection, Context.BIND_AUTO_CREATE);
		//textStat.setText("Connecting...");
		btnService.setText("Desconectar");
		mIsBound = true;
		if (mService != null) {
			try {
				//Request status update
				Message msg = Message.obtain(null, EIDService.MSG_UPDATE_STATUS, 0, 0);
				msg.replyTo = mMessenger;
				mService.send(msg);

				//Request full log from service.
				msg = Message.obtain(null, EIDService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
			} catch (RemoteException e) {}
		}
	}
	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, EIDService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
		btnService.setText("Conectar");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (KeepScreenOn) {
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		try {
			//doUnbindService();
			//stopService(new Intent(MainActivity.this, EIDService.class));
		} catch (Throwable t) {
			//Log.e("MainActivity", "Failed to unbind from the service", t);
		}
	}
}