package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.ports.input.SampleInputPort
import com.ulpgc.proyectopamn.domain.ports.output.SampleOutputPort

/**
 * Capa de Aplicación - Casos de Uso
 *
 * Implementa la lógica de aplicación/negocio orquestando los modelos de dominio
 * y los puertos de salida. Implementa los puertos de entrada (Input Ports).
 */
class SampleUseCase(
    private val outputPort: SampleOutputPort
) : SampleInputPort {

    override fun execute() {
        // Lógica del caso de uso
        outputPort.saveData("Sample")
    }
}
