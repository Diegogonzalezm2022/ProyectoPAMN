package com.ulpgc.proyectopamn.adapters.secondary.persistence

import com.ulpgc.proyectopamn.domain.ports.output.SampleOutputPort

/**
 * Adaptadores de Salida (Secondary / Driven Adapters) - Persistencia
 *
 * Implementación concreta del puerto de salida para la persistencia local de datos
 * (Base de datos Room, SQLite, SharedPreferences, etc.).
 */
class SamplePersistenceAdapter : SampleOutputPort {

    override fun saveData(data: String) {
        // Implementación de guardado local
    }
}
