package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.AuthError
import com.ulpgc.proyectopamn.domain.model.CredentialsValidator
import com.ulpgc.proyectopamn.domain.model.User
import com.ulpgc.proyectopamn.domain.ports.input.LoginInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway

class LoginUseCase(
    private val authGateway: AuthGateway
) : LoginInputPort {

    override suspend fun execute(email: String, password: String): Result<User> {
        val cleanEmail = email.trim()
        // Si el email ni siquiera tiene formato válido, no hace falta preguntar al gateway.
        if (!CredentialsValidator.isValidEmail(cleanEmail)) {
            return Result.failure(AuthError.InvalidEmail)
        }
        return authGateway.login(cleanEmail, password)
    }
}
