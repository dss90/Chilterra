package cl.a2r.sip.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.a2r.common.AppException;
import cl.a2r.common.wsservice.WSAdempiereCliente;
import cl.a2r.common.wsutils.ParamServlet;
import cl.a2r.sip.common.AppLog;
import cl.a2r.sip.model.DctoAdem;
import cl.a2r.sip.model.FMA;
import cl.a2r.sip.model.Instancia;
import cl.a2r.sip.model.Traslado;
import cl.a2r.sip.service.TrasladosService;

public class WSTraslados extends HttpServlet {

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
        	Integer l = 1;
            params = (ParamServlet) inputFromApplet.readObject();
            String servicio = (String) params.getParam("servicio");

            if (servicio.equals("traeTransportistas")){
            	List list = TrasladosService.traeTransportistas();
            	retorno = list;
            } else if (servicio.equals("traeChofer")){
            	Integer transportistaId = (Integer) params.getParam("transportistaId");
            	List list = TrasladosService.traeChofer(transportistaId);
            	retorno = list;
            } else if (servicio.equals("traeCamion")){
            	Integer transportistaId = (Integer) params.getParam("transportistaId");
            	List list = TrasladosService.traeCamion(transportistaId);
            	retorno = list;
            } else if (servicio.equals("traeAcoplado")){
            	Integer transportistaId = (Integer) params.getParam("transportistaId");
            	List list = TrasladosService.traeAcoplado(transportistaId);
            	retorno = list;
            } else if (servicio.equals("traeArrieros")){
            	List list = TrasladosService.traeArrieros();
            	retorno = list;
            } else if (servicio.equals("insertaMovimiento")){
            	Traslado traslado = (Traslado) params.getParam("traslado");
            	Integer g_movimiento_id = TrasladosService.insertaMovimiento(traslado);
            	DctoAdem d = TrasladosService.insertaMovtoAdem(g_movimiento_id);
            	retorno = d;
            	WSAdempiereCliente.completaDocto(d.getIddocto(), d.getIdtipodocto());
            } else if (servicio.equals("generaXMLTraslado")){
            	FMA fma = (FMA) params.getParam("fma");
            	TrasladosService.generaXMLTraslado(fma);
            	Integer i = 1;
            	retorno = i;
            } else if (servicio.equals("traeMovimientosEP")){
            	List list = TrasladosService.traeMovimientosEP();
            	retorno = list;
            } else if (servicio.equals("traeMovimiento")){
            	Integer nro_documento = (Integer) params.getParam("nro_documento");
            	Traslado t = TrasladosService.traeMovimiento(nro_documento);
            	retorno = t;
            } else if (servicio.equals("insertaMovtoConfirm")){
            	Traslado traslado = (Traslado) params.getParam("traslado");
            	TrasladosService.insertaMovtoConfirm(traslado);
            	Integer i = 1;
            	retorno = i;
            } else if (servicio.equals("insertaMovtoReubicacion")){
            	Traslado traslado = (Traslado) params.getParam("traslado");
            	Integer g_movimiento_id = TrasladosService.insertaMovtoReubicacion(traslado);
            	traslado.setG_movimiento_id(g_movimiento_id);
            	TrasladosService.insertaMovtoConfirm(traslado);
            	Integer i = 1;
            	retorno = i;
            } else if (servicio.equals("reubicaGanado")){
            	Traslado traslado = (Traslado) params.getParam("traslado");
            	TrasladosService.reubicaGanado(traslado);
            	Integer i = 1;
            	retorno = i;
                //--------------------- TRASLADOS V2 ----------------------------
            } else if (servicio.equals("traeTraslados")){
            	Integer fundoId = (Integer) params.getParam("fundoId");
            	List list = TrasladosService.traeTraslados(fundoId);
            	retorno = list;
            } else if (servicio.equals("borrarTraslado")){
            	Instancia instancia = (Instancia) params.getParam("instancia");
            	TrasladosService.borrarTraslado(instancia);
            	Integer i = 1;
            	retorno = i;
            } else if (servicio.equals("traeChoferV2")){
            	List list = TrasladosService.traeChofer();
            	retorno = list;
            } else if (servicio.equals("traeCamionV2")){
            	List list = TrasladosService.traeCamion();
            	retorno = list;
            } else if (servicio.equals("traeAcopladoV2")){
            	List list = TrasladosService.traeAcoplado();
            	retorno = list;
            } else if (servicio.equals("insertaTraslado")){
            	Instancia superInstancia = (Instancia) params.getParam("superInstancia");
            	Integer g_procedimiento_instancia_movto_salida_id = TrasladosService.insertaTraslado(superInstancia);
            	DctoAdem d = TrasladosService.insertaMovtoAdemV2(g_procedimiento_instancia_movto_salida_id);
            	WSAdempiereCliente.completaDocto(d.getIddocto(), d.getIdtipodocto());
            	FMA fma = new FMA();
            	fma.setUsuarioId(superInstancia.getUsuarioId());
            	fma.setG_movimiento_id(g_procedimiento_instancia_movto_salida_id);
            	fma.setFundoOrigenId(superInstancia.getInstancia().getTraslado().getOrigen().getId());
            	fma.setFundoDestinoId(superInstancia.getInstancia().getTraslado().getDestino().getId());
            	TrasladosService.generarFmaXml(fma);
            	retorno = l;
            } else if (servicio.equals("traeTraslado")){
            	Integer g_superprocedimiento_instancia_id = (Integer) params.getParam("superInstanciaId");
            	List list = TrasladosService.traeTraslado(g_superprocedimiento_instancia_id);
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
