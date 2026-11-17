package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile

/** Puerto de Entrada para Inicio de Sesión */
interface LoginInputPort {
    suspend fun execute(identifier: String, password: String): AuthResult<UserProfile>
}
