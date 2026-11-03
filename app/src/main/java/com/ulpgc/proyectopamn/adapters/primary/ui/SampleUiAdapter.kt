package com.ulpgc.proyectopamn.adapters.primary.ui

import com.ulpgc.proyectopamn.domain.ports.input.SampleInputPort

/**
 * Adaptadores de Entrada (Primary / Driving Adapters) - UI
 *
 * Se encarga de la presentación de datos e interacción con el usuario.
 * Invoca las acciones a través de los Puertos de Entrada.
 */
class SampleUiAdapter(
    private val inputPort: SampleInputPort
) {
    fun onUserAction() {
        inputPort.execute()
    }
}
