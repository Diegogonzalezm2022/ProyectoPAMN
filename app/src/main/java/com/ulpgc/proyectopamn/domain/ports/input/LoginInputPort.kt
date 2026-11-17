package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.User

/** Caso de uso: iniciar sesión con email y contraseña. */
interface LoginInputPort {
    suspend fun execute(email: String, password: String): Result<User>
}
