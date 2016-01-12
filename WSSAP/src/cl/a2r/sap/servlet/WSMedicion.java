package cl.a2r.sap.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.a2r.common.AppException;
import cl.a2r.common.wsutils.ParamServlet;
import cl.a2r.sap.common.AppLog;
import cl.a2r.sap.model.Calificacion;
import cl.a2r.sap.model.Medicion;
import cl.a2r.sap.service.AutorizacionService;
import cl.a2r.sap.service.MedicionService;

public class WSMedicion extends HttpServlet{

    private static final long serialVersionUID = 1L;
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Object retorno = null;

        ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());
        ParamServlet params = null;

        try {
            params = (ParamServlet) inputFromApplet.readObject();

            String servicio = (String) params.getParam("servicio");
            if (servicio.equals("insertaMedicion") ) {
            	List<Medicion> medList = (List<Medicion>) params.getParam("medList");
            	String correo = (String) params.getParam("correo");
            	Integer g_usuario_id = AutorizacionService.validaUsuario(correo);
            	for (Medicion m : medList){
                    m.setUsuarioId(g_usuario_id);
                    MedicionService.insertaMedicion(m);
            	}
            	retorno = g_usuario_id;
            } else if (servicio.equals("traeStock")){
            	List<Medicion> list = MedicionService.traeStock();
            	retorno = list;
            } else if (servicio.equals("insertaCalificacion")){
            	List<Calificacion> calList = (List<Calificacion>) params.getParam("calList");
            	String correo = (String) params.getParam("correo");
            	Integer g_usuario_id = AutorizacionService.validaUsuario(correo);
            	MedicionService.insertaCalificacion(calList, g_usuario_id);
            	Integer i = 1;
            	retorno = i;
            } else if (servicio.equals("traeCalificacion")){
            	List<Calificacion> list = MedicionService.traeCalificacion();
            	retorno = list;
            } else {
                retorno = new AppException("Servicio no válido.", null);
            }
        } catch (ClassNotFoundException ex) {
            retorno = new AppException("Error en parametros (ParamServlet).", ex);
            AppLog.logSevere(((AppException)retorno).getMessage(), ex);
        } catch (AppException ex) {
            AppLog.logSevere(ex.getMessage(), ex);
            retorno = ex;
        }

        response.setContentType("application/x-java-serialized-object");
        ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
        oos.writeObject(retorno);
        oos.flush();
        oos.close();
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
	
}

