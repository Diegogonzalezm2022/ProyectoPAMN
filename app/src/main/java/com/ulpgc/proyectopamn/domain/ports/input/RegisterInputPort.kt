package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile

/** Puerto de Entrada para Registro de Cuenta */
interface RegisterInputPort {
    suspend fun execute(user: UserProfile, password: String): AuthResult<UserProfile>
}
