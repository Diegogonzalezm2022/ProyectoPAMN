package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile

/**
 * Puerto de Entrada (Primary Port) para Autenticación y Gestión de Usuarios.
 */
interface AuthInputPort {
    fun getCurrentUser(): UserProfile?
    suspend fun login(identifier: String, password: String): AuthResult<UserProfile>
    suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile>
    suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile>
    suspend fun logout()
    suspend fun deleteAccount(): AuthResult<Unit>
}
