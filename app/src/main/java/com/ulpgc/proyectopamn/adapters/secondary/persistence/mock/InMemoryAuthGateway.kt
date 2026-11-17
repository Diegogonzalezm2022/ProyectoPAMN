package com.ulpgc.proyectopamn.adapters.secondary.persistence.mock

import com.ulpgc.proyectopamn.domain.model.AuthError
import com.ulpgc.proyectopamn.domain.model.User
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway
import java.util.UUID

/**
 * Autenticación falsa que guarda las cuentas en memoria.
 *
 * Sirve para desarrollar y probar sin Firebase. Al cerrar la app se pierde todo,
 * por eso trae una cuenta de prueba ya creada.
 */
class InMemoryAuthGateway : AuthGateway {

    // La contraseña solo vive aquí dentro: el resto de la app trabaja con User.
    private data class Account(val user: User, val password: String)

    // Clave = email en minúsculas, para que "Ana@x.com" y "ana@x.com" sean la misma cuenta.
    private val accounts = mutableMapOf<String, Account>()
    private var loggedUser: User? = null

    init {
        val demo = User(id = "demo", name = "Demo", email = "demo@pamn.test")
        accounts[demo.email] = Account(demo, password = "123456")
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        val key = email.lowercase()
        if (key in accounts) return Result.failure(AuthError.EmailAlreadyInUse)

        val user = User(id = UUID.randomUUID().toString(), name = name, email = email)
        accounts[key] = Account(user, password)
        loggedUser = user
        return Result.success(user)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val account = accounts[email.lowercase()]
        // Mismo error si no existe el email o si la contraseña falla: no damos pistas a un atacante.
        if (account == null || account.password != password) {
            return Result.failure(AuthError.InvalidCredentials)
        }
        loggedUser = account.user
        return Result.success(account.user)
    }

    override suspend fun logout() {
        loggedUser = null
    }

    override fun currentUser(): User? = loggedUser

    override suspend fun deleteAccount(): Result<Unit> {
        val user = loggedUser ?: return Result.failure(AuthError.NotLoggedIn)
        accounts.remove(user.email.lowercase())
        loggedUser = null
        return Result.success(Unit)
    }
}
