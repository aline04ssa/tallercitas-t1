Feature: Gestion de citas del taller mecanico

  Scenario: Agendar un mantenimiento ligero de forma exitosa
    Given existe un mecanico disponible para mantenimiento ligero
    When se agenda un mantenimiento ligero para la placa BAS-195
    Then la cita queda en estado PROGRAMADA
    And se notifica el agendamiento

  Scenario: Intentar agendar con un mecanico ocupado a las 11:00
    Given existe un mecanico con una cita programada de 10:00 a 12:00
    When se intenta agendar una cita a las 11:00
    Then el sistema rechaza el agendamiento por horario ocupado

  Scenario: Intentar agendar con un mecanico ocupado a las 12:00
    Given existe un mecanico con una cita programada de 10:00 a 12:00
    When se intenta agendar una cita a las 12:00
    Then la cita queda en estado PROGRAMADA