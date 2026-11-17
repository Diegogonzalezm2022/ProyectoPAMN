package com.ulpgc.proyectopamn.domain.model

/**
 * Reglas de negocio sobre los datos de una cuenta.
 *
 * Están en el dominio (y no en la pantalla) para que se cumplan
 * da igual desde dónde se cree la cuenta.
 */
object CredentialsValidator {

    // Firebase Auth exige al menos 6 caracteres; usamos la misma regla desde ya.
    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidName(name: String): Boolean = name.isNotBlank()

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH
}
