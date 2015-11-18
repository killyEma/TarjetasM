package pojo;

import java.util.Date;

public class Viaje {

	private int id;
	private Date fechahora;
	private Colectivo colectivo;
	private double montoPasaje;
	
	public Colectivo getColectivo() {
		return colectivo;
	}
	public void setColectivo(Colectivo colectivo) {
		this.colectivo = colectivo;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getFechahora() {
		return fechahora;
	}
	public void setFechahora(Date fechahora) {
		this.fechahora = fechahora;
	}
	public double getMontoPasaje() {
		return montoPasaje;
	}
	public void setMontoPasaje(double montoPasaje) {
		this.montoPasaje = montoPasaje;
	}
	
	
}
