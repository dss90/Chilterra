package cl.a2r.animales;

import java.util.List;

import cl.a2r.common.AppException;
import cl.a2r.custom.Calculadora;
import cl.a2r.custom.ConnectThread;
import cl.a2r.custom.ConnectedThread;
import cl.a2r.custom.ShowAlert;
import cl.a2r.login.R;
import cl.a2r.sip.model.Baja;
import cl.a2r.sip.model.CausaBaja;
import cl.a2r.sip.model.Ganado;
import cl.a2r.sip.model.MotivoBaja;
import cl.a2r.sip.wsservice.WSBajasCliente;
import cl.a2r.sip.wsservice.WSGanadoCliente;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BajaGanado extends Activity implements View.OnClickListener {
	
	private TextView despliegaDiio, textViewDiio, tvApp, deshacer;
	private Spinner spinnerMotivo, spinnerCausa;
	private ImageButton goBack, confirmarBaja, undo, logs;
	private RelativeLayout calculadora;
	private int diio;
	
	public static Baja bajaWS = new Baja();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_baja_ganado);
		
		bajaWS.setUserId(Login.user);
		bajaWS.setPredioId(Aplicaciones.predioWS.getId());
		//Se inicializan en 0 para poder usar la funcion updateStatus()
		bajaWS.setGanadoId(0);
		bajaWS.setMotivoId(0);
		bajaWS.setCausaId(0);
		
		cargarInterfaz();
		cargarListeners();
		getMotivosBajaWS();
		getCausasBajaWS();
	}
	
	private void cargarInterfaz(){
		despliegaDiio = (TextView)findViewById(R.id.despliegaDiio);
		despliegaDiio.setOnClickListener(this);
		spinnerMotivo = (Spinner)findViewById(R.id.motivoBaja);
		spinnerCausa = (Spinner)findViewById(R.id.causaBaja);
		goBack = (ImageButton)findViewById(R.id.goBack);
		goBack.setOnClickListener(this);
		confirmarBaja = (ImageButton)findViewById(R.id.confirmarBaja);
		confirmarBaja.setOnClickListener(this);
		undo = (ImageButton)findViewById(R.id.undo);
		undo.setOnClickListener(this);
		calculadora = (RelativeLayout)findViewById(R.id.layoutCalculadora);
		calculadora.setOnClickListener(this);
		textViewDiio = (TextView)findViewById(R.id.textViewDiio);
		textViewDiio.setOnClickListener(this);
		tvApp = (TextView)findViewById(R.id.app);
		deshacer = (TextView)findViewById(R.id.deshacer);
		deshacer.setOnClickListener(this);
		logs = (ImageButton)findViewById(R.id.logs);
		logs.setOnClickListener(this);
		
	}
	
	private void cargarListeners(){
		spinnerMotivo.setOnItemSelectedListener(new OnItemSelectedListener(){
			
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				bajaWS.setMotivoId(((MotivoBaja) arg0.getSelectedItem()).getId());
				updateStatus();
			}
			
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		spinnerCausa.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				bajaWS.setCausaId(((CausaBaja) arg0.getSelectedItem()).getId());
				updateStatus();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	private void getMotivosBajaWS(){
		List<MotivoBaja> listaMotivos = null;
        try {
        	listaMotivos = WSBajasCliente.traeMotivos();
        } catch (AppException ex) {
            ShowAlert.showAlert("Error", ex.getMessage(), this);
        }
        
        //Se agrega un campo nulo para que el spinner comience en blanco
        MotivoBaja mb = new MotivoBaja();
        mb.setId(0);
        mb.setNombre("");
        listaMotivos.add(0, mb);
		
		ArrayAdapter<MotivoBaja> mAdapter = new ArrayAdapter<MotivoBaja>(this, android.R.layout.simple_list_item_1, listaMotivos);
		spinnerMotivo.setAdapter(mAdapter);
	}
	
	@SuppressWarnings("unchecked")
	private void getCausasBajaWS(){
		List<CausaBaja> listaCausas = null;
        try {
        	listaCausas = WSBajasCliente.traeCausas();
        } catch (AppException ex) {
            ShowAlert.showAlert("Error", ex.getMessage(), this);
        }
        
        //Se agrega un campo nulo para que el spinner comience en blanco
		CausaBaja cb = new CausaBaja();
		cb.setId(0);
		cb.setNombre("");
		listaCausas.add(0, cb);
        
		ArrayAdapter<CausaBaja> mAdapter2 = new ArrayAdapter<CausaBaja>(this, android.R.layout.simple_list_item_1, listaCausas);
		spinnerCausa.setAdapter(mAdapter2);
	}

	public void onClick(View v) {
		if (isOnline() == false){
			return;
		}
		int id = v.getId();
		switch (id){
		case R.id.goBack:
			finish();
			break;
		case R.id.confirmarBaja:
			//WSBajasCliente.realizarMovimiento(bajaWS.getGanadoId(), bajaWS.getPredioId())
			//WSBajasCliente.insertaBaja(bajaWS)
			System.out.println("User: " + bajaWS.getUserId());
			System.out.println("Predio: " + bajaWS.getPredioId());
			System.out.println("Diio: " + diio);
			System.out.println("Sr_Ganado: " + bajaWS.getGanadoId());
			System.out.println("Motivo Baja: " + bajaWS.getMotivoId());
			System.out.println("Causa Baja: " + bajaWS.getCausaId());
			//Intent i5 = new Intent(this, Aplicaciones.class);
			//i5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//startActivity(i5);
			finish();
			Toast.makeText(getApplicationContext(), "Registro guardado exitosamente", Toast.LENGTH_LONG).show();
			break;
		case R.id.layoutCalculadora:
		case R.id.despliegaDiio:
		case R.id.textViewDiio:
			Intent i = new Intent(this, Calculadora.class);
			startActivity(i);
			break;
		case R.id.undo:
		case R.id.deshacer:
			//texto que dice "Deshacer"
			Calculadora.ganadoId = 0;
			Calculadora.diio = 0;
			bajaWS.setGanadoId(0);
			bajaWS.setMotivoId(0);
			bajaWS.setCausaId(0);
			spinnerMotivo.setSelection(0);
			spinnerCausa.setSelection(0);
			updateStatus();
			break;
		case R.id.logs:
			Intent i2 = new Intent(this, Logs.class);
			startActivity(i2);
			break;
		}
	}
	
	private void updateStatus(){
		
		//Si escribi� un DIIO en la calculadora, el texto 'DIIO:' desaparece
		if (bajaWS.getGanadoId() != 0){
			textViewDiio.setVisibility(View.INVISIBLE);
			despliegaDiio.setText(Integer.toString(diio));
		}else{
			textViewDiio.setVisibility(View.VISIBLE);
			despliegaDiio.setText("");
		}
		
		//Si llen� todos los campos, el boton de confirmacion aparece
		if (bajaWS.getGanadoId() != 0 && bajaWS.getMotivoId() != 0 && bajaWS.getCausaId() != 0){
			goBack.setVisibility(View.INVISIBLE);
			tvApp.setVisibility(View.INVISIBLE);
			logs.setVisibility(View.INVISIBLE);
			undo.setVisibility(View.VISIBLE);
			deshacer.setVisibility(View.VISIBLE);
			confirmarBaja.setEnabled(true);
			
		}else{
			//Si todos los campos estan vacios, da la opcion de volver atras o ir al historial
			if (bajaWS.getGanadoId() == 0 && bajaWS.getMotivoId() == 0 && bajaWS.getCausaId() == 0){
				tvApp.setVisibility(View.VISIBLE);
				logs.setVisibility(View.VISIBLE);
				goBack.setVisibility(View.VISIBLE);
				undo.setVisibility(View.INVISIBLE);
				deshacer.setVisibility(View.INVISIBLE);
				confirmarBaja.setEnabled(false);
			}else{
				//Si hay algun campo con informacion pero no todos estan llenos a�n, da la opci�n de deshacer
				goBack.setVisibility(View.INVISIBLE);
				tvApp.setVisibility(View.INVISIBLE);
				logs.setVisibility(View.INVISIBLE);
				undo.setVisibility(View.VISIBLE);
				deshacer.setVisibility(View.VISIBLE);
				confirmarBaja.setEnabled(false);
			}
		}
	}

	protected  void onStart(){
		super.onStart();
		ConnectThread.setHandler(mHandler);
		
		if (isOnline() == false){
			return;
		}
		
		checkDiioStatus(Calculadora.diio, Calculadora.ganadoId, Calculadora.activa, Calculadora.predio);
	}
	
	protected  void onDestroy(){
		super.onStart();
		
		//Se debe limpiar el diio y ganadoId, ya que otra app puede tomar el diio de la calculadora.
		Calculadora.ganadoId = 0;
		Calculadora.diio = 0;
		Calculadora.predio = 0;
		Calculadora.activa = "";
		
	}
	
	public void onBackPressed(){
		if (isOnline()){
			finish();
		}
	}
	
	private boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if ((netInfo != null && netInfo.isConnectedOrConnecting()) == false){
	    	ShowAlert.showAlert("Error", "No hay conexi�n a Internet", this);
	    }
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	private void checkDiioStatus(int diio, int ganadoId, String activa, int predio){
		this.diio = diio;
		
		if (activa.equals("N")){
			ShowAlert.showAlert("Error", "DIIO no existe", this);
			return;
		}
		
		bajaWS.setGanadoId(ganadoId);
		if (bajaWS.getGanadoId() != 0){
			if (bajaWS.getPredioId() != predio){
				ShowAlert.realizarMovimiento("Predio", "El Animal figura en otro predio\n�Esta seguro que el DIIO es correcto?", this);
			}
		}
		updateStatus();
	}
	
	//---------------------------------------------------------------------------
	//------------------------DATOS ENVIADOS DESDE BAST�N------------------------
	//---------------------------------------------------------------------------
	
    private Handler mHandler = new Handler(){
    	@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		switch(msg.what){
    		case ConnectThread.SUCCESS_CONNECT:
    			BluetoothSocket mmSocket = (BluetoothSocket) ((List<Object>) msg.obj).get(0);
    			BluetoothDevice mmDevice = (BluetoothDevice) ((List<Object>) msg.obj).get(1);
    	        ConnectedThread connectedThread = new ConnectedThread(mmSocket, mmDevice);
    	        connectedThread.start();
    			break;
    		case ConnectedThread.MESSAGE_READ:
    			String EID = (String) msg.obj;
    			try {
					List<Ganado> list = WSGanadoCliente.traeDIIO(EID);
					if (list.size() == 0){
						ShowAlert.showAlert("Error", "DIIO no existe", BajaGanado.this);
						return;
					}
					for (Ganado g: list){
						checkDiioStatus(g.getDiio(), g.getId(), g.getActiva(), g.getPredio());
					}
				} catch (AppException ex) {
					ShowAlert.showAlert("Error", ex.getMessage(), BajaGanado.this);
				}
    			break;
    		case ConnectedThread.CONNECTION_INTERRUPTED:
    			ShowAlert.askReconnect("Error", "Se perdi� la conexi�n con el bast�n\n�Intentar reconectar?", BajaGanado.this, (BluetoothDevice) msg.obj);
    			break;
    		case ConnectThread.RETRY_CONNECTION:
    			ConnectThread connectThread = new ConnectThread((BluetoothDevice) msg.obj, true);
    			connectThread.start();
    			break;
    		}
    	}
    };
	
}
