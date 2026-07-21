package edu.pe.cibertec.taller.bdd;

import static org.mockito.Mockito.mock;

import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;

	private Cita resultado;
	private Exception excepcion;
	private final String placa = "BAS-195";
	private final LocalDateTime ahora = LocalDateTime.of(2026,9,14,8,0);
	private Mecanico mecanico;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
	}

	@Given("existe un mecanico disponible para mantenimiento ligero")
	public void existeUnMecanicoDisponible() {
		mecanico = new Mecanico(2L, "Fiorella Basurto", TipoServicio.MANTENIMIENTO_LIGERO);
		when(repositorioMecanicos.findById(2L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(ahora);
		when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA)).thenReturn(List.of());
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(i->i.getArgument(0));
	}

	@When("se agenda un mantenimiento ligero para la placa BAS-195")
	public void agendarMantenimientoLigero() {
		resultado = servicioCitas.agendarCita(2L, placa, TipoServicio.MANTENIMIENTO_LIGERO, LocalDateTime.of(2026,9,15,10,0));
	}

	@Then("la cita queda en estado PROGRAMADA")
	public void verificarEstado() {
		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@And("se notifica el agendamiento")
	public void notificarAgendamiento() {
		verify(servicioNotificaciones).notificarCitaAgendada(any(Cita.class));
	}

	@Given("existe un mecanico con una cita programada de 10:00 a 12:00")
	public void mecanicoOcupado() {
		mecanico = new Mecanico(1L, "Fiorella Basurto", TipoServicio.MANTENIMIENTO_LIGERO);
		when(repositorioMecanicos.findById(1L)).thenReturn(Optional.of(mecanico));
		when(proveedorFechaHora.ahora()).thenReturn(ahora);
		Cita existente = new Cita();
		existente.setFechaHoraInicio(LocalDateTime.of(2026,9,15,10,0));
		existente.setDuracionHoras(2);
		existente.setEstado(EstadoCita.PROGRAMADA);
		when(repositorioCitas.findByMecanicoIdAndEstado(1L, EstadoCita.PROGRAMADA)).thenReturn(List.of(existente));
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(i->i.getArgument(0));
	}

	@When("se intenta agendar una cita a las 11:00")
	public void intentarAgendarALas11() {
		excepcion = assertThrows(HorarioOcupadoException.class, () -> servicioCitas.agendarCita(1L, placa, TipoServicio.MANTENIMIENTO_LIGERO, LocalDateTime.of(2026,9,15,11,0)));
	}

	@Then("el sistema rechaza el agendamiento por horario ocupado")
	public void verificarHorarioOcupado(){
		assertNotNull(excepcion);
	}

	@When("se intenta agendar una cita a las 12:00")
	public void intentarAgendarALas12(){
		resultado=servicioCitas.agendarCita(1L, placa, TipoServicio.MANTENIMIENTO_LIGERO, LocalDateTime.of(2026,9,15,12,0));
	}

}
