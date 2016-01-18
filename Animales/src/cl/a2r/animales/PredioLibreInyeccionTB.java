package cl.a2r.animales;

import java.util.ArrayList;
import java.util.List;

import cl.a2r.animales.R;
import cl.a2r.common.AppException;
import cl.a2r.custom.Calculadora;
import cl.a2r.custom.ConnectThread;
import cl.a2r.custom.ConnectedThread;
import cl.a2r.custom.ShowAlert;
import cl.a2r.sip.model.Ganado;
import cl.a2r.sip.model.InyeccionTB;
import cl.a2r.sip.model.PPD;
import cl.a2r.sip.wsservice.WSPredioLibreCliente;
import cl.ar2.sqlite.servicio.PredioLibreServicio;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PredioLibreInyeccionTB extends Fragment implements View.OnClickListener{

	private Activity act;
	private ImageButton confirmarAnimal;
	private Spinner spinnerPPD;
	private View v;
	private TextView tvDiio, tvFaltantes, tvEncontrados;
	private LinearLayout llEncontrados, llFaltantes;
	private InyeccionTB ganTB;
	private Integer instancia;
	public static List<InyeccionTB> listEncontrados;
	public static List<Ganado> listFaltantes;
	
	public PredioLibreInyeccionTB(Activity act){
		this.act = act;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	v = inflater.inflate(R.layout.fragment_inyeccion_tb, container, false);
    	tvDiio = (TextView)v.findViewById(R.id.tvDiio);
    	tvDiio.setOnClickListener(this);
    	tvFaltantes = (TextView)v.findViewById(R.id.tvFaltantes);
    	tvEncontrados = (TextView)v.findViewById(R.id.tvEncontrados);
    	confirmarAnimal = (ImageButton)v.findViewById(R.id.confirmarAnimal);
    	confirmarAnimal.setOnClickListener(this);
    	llEncontrados = (LinearLayout)v.findViewById(R.id.llEncontrados);
    	llEncontrados.setOnClickListener(this);
    	llFaltantes = (LinearLayout)v.findViewById(R.id.llFaltantes);
    	llFaltantes.setOnClickListener(this);
    	spinnerPPD = (Spinner)v.findViewById(R.id.spinnerPPD);
    	ganTB = new InyeccionTB();
    	cargarListeners();
    	mostrarCandidatos();
    	getPPDWS();
    	
		Bundle extras = act.getIntent().getExtras();
		if (extras != null) {
		    instancia = extras.getInt("instancia");
		}
    
    	return v;
	}
	
	private void getPPDWS(){
		try {
			List<PPD> list = WSPredioLibreCliente.traeTuberculinaPPD();
			ArrayAdapter<PPD> mApdater = new ArrayAdapter<PPD>(act, android.R.layout.simple_list_item_1, list);
			spinnerPPD.setAdapter(mApdater);
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), act);
		}
	}
	
	private void mostrarCandidatos(){
		try {
			List<InyeccionTB> list = PredioLibreServicio.traeGanadoPL();
			listEncontrados = new ArrayList<InyeccionTB>();
			for (InyeccionTB tb : list){
				if (tb.getFundoId().intValue() == Aplicaciones.predioWS.getId()){
					listEncontrados.add(tb);
				}
			}
			List<Ganado> listTodos = PredioLibreServicio.traeDiioFundo(Aplicaciones.predioWS.getId());
			listFaltantes = new ArrayList<Ganado>();
			for (Ganado g : listTodos){
				boolean isEncontrado = false;
				for (InyeccionTB tb : listEncontrados){
					if (g.getId().intValue() == tb.getGanadoID().intValue()){
						isEncontrado = true;
					}
				}
				if (!isEncontrado){
					listFaltantes.add(g);
				}
			}
			tvFaltantes.setText(Integer.toString(listFaltantes.size()));
			tvEncontrados.setText(Integer.toString(listEncontrados.size()));
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), act);
		}
	}
	
	private void cargarListeners(){
		spinnerPPD.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ganTB.setTuboPPDId(((PPD) arg0.getItemAtPosition(arg2)).getId());
				updateStatus();
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	private void updateStatus(){
		if (ganTB.getGanadoDiio() == null &&
				ganTB.getGanadoID() == null){
			tvDiio.setGravity(Gravity.LEFT);
			tvDiio.setText("DIIO:");
		}
		
		if (ganTB.getGanadoID() != null &&
				ganTB.getGanadoDiio() != null &&
				ganTB.getTuboPPDId() != null){
			
			confirmarAnimal.setEnabled(true);
		} else {
			confirmarAnimal.setEnabled(false);
			
		}
		
	}
	
	public void onClick(View v) {
		int id = v.getId();
		Intent i;
		switch (id){
		case R.id.confirmarAnimal:
			agregarAnimal();
			break;
		case R.id.tvDiio:
			startActivity(new Intent(act, Calculadora.class));
			break;
		case R.id.llFaltantes:
			i = new Intent(act, Candidatos.class);
			i.putExtra("stance", "predioLibreFaltantes");
			startActivity(i);
			break;
		case R.id.llEncontrados:
			i = new Intent(act, Candidatos.class);
			i.putExtra("stance", "predioLibreEncontrados");
			startActivity(i);
			break;
		}
	}
	
	private void agregarAnimal(){
		try {
			ganTB.setSincronizado("N");
			ganTB.setInstancia(instancia);
			boolean exists = PredioLibreServicio.existsGanadoPL(ganTB.getGanadoID());
			if (!exists){
				PredioLibreServicio.insertaGanadoPL(ganTB);
				mostrarCandidatos();
			} else {
				Toast.makeText(act, "Animal ya existe", Toast.LENGTH_LONG).show();
			}
			ganTB = new InyeccionTB();
			ganTB.setTuboPPDId(((PPD) spinnerPPD.getSelectedItem()).getId());
			updateStatus();
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), act);
		}
	}
	
	private void resetCalculadora(){
		Calculadora.gan = null;
	}
	
	private void checkDiioStatus(Ganado gan){
		if (gan != null){
			ganTB.setGanadoID(gan.getId());
			ganTB.setGanadoDiio(gan.getDiio());
			ganTB.setFundoId(gan.getPredio());
			tvDiio.setText(Integer.toString(ganTB.getGanadoDiio()));
			tvDiio.setGravity(Gravity.CENTER_HORIZONTAL);
		}
		updateStatus();
	}
	
	public void onDestroy(){
		super.onStart();

		Calculadora.isPredioLibre = false;
		resetCalculadora();
	}
	
	public void onStart(){
		super.onStart();
		
		Calculadora.isPredioLibre = true;
		ConnectThread.setHandler(mHandler);
		
		checkDiioStatus(Calculadora.gan);
	}
	
	//---------------------------------------------------------------------------
	//------------------------DATOS ENVIADOS DESDE BAST�N------------------------
	//---------------------------------------------------------------------------
	
    private Handler mHandler = new Handler(){
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
    			EID = EID.trim();
    			long temp = Long.parseLong(EID);
    			EID = Long.toString(temp);
    			try {
					Ganado g = PredioLibreServicio.traeEID(EID);
					if (g != null){
						checkDiioStatus(g);
					} else {
						ShowAlert.showAlert("Error", "DIIO no existe", act);
					}
				} catch (AppException e) {
					ShowAlert.showAlert("Error", e.getMessage(), act);
				}
    			break;
    		case ConnectedThread.CONNECTION_INTERRUPTED:
    			ShowAlert.askReconnect("Error", "Se perdi� la conexi�n con el bast�n\n�Intentar reconectar?", act, (BluetoothDevice) msg.obj);
    			break;
    		case ConnectThread.RETRY_CONNECTION:
    			ConnectThread connectThread = new ConnectThread((BluetoothDevice) msg.obj, true);
    			connectThread.start();
    			break;
    		}
    	}
    };
	
}
