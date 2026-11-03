package com.ulpgc.proyectopamn.domain.ports.input

/**
 * Puertos de Entrada (Primary / Inbound Ports)
 *
 * Define las interfaces / contratos de los casos de uso que la aplicación
 * expone hacia los adaptadores de entrada (UI, Notificaciones, etc.).
 */
interface SampleInputPort {
    fun execute()
}
