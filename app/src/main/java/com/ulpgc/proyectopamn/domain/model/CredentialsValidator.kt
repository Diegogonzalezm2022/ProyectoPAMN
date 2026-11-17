package com.ulpgc.proyectopamn.domain.model

/**
 * Reglas de negocio para la validación de credenciales en el dominio.
 */
object CredentialsValidator {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isValidName(name: String): Boolean = name.isNotBlank()

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    fun isValidPhone(phone: String): Boolean =
        phone.isEmpty() || (phone.startsWith("+") || phone.all { it.isDigit() || it == '-' || it == ' ' })
}
