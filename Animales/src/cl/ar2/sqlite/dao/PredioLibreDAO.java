package cl.ar2.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import cl.a2r.sip.model.Brucelosis;
import cl.a2r.sip.model.Ganado;
import cl.a2r.sip.model.InyeccionTB;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;

public class PredioLibreDAO {
	
	private static final String SQL_DELETE_DIIO = ""
			+ "DELETE FROM diio";
	
	private static final String SQL_INSERTA_DIIO = ""
			+ "INSERT INTO diio (id, diio, eid, fundoId) VALUES (?, ?, ?, ?)";
	
	private static final String SQL_SELECT_DIIO = ""
			+ "SELECT id, diio, eid, fundoId FROM diio WHERE diio = ?";
	
	private static final String SQL_SELECT_EID = ""
			+ "SELECT id, diio, eid, fundoId FROM diio WHERE eid = ?";
	
	private static final String SQL_ALL = ""
			+ "SELECT id, diio, eid, fundoId FROM diio";
	
	private static final String SQL_SELECT_DIIO_FUNDO = ""
			+ "SELECT id, diio, eid, fundoId FROM diio WHERE fundoId = ?";
	
	private static final String SQL_INSERTA_GANADO_PL = ""
			+ "INSERT INTO predio_libre (ganadoId, fundoId, instancia, ganadoDiio, tuboPPDId, sincronizado) VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String SQL_SELECT_GANADO_PL = ""
			+ "SELECT ganadoId, fundoId, instancia, ganadoDiio, tuboPPDId, lecturaTB, sincronizado FROM predio_libre";
	
	private static final String SQL_EXISTS_GANADO_PL = ""
			+ "SELECT ganadoId, fundoId, instancia, ganadoDiio, tuboPPDId, sincronizado FROM predio_libre WHERE ganadoId = ?";
	
	private static final String SQL_DELETE_SINCRONIZADO = ""
			+ " DELETE FROM predio_libre WHERE sincronizado = ?";
	
	private static final String SQL_DELETE_PL = ""
			+ " DELETE FROM predio_libre";
	
	private static final String SQL_INSERT_GANADO_PL_BRUCELOSIS = ""
			+ "INSERT INTO predio_libre_brucelosis "
			+ " (ganadoId, fundoId, instancia, ganadoDiio, codBarra, sincronizado) "
			+ " VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String SQL_SELECT_GANADO_PL_BRUCELOSIS = ""
			+ "SELECT ganadoId, fundoId, instancia, ganadoDiio, codBarra, sincronizado "
			+ " FROM predio_libre_brucelosis";

	private static final String SQL_EXISTS_GANADO_PL_BRUCELOSIS = ""
			+ "SELECT ganadoId, fundoId, instancia, ganadoDiio, codBarra, sincronizado "
			+ " FROM predio_libre_brucelosis "
			+ " WHERE ganadoId = ?";
	
	private static final String SQL_EXISTS_COD_BARRA = ""
			+ "SELECT codBarra "
			+ " FROM predio_libre_brucelosis "
			+ " WHERE codBarra = ?";
	
	private static final String SQL_DELETE_PL_BRUCELOSIS = ""
			+ " DELETE FROM predio_libre_brucelosis";
	
	private static final String SQL_INSERT_LECTURA_TUBERCULOSIS = ""
			+ " UPDATE predio_libre"
			+ " SET lecturaTB = ?"
			+ " WHERE ganadoId = ?";
	
    public static void deleteDiio(SqLiteTrx trx) throws SQLException {
    	SQLiteStatement statement = trx.getDB().compileStatement(SQL_DELETE_DIIO);
    	statement.clearBindings();
    	statement.executeUpdateDelete();
    }
    
    public static void insertaDiio(SqLiteTrx trx, List<Ganado> list) throws SQLException {

        SQLiteStatement statement = trx.getDB().compileStatement(SQL_INSERTA_DIIO);
        
        for (Ganado g : list){
        	statement.clearBindings();
        	statement.bindLong(1, g.getId());
        	statement.bindLong(2, g.getDiio());
        	statement.bindString(3, g.getEid());
        	statement.bindLong(4, g.getPredio());
        	statement.executeInsert();
        }
    }
    
