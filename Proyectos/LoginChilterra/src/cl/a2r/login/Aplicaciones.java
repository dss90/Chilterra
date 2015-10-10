package cl.a2r.login;

import java.io.InputStream;
import java.util.ArrayList;

import cl.a2r.custom.GridViewAdapter;
import cl.a2r.custom.RoundedImageView;

import com.example.bt.EIDService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Aplicaciones extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ListView.OnItemClickListener {

	private static final int PROFILE_PIC_SIZE = 75;
	private RoundedImageView roundedImage;
	private GoogleApiClient mGoogleApiClient;
	private GridViewAdapter gridAdapter;
	
	private GridView gridApps;
	private ImageButton menu;
	private ArrayList<ArrayList<String>> apps;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;;
	private ArrayAdapter<String> mAdapter;
	private RelativeLayout fotoPortada;
	private ImageView circleView;
	private TextView predio, nombrePerfil, correoPerfil, confBaston, signOut;
	private int totalItems;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_aplicaciones);
		
		cargarInterfaz();
		mGoogleApiClient = buildGoogleAPIClient();
		getPrediosWS();
		getAppsWS();

	}
	
	private void getAppsWS(){
		//Llama a WS, �ste entrega una lista con: CodigoApp, NombreApp y si esta Activa o no.
		apps.add(new ArrayList<String>());
		apps.get(0).add("RM1");
		apps.get(0).add("Recibo Material");
		apps.get(0).add("Si");
		apps.add(new ArrayList<String>());
		apps.get(1).add("RM1");
		apps.get(1).add("Recibo Material 2");
		apps.get(1).add("Si");
		apps.add(new ArrayList<String>());
		apps.get(2).add("RM1");
		apps.get(2).add("Recibo Material 3");
		apps.get(2).add("Si");
		
		apps.add(new ArrayList<String>());
		apps.get(3).add("RM1");
		apps.get(3).add("Recibo Material 4");
		apps.get(3).add("Si");
		
		
		//Busca cuales apps tiene activas para desplegar en la grilla
		ArrayList<ArrayList<String>> appsActivas = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < apps.size(); i++){
			if (apps.get(i).get(2) == "Si"){
				appsActivas.add(new ArrayList<String>());
				appsActivas.get(appsActivas.size()-1).add(apps.get(i).get(0));
				appsActivas.get(appsActivas.size()-1).add(apps.get(i).get(1));
			}
		}
		
		 gridAdapter = new GridViewAdapter(Aplicaciones.this, appsActivas);
		 gridApps.setAdapter(gridAdapter);
		 gridApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			 public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				 
			 }
		 });
	}
	
	private void cargarInterfaz(){
		predio = (TextView)findViewById(R.id.predio);
		gridApps = (GridView)findViewById(R.id.gridApps);
		apps = new ArrayList<ArrayList<String>>();
		menu = (ImageButton)findViewById(R.id.menu);
		menu.setOnClickListener(this);
	
		mDrawerList = (ListView)findViewById(R.id.left_drawer);
		mDrawerList.setOnItemClickListener(this);
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer2Layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		configurarMenu();
	
	}
	
	private void configurarMenu(){
		
		View inflatedView = getLayoutInflater().inflate(R.layout.drawerlayout_header, null);
		fotoPortada = (RelativeLayout)inflatedView.findViewById(R.id.fotoPortada);
		fotoPortada.setBackground(getResources().getDrawable(R.drawable.google_cover2));
		correoPerfil = (TextView)inflatedView.findViewById(R.id.profile_email);
		nombrePerfil = (TextView)inflatedView.findViewById(R.id.profile_name);
		circleView = (ImageView)inflatedView.findViewById(R.id.circleView);
        mDrawerList.addHeaderView(inflatedView);
        
        
        View footerView = getLayoutInflater().inflate(R.layout.drawerlayout_divider, null);	
	    RelativeLayout relativeLayoutDivider = (RelativeLayout)footerView.findViewById(R.id.relativeLayoutDivider);
	    relativeLayoutDivider.setBackground(getResources().getDrawable(R.color.gris));
	    footerView.setOnClickListener(null);
	    mDrawerList.addFooterView(footerView);
	    
	    View footerView2 = getLayoutInflater().inflate(R.layout.drawerlayout_text, null);
	    confBaston = (TextView)footerView2.findViewById(R.id.footerText);
	    confBaston.setText("Configurar Bast�n");
	    mDrawerList.addFooterView(footerView2);
	    
	    View footerView3 = getLayoutInflater().inflate(R.layout.drawerlayout_text, null);
	    signOut = (TextView)footerView3.findViewById(R.id.footerText);
	    signOut.setText("Cerrar Sesi�n");
	    mDrawerList.addFooterView(footerView3);
	    
	}
	
	private void getPrediosWS(){
		//Llama a WS, �ste entrega una lista con los predios
		
		String[] fundos = { "El Huite 1", "El Huite 2", "Santa Laura", "Santa Genova", "La Monta�a" };
	    mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fundos);
	    mDrawerList.setAdapter(mAdapter);
	}
	
    private GoogleApiClient buildGoogleAPIClient() {
 	   return new GoogleApiClient.Builder(this)
 	   		 .addConnectionCallbacks(this)
 	         .addOnConnectionFailedListener(this)
 	         .addApi(Plus.API, PlusOptions.builder().build())
 	         .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }
 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.aplicaciones, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	} 

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
		case R.id.menu:
			mDrawerLayout.openDrawer(Gravity.LEFT);
			totalItems = mDrawerList.getCount();
			break;
		 
		}
	}
	
	protected  void onStart(){
		super.onStart();
		mGoogleApiClient.connect();
	}
	
	protected void onStop(){
		super.onStop();
		if (mGoogleApiClient.isConnected()){
			mGoogleApiClient.disconnect();
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();

		try {
			//doUnbindService();
			stopService(new Intent(Aplicaciones.this, EIDService.class));
		} catch (Throwable t) {
			//Log.e("MainActivity", "Failed to unbind from the service", t);
		}
	}

	public void onConnectionFailed(ConnectionResult result) {
		
	}

	public void onConnected(Bundle connectionHint) {
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            predio.setText(person.getDisplayName());
            nombrePerfil.setText(person.getDisplayName());
            correoPerfil.setText(Plus.AccountApi.getAccountName(mGoogleApiClient));
            cargarFotoPerfil();
		}
	}

	private void cargarFotoPerfil() {
	    try {
	        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
	            Person currentPerson = Plus.PeopleApi
	                    .getCurrentPerson(mGoogleApiClient);
	            String personPhotoUrl = currentPerson.getImage().getUrl();
	            personPhotoUrl = personPhotoUrl.substring(0,
	                    personPhotoUrl.length() - 2)
	                    + PROFILE_PIC_SIZE;
	 
	            new LoadProfileImage(circleView).execute(personPhotoUrl);
	            
	        } else {
	            Toast.makeText(getApplicationContext(),
	                    "Person information is null", Toast.LENGTH_LONG).show();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	 
	/**
	 * Background Async task to load user profile picture from url
	 * */
	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;
	 
	    public LoadProfileImage(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }
	 
	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }
	 
	    protected void onPostExecute(Bitmap result) {
	    	roundedImage = new RoundedImageView(result);
	        bmImage.setImageDrawable(roundedImage);
	    }
	}
	
	public void onConnectionSuspended(int cause) {
		
	}
	
	private void processSignOut(){
		if (mGoogleApiClient.isConnected()){
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			Toast.makeText(getApplicationContext(), "Sesi�n Finalizada", Toast.LENGTH_LONG).show();
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg2 != 0 && arg2 != totalItems - 1 && arg2 != totalItems - 2){
			//Seleccion� algun predio
			predio.setText((CharSequence) arg0.getItemAtPosition(arg2));
			mDrawerLayout.closeDrawer(mDrawerList);
		}else{
			//Configurar baston
			if (arg2 == totalItems - 2){
				Intent i = new Intent(this, com.example.bt.MainActivity.class);
				startActivity(i);
			}else{
				//Cerrar Sesion
				if (arg2 == totalItems - 1){
					processSignOut();
					finish();
				}
			}
		}
	}

}
