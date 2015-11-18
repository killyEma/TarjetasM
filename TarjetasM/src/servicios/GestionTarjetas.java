package servicios;



import java.util.Calendar;
import java.util.Date;
import java.util.List;






import pojo.Colectivo;
import pojo.TarjetaMagnetica;
import pojo.Viaje;

public class GestionTarjetas {
	
	//tipos de pagos de pasajes
	private static final double MONTO_NORMAL = 5.75;
	private static final double MONTO_MEDIO = 2.9;
	private static final double MONTO_NORMAL_TRASNBORDO = 1.9;
	private static final double MONTO_MEDIO_TRANBORDO = 0.96;
	
	// limites para cuando se hace una recarga de la tarjeta, con el fin de agregar o no, al abono
	private static final int LIMITE_SIN_VENEFICIOS= 196;
	private static final int LIMITE_CON_VENEFICIOS= 368;
	private static final int PRIMER_VENEFICIO= 34;
	private static final int SEGUNDO_VENEFICIO= 92;
	
	/**
	 * pagar un boletoya sea medio boleto, normal, o transbordo
	 * @param horario hora donde se desea pagar el boleto
	 * @param tarjeta es a la que se le desea cobrar el boleto
	 * @param colectivo es el que se desea pagar el boleto
	 * @return si se pudo gestionar el pago el boleto o no
	 */
	public static boolean pagarBoleto(Date horario,TarjetaMagnetica tarjeta,Colectivo colectivo )
	{
		boolean horarioMadrgada=validarQueSeaDeMadrugada(horario);
		if (horarioMadrgada) 
		{// significa que no importa que sea medio boleto, se le cobrara como un pasaje normal o transbordo normal
			
			//normal y transbordo
			if(tarjeta.getMonto() >= MONTO_NORMAL_TRASNBORDO && transbordo(tarjeta.getViajes(),colectivo,horario))
			{ 
				tarjeta=pagar(tarjeta, MONTO_NORMAL_TRASNBORDO, colectivo, horario);
				return true;
			}
			// normal
			else if(tarjeta.getMonto() >= MONTO_NORMAL)
			{ 
				tarjeta=pagar(tarjeta,MONTO_NORMAL,colectivo,horario);
				
				return true;
			}
			
		}
		else
		{// no es de madrugada y hay que ver si es un pasaje normal o medio boleto
			boolean esMedio=tarjeta.isMedioBoleto();
			//medio boleto
			if (esMedio) {
				//transbordo:
				if (tarjeta.getMonto() >= MONTO_MEDIO_TRANBORDO && transbordo(tarjeta.getViajes(),colectivo,horario)) 
				{
					tarjeta=pagar(tarjeta, MONTO_MEDIO_TRANBORDO, colectivo,horario);
					return true;
					
				}
				//medio boleto normal
				else if (tarjeta.getMonto() >= MONTO_MEDIO) 
				{ 
					tarjeta=pagar(tarjeta, MONTO_MEDIO, colectivo, horario);
					return true;
				}
				return false;
			}
			// transbordo normal
			else if(tarjeta.getMonto() >= MONTO_NORMAL_TRASNBORDO && transbordo(tarjeta.getViajes(),colectivo,horario))
			{ 
				tarjeta=pagar(tarjeta, MONTO_NORMAL_TRASNBORDO, colectivo, horario);
				return true;
			}
			// pasaje normal
			else if(tarjeta.getMonto() >= MONTO_NORMAL)
			{ 
				tarjeta=pagar(tarjeta,MONTO_NORMAL,colectivo,horario);
				
				return true;
			}
		}
		
		return false;
	}
	
	

	public static boolean validarQueSeaDeMadrugada(Date horario) {
		Calendar dia= Calendar.getInstance();
		dia.setTime(horario);
		int hora=dia.get(Calendar.HOUR_OF_DAY);
		if (hora >= 0 && hora < 6) {
			return true;
		}
		return false;
	}
	
	/**
	 * recarga la tarjeta con el monto ingresdo 
	 * 
	 * @param monto
	 * @param tarjeta
	 * @return
	 */
	public static Double recargar(int monto,TarjetaMagnetica tarjeta)
	{
		if (monto < LIMITE_SIN_VENEFICIOS) 
		{
			tarjeta.setMonto(tarjeta.getMonto()+monto);
		}
		else if (monto >= LIMITE_SIN_VENEFICIOS && monto < LIMITE_CON_VENEFICIOS)
		{
			tarjeta.setMonto(tarjeta.getMonto() + monto + PRIMER_VENEFICIO );
		}
		else if(monto >= LIMITE_CON_VENEFICIOS )
		{
			tarjeta.setMonto(tarjeta.getMonto() + monto + SEGUNDO_VENEFICIO);
		}
		
		
		return tarjeta.getMonto();
	}
	
	/**
	 * retorna el saldo de la tarjeta
	 * @param tarjeta a la que se quiere conocer el saldo
	 * @return Double el saldo de la tarjeta
	 */
	public static Double saldo(TarjetaMagnetica tarjeta){
		return tarjeta.getMonto();
	}
	
	
	/**
	 * retorna la lista de los lo viajes realizados por la tarjeta
	 * @param tarjeta a la que se le quiere conocer los viejes realizados
	 * @return List<Viajes> lista de viajes, devuelve nulo si no encuentra
	 */
	public static List<Viaje> viajesRealizados(TarjetaMagnetica tarjeta)
	{
		return tarjeta.getViajes();
	}
	
	
	/**
	 * validacion necesaria para saber si el vije sera de transbordo 
	 * @param viajes necesario para saber los viajes anterios
	 * @param colectivo al que se desea marcar el trandsbordo
	 * @param horario hora cuando se desea marcar el trasbordo 
	 * @return 
	 */
	public static boolean transbordo(List<Viaje> viajes, Colectivo colectivo, Date horario)
	{
		if (viajes!= null && !viajes.isEmpty()) 
		{
			Viaje viajeUltimo=viajes.get(viajes.size()-1);
			//valido q sea distinto cole
			if (viajeUltimo.getColectivo().getLinea() != colectivo.getLinea()) 
			{
				// valido q el transbordo este dentro de la hora, milisegundos
				if (horario.getTime() - viajeUltimo.getFechahora().getTime() <= 3600000 ) 
				{
					return true;
				}
			}	
		}
		return false;
	}
	
	
	/**
	 * realiza el pago del boleto a la tarjeta
	 * @param tarjeta es la que se le cobrara el boleto
	 * @param peaje cantidad que se le cobrara al boleto
	 * @param cole colectivo al que se registrara junto con el pago
	 * @param horario hora exacta en la q se hace el registro de pago
	 * @return 
	 */
	private static TarjetaMagnetica pagar(TarjetaMagnetica tarjeta, double peaje,Colectivo cole,Date horario) 
	{
		tarjeta.setMonto(tarjeta.getMonto() - peaje);
		Viaje viaje= new Viaje();
		viaje.setColectivo(cole);
		viaje.setFechahora(horario);
		viaje.setMontoPasaje(peaje);
		tarjeta.getViajes().add(viaje);
		return tarjeta;
	}
}