    public static Ganado selectDiio(SqLiteTrx trx, Integer diio) throws SQLException {
        Ganado g = null;
        boolean hayReg;
        
        String[] args = {Integer.toString(diio)};
        Cursor c = trx.getDB().rawQuery(SQL_SELECT_DIIO, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {

        	g = new Ganado();
        	g.setId(c.getInt(c.getColumnIndex("id")));
        	g.setDiio(c.getInt(c.getColumnIndex("diio")));
        	g.setEid(c.getString(c.getColumnIndex("eid")));
        	g.setPredio(c.getInt(c.getColumnIndex("fundoId")));
            hayReg = c.moveToNext();
        }

        return g;
    }
    
    public static Ganado selectEID(SqLiteTrx trx, String eid) throws SQLException {
        Ganado g = null;
        boolean hayReg;

        String[] args = {eid};
        Cursor c = trx.getDB().rawQuery(SQL_SELECT_EID, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {

        	g = new Ganado();
        	g.setId(c.getInt(c.getColumnIndex("id")));
        	g.setDiio(c.getInt(c.getColumnIndex("diio")));
        	g.setEid(c.getString(c.getColumnIndex("eid")));
        	g.setPredio(c.getInt(c.getColumnIndex("fundoId")));
            hayReg = c.moveToNext();
        }

        return g;
    }
    
    public static List selectAll(SqLiteTrx trx) throws SQLException {
        List list = new ArrayList();
        boolean hayReg;

        Cursor c = trx.getDB().rawQuery(SQL_ALL, null);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	
        	Ganado g = new Ganado();
        	g.setId(c.getInt(c.getColumnIndex("id")));
        	g.setDiio(c.getInt(c.getColumnIndex("diio")));
        	g.setEid(c.getString(c.getColumnIndex("eid")));
        	g.setPredio(c.getInt(c.getColumnIndex("fundoId")));
        	list.add(g);
            hayReg = c.moveToNext();
        }

        return list;
    }
    
    public static void insertaGanadoPL(SqLiteTrx trx, InyeccionTB tb) throws SQLException {

        SQLiteStatement statement = trx.getDB().compileStatement(SQL_INSERTA_GANADO_PL);
        
    	statement.clearBindings();
    	statement.bindLong(1, tb.getGanadoID());
    	statement.bindLong(2, tb.getFundoId());
    	statement.bindLong(3, tb.getInstancia());
    	statement.bindLong(4, tb.getGanadoDiio());
    	statement.bindLong(5, tb.getTuboPPDId());
    	statement.bindString(6, tb.getSincronizado());
    	statement.executeInsert();
    }
    
    public static List selectGanadoPL(SqLiteTrx trx) throws SQLException {
        List list = new ArrayList();
        boolean hayReg;

        Cursor c = trx.getDB().rawQuery(SQL_SELECT_GANADO_PL, null);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	InyeccionTB i = new InyeccionTB();
        	i.setGanadoID(c.getInt(c.getColumnIndex("ganadoId")));
        	i.setFundoId(c.getInt(c.getColumnIndex("fundoId")));
        	i.setInstancia(c.getInt(c.getColumnIndex("instancia")));
        	i.setGanadoDiio(c.getInt(c.getColumnIndex("ganadoDiio")));
        	i.setTuboPPDId(c.getInt(c.getColumnIndex("tuboPPDId")));
        	i.setLecturaTB(c.getString(c.getColumnIndex("lecturaTB")));
        	i.setSincronizado(c.getString(c.getColumnIndex("sincronizado")));
        	list.add(i);
            hayReg = c.moveToNext();
        }

        return list;
    }
    
    public static boolean existsGanadoPL(SqLiteTrx trx, Integer ganadoId) throws SQLException {
        boolean exists = false;
        boolean hayReg;

        String[] args = {Integer.toString(ganadoId)};
        Cursor c = trx.getDB().rawQuery(SQL_EXISTS_GANADO_PL, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	exists = true;
            hayReg = c.moveToNext();
        }

        return exists;
    }
    
