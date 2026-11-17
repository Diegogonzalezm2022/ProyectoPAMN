package com.ulpgc.proyectopamn.domain.model

/**
 * Resultado de operaciones de Autenticación en el Dominio
 */
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}
