package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import com.ulpgc.proyectopamn.domain.ports.input.LoginInputPort

/**
 * Caso de Uso de Login delegando en [AuthInputPort].
 */
class LoginUseCase(
    private val authInputPort: AuthInputPort
) : LoginInputPort {

    override suspend fun execute(identifier: String, password: String): AuthResult<UserProfile> {
        return authInputPort.login(identifier, password)
    }
}
