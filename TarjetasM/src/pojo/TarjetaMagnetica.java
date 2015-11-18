package pojo;

import java.util.List;

public class TarjetaMagnetica {
	
	private int id_tarjeta;
	private Double monto;
	private List<Viaje> viajes;
	private boolean medioBoleto;
	

	public int getId_tarjeta() {
		return id_tarjeta;
	}
	public void setId_tarjeta(int id_tarjeta) {
		this.id_tarjeta = id_tarjeta;
	}
	
	public List<Viaje> getViajes() {
		return viajes;
	}
	public void setViajes(List<Viaje> viajes) {
		this.viajes = viajes;
	}
	public boolean isMedioBoleto() {
		return medioBoleto;
	}
	public void setMedioBoleto(boolean medioBoleto) {
		this.medioBoleto = medioBoleto;
	}
	public Double getMonto() {
		return monto;
	}
	public void setMonto(Double monto) {
		this.monto = monto;
	}
	
	
	/**
	 * return the last travel and null if doesn't have any travel 
	 * @return viajes last travel
	 */
	public Viaje lastViaje(){
		if (!viajes.isEmpty() && viajes !=null) {
			return viajes.get(viajes.size()-1);
		}
		return null;
	}
	
}
