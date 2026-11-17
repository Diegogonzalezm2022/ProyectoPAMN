package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import com.ulpgc.proyectopamn.domain.ports.input.RegisterInputPort

/**
 * Caso de Uso de Registro delegando en [AuthInputPort].
 */
class RegisterUseCase(
    private val authInputPort: AuthInputPort
) : RegisterInputPort {

    override suspend fun execute(user: UserProfile, password: String): AuthResult<UserProfile> {
        return authInputPort.register(user, password)
    }
}
