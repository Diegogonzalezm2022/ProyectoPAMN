package com.ulpgc.proyectopamn.adapters.secondary.persistence.mock

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort

/**
 * Adaptador Secundario Falso (Mock en Memoria).
 *
 * Implementa [AuthOutputPort] permitiendo probar la app localmente sin Firebase.
 */
class InMemoryAuthGateway : AuthOutputPort {

    private data class Account(val userProfile: UserProfile, val password: String)

    private val accounts = mutableMapOf<String, Account>()
    private var loggedUser: UserProfile? = null

    init {
        val demo = UserProfile(
            id = "demo",
            username = "DemoUser",
            email = "demo@pamn.test",
            phone = "+34600000000",
            description = "Cuenta de prueba en memoria"
        )
        accounts[demo.email.lowercase()] = Account(demo, password = "123456")
    }

    override fun getCurrentUser(): UserProfile? = loggedUser

    override suspend fun loginWithEmail(email: String, password: String): AuthResult<UserProfile> {
        val account = accounts[email.lowercase()]
            ?: return AuthResult.Error("Credenciales incorrectas")

        if (account.password != password) {
            return AuthResult.Error("Contraseña incorrecta")
        }

        loggedUser = account.userProfile
        return AuthResult.Success(account.userProfile)
    }

    override suspend fun findUserByUsername(username: String): UserProfile? {
        return accounts.values.find { it.userProfile.username.equals(username, ignoreCase = true) }?.userProfile
    }

    override suspend fun findUserByPhone(phone: String): UserProfile? {
        return accounts.values.find { it.userProfile.phone == phone }?.userProfile
    }

    override suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile> {
        val emailKey = user.email.lowercase()
        if (emailKey.isNotBlank() && emailKey in accounts) {
            return AuthResult.Error("El correo electrónico ya está registrado")
        }

        val newId = if (user.id.isBlank()) "local_" + System.currentTimeMillis() else user.id
        val newProfile = user.copy(id = newId)

        accounts[emailKey.ifBlank { newId }] = Account(newProfile, password)
        loggedUser = newProfile
        return AuthResult.Success(newProfile)
    }

    override suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile> {
        val current = loggedUser ?: return AuthResult.Error("No hay sesión activa")
        val oldAccount = accounts.values.find { it.userProfile.id == current.id }
            ?: return AuthResult.Error("Usuario no encontrado")

        val updatedAccount = Account(user, oldAccount.password)
        accounts.remove(oldAccount.userProfile.email.lowercase())
        accounts[user.email.lowercase().ifBlank { user.id }] = updatedAccount
        loggedUser = user
        return AuthResult.Success(user)
    }

    override suspend fun logout() {
        loggedUser = null
    }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        val current = loggedUser ?: return AuthResult.Error("No hay sesión activa")
        accounts.values.removeIf { it.userProfile.id == current.id }
        loggedUser = null
        return AuthResult.Success(Unit)
    }
}
