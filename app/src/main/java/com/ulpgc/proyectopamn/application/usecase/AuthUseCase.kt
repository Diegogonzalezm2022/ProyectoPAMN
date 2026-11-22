package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

/**
 * Caso de Uso de Autenticación y Gestión de Usuarios.
 *
 * Permite la autenticación con usuario, email o número de teléfono y contraseña.
 */
class AuthUseCase(
    private val authOutputPort: AuthOutputPort,
    private val productOutputPort: ProductOutputPort,
) : AuthInputPort {

    override suspend fun getCurrentUser(): UserProfile? {
        return authOutputPort.getCurrentUser()
    }

    override suspend fun login(identifier: String, password: String): AuthResult<UserProfile> {
        val trimmedIdentifier = identifier.trim()
        val trimmedPassword = password

        if (trimmedIdentifier.isEmpty()) {
            return AuthResult.Error("El identificador (usuario, email o teléfono) es requerido")
        }
        if (trimmedPassword.isEmpty()) {
            return AuthResult.Error("La contraseña es requerida")
        }

        return when {
            trimmedIdentifier.contains("@") -> {
                authOutputPort.loginWithEmail(trimmedIdentifier, trimmedPassword)
            }
            isPhoneNumber(trimmedIdentifier) -> {
                val userByPhone = authOutputPort.findUserByPhone(trimmedIdentifier)
                    ?: return AuthResult.Error("No se encontró usuario con ese número de teléfono")
                authOutputPort.loginWithEmail(userByPhone.email, trimmedPassword)
            }
            else -> {
                val userByUsername = authOutputPort.findUserByUsername(trimmedIdentifier)
                    ?: return AuthResult.Error("No se encontró usuario con ese nombre de usuario")
                authOutputPort.loginWithEmail(userByUsername.email, trimmedPassword)
            }
        }
    }

    override suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile> {
        if (user.username.isBlank()) {
            return AuthResult.Error("El nombre de usuario es obligatorio")
        }
        if (user.email.isBlank() && user.phone.isBlank()) {
            return AuthResult.Error("Debe proporcionar un email o número de teléfono")
        }
        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        val existingUsername = authOutputPort.findUserByUsername(user.username.trim())
        if (existingUsername != null) {
            return AuthResult.Error("El nombre de usuario ya está registrado")
        }

        val emailToUse = if (user.email.isBlank() && user.phone.isNotBlank()) {
            "${user.phone.filter { it.isDigit() }}@proyectopamn.app"
        } else {
            user.email.trim()
        }

        val userToRegister = user.copy(
            username = user.username.trim(),
            email = emailToUse,
            phone = user.phone.trim()
        )

        return authOutputPort.register(userToRegister, password)
    }

    override suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile> {
        if (user.username.isBlank()) {
            return AuthResult.Error("El nombre de usuario no puede estar vacío")
        }
        val current = authOutputPort.getCurrentUser()
            ?: return AuthResult.Error("No hay sesión activa")
        // Regla de negocio: el email es el que usa Firebase Auth para entrar y no se cambia
        // desde el perfil. Si se cambiara solo en Firestore, el usuario no podría volver a entrar.
        return authOutputPort.updateProfile(user.copy(email = current.email))
    }

    override suspend fun logout() {
        authOutputPort.logout()
    }

    override suspend fun deleteAccount(password: String): AuthResult<Unit> {
        if (password.isEmpty()) {
            return AuthResult.Error("Escribe tu contraseña para confirmar")
        }
        val user = authOutputPort.getCurrentUser()
            ?: return AuthResult.Error("No hay sesión activa")

        // El orden importa: si algo falla, se para antes de borrar nada que no se pueda recuperar.
        // 1. Comprobar que es el dueño.
        val reauth = authOutputPort.reauthenticate(password)
        if (reauth is AuthResult.Error) return reauth

        // 2. Retirar lo que tiene en venta (lo ya vendido se conserva como historial).
        //    Va antes que la cuenta porque, sin sesión, la base de datos no dejaría borrarlos.
        productOutputPort.deleteAvailableBySeller(user.id).onFailure {
            return AuthResult.Error("No se pudieron retirar tus productos. Inténtalo de nuevo.")
        }

        // 3. Borrar perfil y cuenta.
        return authOutputPort.deleteAccount()
    }

    private fun isPhoneNumber(text: String): Boolean {
        return text.isNotEmpty() && (text.startsWith("+") || text.all { it.isDigit() || it == '-' || it == ' ' })
    }
}
