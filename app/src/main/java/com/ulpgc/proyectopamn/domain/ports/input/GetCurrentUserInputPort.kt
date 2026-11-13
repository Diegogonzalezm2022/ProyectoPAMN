package com.ulpgc.proyectopamn.domain.ports.input

import com.ulpgc.proyectopamn.domain.model.User

/** Caso de uso: saber quién tiene la sesión iniciada (o `null` si nadie). */
interface GetCurrentUserInputPort {
    fun execute(): User?
}
