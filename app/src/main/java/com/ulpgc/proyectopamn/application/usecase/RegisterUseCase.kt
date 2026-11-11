package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.AuthError
import com.ulpgc.proyectopamn.domain.model.CredentialsValidator
import com.ulpgc.proyectopamn.domain.model.User
import com.ulpgc.proyectopamn.domain.ports.input.RegisterInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway

class RegisterUseCase(
    private val authGateway: AuthGateway
) : RegisterInputPort {

    override suspend fun execute(name: String, email: String, password: String): Result<User> {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        return when {
            !CredentialsValidator.isValidName(cleanName) -> Result.failure(AuthError.EmptyName)
            !CredentialsValidator.isValidEmail(cleanEmail) -> Result.failure(AuthError.InvalidEmail)
            !CredentialsValidator.isValidPassword(password) -> Result.failure(AuthError.WeakPassword)
            else -> authGateway.register(cleanName, cleanEmail, password)
        }
    }
}
