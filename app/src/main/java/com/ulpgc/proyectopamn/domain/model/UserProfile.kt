package com.ulpgc.proyectopamn.domain.model

/**
 * Entidad de Dominio: Perfil de Usuario
 *
 * Contiene la información mínima requerida para el usuario en la aplicación.
 * Esta clase es pura y no depende de ninguna biblioteca externa ni de Android.
 */
data class UserProfile(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val description: String = ""
)
