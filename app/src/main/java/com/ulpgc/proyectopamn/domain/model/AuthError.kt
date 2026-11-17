package com.ulpgc.proyectopamn.domain.model

/**
 * Motivos por los que puede fallar una operación de autenticación.
 *
 * Son errores del dominio: la UI decide qué texto mostrar para cada uno.
 */
sealed class AuthError : Exception() {
    data object EmptyName : AuthError()
    data object InvalidEmail : AuthError()
    data object WeakPassword : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object InvalidCredentials : AuthError()
    data object NotLoggedIn : AuthError()
}
