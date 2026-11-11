package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.User

/** Caso de uso: crear una cuenta nueva y dejar la sesión iniciada. */
interface RegisterInputPort {
    suspend fun execute(name: String, email: String, password: String): Result<User>
}
