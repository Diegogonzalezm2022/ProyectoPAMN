package com.ulpgc.proyectopamn.domain.ports.output

import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile

/**
 * Puerto de Salida (Secondary Port) para los adaptadores de autenticación e infraestructura.
 */
interface AuthOutputPort {
    /** `suspend` porque el perfil completo vive en la base de datos y hay que ir a buscarlo. */
    suspend fun getCurrentUser(): UserProfile?
    suspend fun loginWithEmail(email: String, password: String): AuthResult<UserProfile>
    suspend fun findUserByUsername(username: String): UserProfile?
    suspend fun findUserByPhone(phone: String): UserProfile?
    suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile>
    suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile>
    suspend fun logout()
    /** Pide la contraseña para confirmar que es el dueño antes de borrar nada. */
    suspend fun deleteAccount(password: String): AuthResult<Unit>
}
