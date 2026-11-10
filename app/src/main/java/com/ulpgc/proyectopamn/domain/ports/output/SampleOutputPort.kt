package com.ulpgc.proyectopamn.domain.ports.output

/**
 * Puertos de Salida (Secondary / Outbound Ports)
 *
 * Define las interfaces / contratos para acceder a servicios externos
 * o recursos del sistema (repositorios de datos, clientes API, sensores, etc.).
 */
interface SampleOutputPort {
    fun saveData(data: String)
}
