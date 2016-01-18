package cl.a2r.sip.model;

import java.io.Serializable;
import java.util.Date;

public class InyeccionTB implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer usuarioId;
	private Integer ganadoID;
	private Integer ganadoDiio;
	private Integer tuboPPDId;
	private Integer tuboPPDSerie;
	private Integer instancia;
	private Integer fundoId;
	private Date fecha_dosis;
	private String sincronizado;
	
	public Integer getGanadoID() {
		return ganadoID;
	}
	
	public void setGanadoID(Integer ganadoID) {
		this.ganadoID = ganadoID;
	}
	
	public Integer getGanadoDiio() {
		return ganadoDiio;
	}
	
	public void setGanadoDiio(Integer ganadoDiio) {
		this.ganadoDiio = ganadoDiio;
	}

	public Integer getTuboPPDId() {
		return tuboPPDId;
	}

	public void setTuboPPDId(Integer tuboPPDId) {
		this.tuboPPDId = tuboPPDId;
	}

	public Integer getTuboPPDSerie() {
		return tuboPPDSerie;
	}

	public void setTuboPPDSerie(Integer tuboPPDSerie) {
		this.tuboPPDSerie = tuboPPDSerie;
	}

	public Integer getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Integer usuarioId) {
		this.usuarioId = usuarioId;
	}

	public Date getFecha_dosis() {
		return fecha_dosis;
	}

	public void setFecha_dosis(Date fecha_dosis) {
		this.fecha_dosis = fecha_dosis;
	}

	public Integer getInstancia() {
		return instancia;
	}

	public void setInstancia(Integer instancia) {
		this.instancia = instancia;
	}

	public String getSincronizado() {
		return sincronizado;
	}

	public void setSincronizado(String sincronizado) {
		this.sincronizado = sincronizado;
	}

	public Integer getFundoId() {
		return fundoId;
	}

	public void setFundoId(Integer fundoId) {
		this.fundoId = fundoId;
	}
	
	public String toString(){
		return Integer.toString(this.ganadoDiio);
	}

	
}
