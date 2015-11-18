package servicios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import pojo.Colectivo;
import pojo.TarjetaMagnetica;
import pojo.Viaje;

public class GestionTarjetasTest {

	//tipos de pagos de pasajes
		private static final double MONTO_NORMAL = 5.75;
		private static final double MONTO_MEDIO = 2.9;
		private static final double MONTO_NORMAL_TRASNBORDO = 1.9;
		private static final double MONTO_MEDIO_TRANBORDO = 0.96;
		
		private static TarjetaMagnetica cardRecharge;
		private static TarjetaMagnetica cardWithCharge;
		private static TarjetaMagnetica cardTravels;
		private static TarjetaMagnetica cardPay;
		private static Colectivo colectivo;
		private static Colectivo colectivo2;
		
		@BeforeClass
		public static void onTimeBeforeClass() {
			
			// cardRecharge es para la recargar la tarjeta
			cardRecharge = new TarjetaMagnetica();
			cardRecharge.setMonto(4.25);
			
			// carWithLoad es para preguntar si tiene carga la tarjeta
			cardWithCharge = new TarjetaMagnetica();
			cardWithCharge.setMonto(0.1);
			
			
			// para mirar los viajes de una tarjeta
			Viaje v1= new Viaje();
			Viaje v2= new Viaje();
			Viaje v3= new Viaje();
			Viaje v4= new Viaje();
			
			List<Viaje> viajes= new ArrayList<>();
			viajes.add(v1);
			viajes.add(v2);
			viajes.add(v3);
			viajes.add(v4);
			
			cardTravels = new TarjetaMagnetica();
			cardTravels.setViajes(viajes);

			//para pagar el boleto del viaje  
			cardPay= new TarjetaMagnetica();
			cardPay.setMonto(100.0);
			cardPay.setViajes(new ArrayList<Viaje>());
			
			colectivo= new Colectivo();
			colectivo.setId(1);
			colectivo.setEmpresa("em1");
			colectivo.setLinea("123");
			
			colectivo2= new Colectivo();
			colectivo2.setId(1);
			colectivo2.setEmpresa("em1");
			colectivo2.setLinea("122");
			
		}
		
		
		@Test
		public void testPagarBoleto() {
			//1) viaje normal, sin transbordo 
			cardPay.setMedioBoleto(false);
			cardPay.setMonto(100.0);
			Date hora= new Date();
			boolean pay= GestionTarjetas.pagarBoleto(hora, cardPay, colectivo);
			assertTrue(pay);
			//a) se valida que no se pueda hacer un transbordo en el mismo colectivo
			boolean transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo, hora);
			assertFalse(transbordo);
			//a-1) valido que se halla cobrado un pasaje normal
			Viaje last=cardPay.lastViaje();
			assertEquals(MONTO_NORMAL, last.getMontoPasaje(),0);		
			//b)se valida otro pasaje en otro cole pero despues de pasado 1 hora
			Date horaT= new Date(hora.getTime()+3600001);
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo2, horaT);
			assertFalse(transbordo);
			//b-1) valido que se halla cobrado un pasaje normal
			GestionTarjetas.pagarBoleto(horaT, cardPay, colectivo2);
			last=cardPay.lastViaje();
			assertEquals(MONTO_NORMAL, last.getMontoPasaje(),0);
			
			//2) viaje normal, transbordo
			hora= new Date();
			GestionTarjetas.pagarBoleto(hora, cardPay, colectivo);
			horaT= new Date(hora.getTime()+3600000);
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo2, horaT);
			assertTrue(transbordo);
			//a-1)se valida otro pasaje en otro cole pero antes de pasarse una hora
			GestionTarjetas.pagarBoleto(horaT, cardPay, colectivo2);
			last= cardPay.lastViaje();
			assertEquals(MONTO_NORMAL_TRASNBORDO, last.getMontoPasaje(), 0);
			
			//3) medio, sin transbordo, 
			cardPay.setMedioBoleto(true);
			cardPay.setMonto(100.0);
			Calendar horaHabil= Calendar.getInstance();
			horaHabil.set(Calendar.HOUR_OF_DAY, 6);
			pay= GestionTarjetas.pagarBoleto(horaHabil.getTime(), cardPay, colectivo);
			assertTrue(pay); // primero se valida q se pago un boleto
			//a-1) se valida que no se pueda hacer un transbordo en el mismo colectivo
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo, horaHabil.getTime());
			assertFalse(transbordo);
			//a-b) se valida otro pasaje en otro cole pero despues de pasado 1 hora
			horaT= new Date(horaHabil.getTimeInMillis()+3600001);
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo2, horaT);
			assertFalse(transbordo);
			//a-c) se valida que se cobro el pasaje de medio boleto
			GestionTarjetas.pagarBoleto(horaT, cardPay, colectivo2);
			last= cardPay.lastViaje();
			assertEquals(MONTO_MEDIO, last.getMontoPasaje(), 0);
			
			//4)medio, con transbordo en los horarios de 6 a 11 
			Calendar horaFst= Calendar.getInstance();
			Calendar horaSct= Calendar.getInstance();
			horaFst.set(Calendar.HOUR_OF_DAY, 13);
			horaSct.set(Calendar.HOUR_OF_DAY, 13);
			horaSct.set(Calendar.MINUTE, 59);
			GestionTarjetas.pagarBoleto(horaFst.getTime(), cardPay, colectivo);
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo2, horaSct.getTime());
			assertTrue(transbordo);
			//a-1)se valida otro pasaje en otro cole pero antes de pasarse una hora
			GestionTarjetas.pagarBoleto(horaSct.getTime(), cardPay, colectivo2);
			last= cardPay.lastViaje();
			assertEquals(MONTO_MEDIO_TRANBORDO, last.getMontoPasaje(), 0);
			
			//6) medio boleto pero en horario de madrugada de 00 a 6
			
			Calendar horahelper= Calendar.getInstance();
			horahelper.set(Calendar.DAY_OF_MONTH, 1);
			horaFst= Calendar.getInstance();
			horaSct= Calendar.getInstance();
			horaFst.set(Calendar.DAY_OF_MONTH, 2);
			horaSct.set(Calendar.DAY_OF_MONTH, 2);
			horaFst.set(Calendar.HOUR_OF_DAY, 4);
			horaSct.set(Calendar.HOUR_OF_DAY, 4);
			horaFst.set(Calendar.MINUTE, 05);
			horaSct.set(Calendar.MINUTE, 59);
			
			//a) primero que el medio no se cobre si no que sea uno normal
			GestionTarjetas.pagarBoleto(horahelper.getTime(), cardPay, colectivo2); //cargo uno anterior para que no se produsca un transbordo por error
			GestionTarjetas.pagarBoleto(horaFst.getTime(), cardPay, colectivo);
			last= cardPay.lastViaje();
			assertEquals(MONTO_NORMAL, last.getMontoPasaje(), 0);
			//b) q se haga un transbordo normal
			GestionTarjetas.pagarBoleto(horaSct.getTime(), cardPay, colectivo2);
			last= cardPay.lastViaje();
			assertEquals(MONTO_NORMAL_TRASNBORDO, last.getMontoPasaje(), 0);
			
			//b-2) q no se haga un transbordo si se marca otro pasaje en elmismo cole
			transbordo = GestionTarjetas.transbordo(cardPay.getViajes(), colectivo2, horaSct.getTime());
			assertFalse(transbordo);
			
			//5)finalmente no viaja por falta de carga en tarjeta
			cardPay.setMonto(0.01);
			pay = GestionTarjetas.pagarBoleto(hora, cardPay, colectivo);
			assertFalse(pay);
			
		}

		@Test
		public void testRecargar() 
		{
			//1) recarga sin veneficios
			assertEquals(14.25, GestionTarjetas.recargar(10, cardRecharge), 0);
			//2) recarga con primer veneficio
			cardRecharge.setMonto(0.0);
			assertEquals(231.0, GestionTarjetas.recargar(197, cardRecharge), 0);
			//3)recarga con segundo veneficio
			cardRecharge.setMonto(0.0);
			assertEquals(461.0, GestionTarjetas.recargar(369, cardRecharge), 0);
		}

		@Test
		public void testSaldo() {
			boolean isLoad = false;
			if (GestionTarjetas.saldo(cardWithCharge) > 0) {
				isLoad=true;
			}
			assertTrue(isLoad);
		}

		@Test
		public void testViajesRealizados() {
			boolean haveFourTravels=false;
			if (GestionTarjetas.viajesRealizados(cardTravels).size() == 4) {
				haveFourTravels=true;
			}
			assertTrue(haveFourTravels);
			
		}

}
