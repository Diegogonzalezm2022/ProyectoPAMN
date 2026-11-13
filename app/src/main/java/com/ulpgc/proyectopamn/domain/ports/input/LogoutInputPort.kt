package com.ulpgc.proyectopamn.domain.ports.input

/** Caso de uso: cerrar la sesión actual. */
interface LogoutInputPort {
    suspend fun execute()
}