    public static void deletePL(SqLiteTrx trx) throws SQLException {
    	SQLiteStatement statement = trx.getDB().compileStatement(SQL_DELETE_PL);
    	statement.clearBindings();
    	statement.executeUpdateDelete();
    }
    
    public static List selectDiioFundo(SqLiteTrx trx, Integer g_fundo_id) throws SQLException {
        List list = new ArrayList();
        boolean hayReg;

        String[] args = {Integer.toString(g_fundo_id)};
        Cursor c = trx.getDB().rawQuery(SQL_SELECT_DIIO_FUNDO, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	Ganado g = new Ganado();
        	g.setId(c.getInt(c.getColumnIndex("id")));
        	g.setDiio(c.getInt(c.getColumnIndex("diio")));
        	g.setEid(c.getString(c.getColumnIndex("eid")));
        	g.setPredio(c.getInt(c.getColumnIndex("fundoId")));
        	list.add(g);
            hayReg = c.moveToNext();
        }

        return list;
    }
    
    public static void insertaGanadoPLBrucelosis(SqLiteTrx trx, Brucelosis b) throws SQLException {

        SQLiteStatement statement = trx.getDB().compileStatement(SQL_INSERT_GANADO_PL_BRUCELOSIS);
        
    	statement.clearBindings();
    	statement.bindLong(1, b.getGanado().getId());
    	statement.bindLong(2, b.getGanado().getPredio());
    	statement.bindLong(3, b.getInstancia());
    	statement.bindLong(4, b.getGanado().getDiio());
    	statement.bindString(5, b.getCodBarra());
    	statement.bindString(6, b.getSincronizado());
    	statement.executeInsert();
    }
    
    public static List selectGanadoPLBrucelosis(SqLiteTrx trx) throws SQLException {
        List list = new ArrayList();
        boolean hayReg;

        Cursor c = trx.getDB().rawQuery(SQL_SELECT_GANADO_PL_BRUCELOSIS, null);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	Brucelosis b = new Brucelosis();
        	b.getGanado().setId(c.getInt(c.getColumnIndex("ganadoId")));
        	b.getGanado().setPredio(c.getInt(c.getColumnIndex("fundoId")));
        	b.setInstancia(c.getInt(c.getColumnIndex("instancia")));
        	b.getGanado().setDiio(c.getInt(c.getColumnIndex("ganadoDiio")));
        	b.setCodBarra(c.getString(c.getColumnIndex("codBarra")));
        	b.setSincronizado(c.getString(c.getColumnIndex("sincronizado")));
        	list.add(b);
            hayReg = c.moveToNext();
        }

        return list;
    }
    
    public static boolean existsGanadoPLBrucelosis(SqLiteTrx trx, Integer ganadoId) throws SQLException {
        boolean exists = false;
        boolean hayReg;

        String[] args = {Integer.toString(ganadoId)};
        Cursor c = trx.getDB().rawQuery(SQL_EXISTS_GANADO_PL_BRUCELOSIS, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	exists = true;
            hayReg = c.moveToNext();
        }

        return exists;
    }
    
    public static boolean existsCodigoBarra(SqLiteTrx trx, String codBarra) throws SQLException {
        boolean exists = false;
        boolean hayReg;

        String[] args = {codBarra};
        Cursor c = trx.getDB().rawQuery(SQL_EXISTS_COD_BARRA, args);
        hayReg = c.moveToFirst();
        while ( hayReg ) {
        	exists = true;
            hayReg = c.moveToNext();
        }

        return exists;
    }
    
    public static void deletePLBrucelosis(SqLiteTrx trx) throws SQLException {
    	SQLiteStatement statement = trx.getDB().compileStatement(SQL_DELETE_PL_BRUCELOSIS);
    	statement.clearBindings();
    	statement.executeUpdateDelete();
    }
    
    public static void insertLecturaTuberculosis(SqLiteTrx trx, String lecturaTB, Integer ganadoId) throws SQLException {
    	SQLiteStatement statement = trx.getDB().compileStatement(SQL_INSERT_LECTURA_TUBERCULOSIS);
    	statement.clearBindings();
    	statement.bindString(1, lecturaTB);
    	statement.bindLong(2, ganadoId);
    	statement.executeUpdateDelete();
    }

}