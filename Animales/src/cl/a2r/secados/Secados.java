package cl.a2r.secados;

import java.util.ArrayList;
import java.util.List;

import cl.a2r.animales.Aplicaciones;
import cl.a2r.animales.R;
import cl.a2r.common.AppException;
import cl.a2r.custom.Calculadora;
import cl.a2r.custom.ConnectThread;
import cl.a2r.custom.ConnectedThread;
import cl.a2r.custom.ShowAlert;
import cl.a2r.ecografias.Ecografias;
import cl.a2r.sip.model.Ecografia;
import cl.a2r.sip.model.EstadoLeche;
import cl.a2r.sip.model.Ganado;
import cl.a2r.sip.model.Medicamento;
import cl.a2r.sip.model.Traslado;
import cl.a2r.sip.wsservice.WSSecadosCliente;
import cl.ar2.sqlite.servicio.EcografiasServicio;
import cl.ar2.sqlite.servicio.PredioLibreServicio;
import cl.ar2.sqlite.servicio.SecadosServicio;
import cl.ar2.sqlite.servicio.TrasladosServicio;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class Secados extends Activity implements View.OnClickListener {

	private ImageButton goBack, logs, confirmarAnimal, sync, cerrarMangada;
	private TextView tvLogs, tvDiio, tvTotalAnimales, tvMangada, tvAnimalesMangada;
	private Spinner spEstado, spMedicamento;
	private ProgressBar loading;
	private List<Medicamento> medList;
	private List<EstadoLeche> estList;
	private Integer mangadaActual;
	private boolean isMangadaCerrada;
	private Ganado gan;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_secados);
		
		cargarInterfaz();
		traeDatosWS();
	}
	
	private void cargarInterfaz(){
		goBack = (ImageButton)findViewById(R.id.goBack);
		goBack.setOnClickListener(this);
		logs = (ImageButton)findViewById(R.id.logs);
		logs.setOnClickListener(this);
		sync = (ImageButton)findViewById(R.id.sync);
		sync.setOnClickListener(this);
		cerrarMangada = (ImageButton)findViewById(R.id.cerrarMangada);
		cerrarMangada.setOnClickListener(this);
		confirmarAnimal = (ImageButton)findViewById(R.id.confirmarAnimal);
		confirmarAnimal.setOnClickListener(this);
		tvDiio = (TextView)findViewById(R.id.tvDiio);
		tvDiio.setOnClickListener(this);
		tvLogs = (TextView)findViewById(R.id.tvLogs);
    	tvTotalAnimales = (TextView)findViewById(R.id.tvTotalAnimales);
    	tvMangada = (TextView)findViewById(R.id.tvMangada);
    	tvAnimalesMangada = (TextView)findViewById(R.id.tvAnimalesMangada);
    	spEstado = (Spinner)findViewById(R.id.spEstado);
    	spEstado.setEnabled(false);
    	spMedicamento = (Spinner)findViewById(R.id.spMedicamento);
		loading = (ProgressBar)findViewById(R.id.loading);
		loading.setVisibility(View.INVISIBLE);
		
		medList = new ArrayList<Medicamento>();
		estList = new ArrayList<EstadoLeche>();
		isMangadaCerrada = false;
		gan = new Ganado();
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id){
		case R.id.goBack:
			finish();
			break;
		case R.id.logs:
			break;
		case R.id.confirmarAnimal:
			agregarAnimal();
			break;
		case R.id.tvDiio:
			startActivity(new Intent(this, Calculadora.class));
			break;
		case R.id.cerrarMangada:
			verificarCierreMangada();
			break;
		case R.id.sync:
			break;
		}
	}
	
	private void traeDatosWS(){
		new AsyncTask<Void, Void, Void>(){
			String title, msg;
			List<Ganado> ganList;
			
			protected void onPreExecute(){
				loading.setVisibility(View.VISIBLE);
				title = "";
				msg = "";
			}
			
			protected Void doInBackground(Void... arg0) {
				try {
					ganList = WSSecadosCliente.traeAllDiio();
					System.out.println("ganList: "+ganList.size());
					SecadosServicio.deleteSincronizados();
					List<Ganado> ganExistentes = SecadosServicio.traeDiios();
					List<Ganado> toAdd = new ArrayList<Ganado>();
					for (Ganado g1 : ganList){
						boolean exists = false;
						for (Ganado g2 : ganExistentes){
							if (g1.getId().intValue() == g2.getId().intValue()){
								exists = true;
								break;
							}
						}
						if (!exists){
							toAdd.add(g1);
						}
					}
					System.out.println("toAdd: "+toAdd.size());
					SecadosServicio.insertaDiio(toAdd);

					medList = WSSecadosCliente.traeMedicamentos(Aplicaciones.appId);
					estList = WSSecadosCliente.traeEstadosLeche();
					
				} catch (AppException e) {
					title = "Error";
					msg = e.getMessage();
				}
				return null;
			}
			
			protected void onPostExecute(Void result){
				loading.setVisibility(View.INVISIBLE);
				if (title.equals("Error")){
					ShowAlert.showAlert(title, msg, Secados.this);
				} else {
					Medicamento m = new Medicamento();
					medList.add(0, m);
					ArrayAdapter<Medicamento> mAdapter1 = new ArrayAdapter<Medicamento>(Secados.this, android.R.layout.simple_list_item_1, medList);
					spMedicamento.setAdapter(mAdapter1);
					
					ArrayAdapter<EstadoLeche> mAdapter2 = new ArrayAdapter<EstadoLeche>(Secados.this, android.R.layout.simple_list_item_1, estList);
					spEstado.setAdapter(mAdapter2);
				}
			}
	
		}.execute();
	}
	
	private void agregarAnimal(){
		if (isMangadaCerrada){
			isMangadaCerrada = false;
			mangadaActual++;
		}
		
		try {
			SecadosServicio.updateDiio(gan);
			Toast.makeText(this, "Animal Registrado", Toast.LENGTH_LONG).show();
			clearScreen();
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), this);
		}
	}
	
	private void verificarCierreMangada(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Title");
	
		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(input);
		// Set up the buttons
		builder.setTitle("Animales");
		builder.setMessage("Ingrese numero de Animales en Mangada");
		builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	dialog.cancel();
		    }
		});
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	try {
		    		int ingresados = Integer.parseInt(input.getText().toString());
					List<Ganado> list = SecadosServicio.traeDiios();
					int totalesManga = 0;
					for (Ganado g : list){
						if (g.getMangada().intValue() == mangadaActual.intValue()){
							totalesManga++;
						}
					}
					if (totalesManga == ingresados){
						cerrarMangada(true);
					} else {
						ShowAlert.showAlert("Error", "El n�mero de animales ingresado no coincide con la mangada actual", Secados.this);
					}
				} catch (AppException e) {
					ShowAlert.showAlert("Error", e.getMessage(), Secados.this);
				} catch (NumberFormatException e){
					ShowAlert.showAlert("Error", "Debe ingresar un numero", Secados.this);
				}
		    	
		    }
		});
	
		builder.show();
	}
	
	private void cerrarMangada(boolean showMsg){
		if (showMsg){
			Toast.makeText(this, "Mangada Cerrada", Toast.LENGTH_LONG).show();
		}
		isMangadaCerrada = true;
		updateStatus();
	}
	
	private void mostrarCandidatos(){
		try {
			List<Ganado> list = SecadosServicio.traeDiios();
			if (list.size() > 0){
				tvLogs.setText(Integer.toString(list.size()));
			} else {
				tvLogs.setText("");
			}
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), this);
		}
	}
	
	private void updateStatus(){
		if (gan.getId() != null && gan.getDiio() != null){
			tvDiio.setText(Integer.toString(gan.getDiio()));
			tvDiio.setGravity(Gravity.CENTER_HORIZONTAL);
			confirmarAnimal.setEnabled(true);
		} else {
			tvDiio.setGravity(Gravity.LEFT);
			tvDiio.setText("DIIO:");
			confirmarAnimal.setEnabled(false);
		}
		
		if (isMangadaCerrada){
			cerrarMangada.setEnabled(false);
		} else {
			cerrarMangada.setEnabled(true);
		}
		
		try {
			List<Ganado> list = SecadosServicio.traeDiios();
			tvTotalAnimales.setText(Integer.toString(list.size()));
			tvMangada.setText(Integer.toString(mangadaActual));
			int animalesMangada = 0;
			for (Ganado g : list){
				if (g.getMangada().intValue() == mangadaActual.intValue()){
					animalesMangada++;
				}
			}
			tvAnimalesMangada.setText(Integer.toString(animalesMangada));
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), this);
		}
	}
	
	private void clearScreen(){
		gan = new Ganado();
		resetCalculadora();
		updateStatus();
	}
	
	private void resetCalculadora(){
		Calculadora.gan = null;
	}
	
	private void verReubicacion(final Ganado gan){
		if (gan.getPredio().intValue() != Aplicaciones.predioWS.getId().intValue()){
			ShowAlert.askYesNo("Predio", "El Animal figura en otro predio\n�Esta seguro que el DIIO es correcto?", this, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					if (arg1 == -2){
						try {
							Traslado t = new Traslado();
							t.setFundoOrigenId(gan.getPredio());
							t.setFundoDestinoId(Aplicaciones.predioWS.getId());
							t.getGanado().add(gan);
							TrasladosServicio.insertaReubicacion(t);
							TrasladosServicio.updateGanadoFundo(Aplicaciones.predioWS.getId(), gan.getId());
							gan.setPredio(Aplicaciones.predioWS.getId());
							//EcografiasServicio.updateEcoFundo(Aplicaciones.predioWS.getId(), gan.getId());
							showDiio(gan);
						} catch (AppException e) {
							ShowAlert.showAlert("Error", e.getMessage(), Secados.this);
						}
					}
				}
			});
		} else {
			showDiio(gan);
		}
	}
	
	private void verEstadoLeche(Ganado gan){
		try {
			SecadosServicio.traeGanado(gan.getId());
			boolean found = false;
			for (EstadoLeche e : estList){
				if (gan.getEstadoLecheId() != null &&
						e.getId().intValue() == gan.getEstadoLecheId().intValue()){
					
					spEstado.setSelection(gan.getEstadoLecheId().intValue() - 1);
					found = true;
				}
			}
			if (!found){
				spEstado.setAdapter(null);
			}
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), Secados.this);
		}
	}
	
	private void showDiio(Ganado gan){
		this.gan = gan;
		verEstadoLeche(gan);
		updateStatus();
	}
	
	private void checkDiioStatus(Ganado gan){
		if (gan != null){
			clearScreen();
			verReubicacion(gan);
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
		
		try {
			mangadaActual = SecadosServicio.traeMangadaActual();
			if (mangadaActual == null){
				mangadaActual = 0;
				cerrarMangada(false);
			}
		} catch (AppException e) {
			ShowAlert.showAlert("Error", e.getMessage(), this);
		}
		
		mostrarCandidatos();
		
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
						ShowAlert.showAlert("Error", "DIIO no existe", Secados.this);
					}
				} catch (AppException e) {
					ShowAlert.showAlert("Error", e.getMessage(), Secados.this);
				}
    			break;
    		case ConnectedThread.CONNECTION_INTERRUPTED:
    			ShowAlert.askReconnect("Error", "Se perdi� la conexi�n con el bast�n\n�Intentar reconectar?", Secados.this, (BluetoothDevice) msg.obj);
    			break;
    		case ConnectThread.RETRY_CONNECTION:
    			ConnectThread connectThread = new ConnectThread((BluetoothDevice) msg.obj, true);
    			connectThread.start();
    			break;
    		}
    	}
    };

}
