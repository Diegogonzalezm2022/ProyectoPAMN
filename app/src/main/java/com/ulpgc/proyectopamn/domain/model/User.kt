package com.ulpgc.proyectopamn.domain.model

/**
 * Usuario de la app tal y como lo entiende el dominio.
 *
 * No guarda la contraseña: de comprobarla se encarga el adaptador de autenticación
 * (ahora el mock local, más adelante Firebase Auth).
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
)
