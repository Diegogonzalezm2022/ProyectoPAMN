package com.ulpgc.proyectopamn.domain.ports.output

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile

/**
 * Puerto de Salida (Secondary Port) para los adaptadores de autenticación e infraestructura.
 */
interface AuthOutputPort {
    fun getCurrentUser(): UserProfile?
    suspend fun loginWithEmail(email: String, password: String): AuthResult<UserProfile>
    suspend fun findUserByUsername(username: String): UserProfile?
    suspend fun findUserByPhone(phone: String): UserProfile?
    suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile>
    suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile>
    suspend fun logout()
    suspend fun deleteAccount(): AuthResult<Unit>
}
