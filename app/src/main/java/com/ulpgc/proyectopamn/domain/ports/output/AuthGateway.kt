package com.ulpgc.proyectopamn.domain.ports.output

import com.ulpgc.proyectopamn.domain.model.User

/**
 * Puerto de salida para la autenticación.
 *
 * El dominio solo conoce este contrato; quien lo implemente (mock en memoria,
 * Firebase Auth...) decide cómo se guardan y comprueban las credenciales.
 * Las funciones son `suspend` porque la implementación real (Firebase) es asíncrona.
 */
interface AuthGateway {

    /** Crea la cuenta y deja la sesión iniciada. Falla si el email ya está registrado. */
    suspend fun register(name: String, email: String, password: String): Result<User>

    /** Inicia sesión. Falla si el email o la contraseña no son correctos. */
    suspend fun login(email: String, password: String): Result<User>

    suspend fun logout()

    /** Usuario con la sesión iniciada, o `null` si no hay ninguno. */
    fun currentUser(): User?

    /** Borra la cuenta del usuario actual y cierra su sesión. */
    suspend fun deleteAccount(): Result<Unit>
}
